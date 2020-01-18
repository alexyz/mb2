import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.*;
import java.util.List;

public class MBM {

    private static void println(Object o) {
        MBJF.println("MBM", o);
    }

    public final Deque<MBP> unknown = new ArrayDeque<>();
    public final List<MBP> out = new ArrayList<>(), in = new ArrayList<>();
    public final BufferedImage image;
    public double r1, r2, i1, i2;
    private final int width, height;
    private List<MBMR> threads = new ArrayList<>();
    public volatile int twait;

    public MBM(int w, int h) {
        this.image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        this.width = w;
        this.height = h;
        reinit();
    }

    public void reinit() {
        println("reinit");
        init(-2.5, -1.5, 1.5, 1.5);
    }

    public double xtor(int x) {
        // map 0...w-1 to r1...r2
        if (x < 0 || x >= width) throw new RuntimeException();
        return ((x * (r2 - r1)) / (width - 1)) + r1;
    }

    public double ytoi(int y) {
        // map 0...h-1 to i1...i2
        if (y < 0 || y >= height) throw new RuntimeException();
        return ((y * (i2 - i1)) / (height - 1)) + i1;
    }

    public void init(double r1, double i1, double r2, double i2) {
        println("init " + r1 + "," + i1 + " ... " + r2 + "," + i2);
        if (r1 >= r2 || i1 >= i2) throw new RuntimeException();
        this.r1 = r1;
        this.r2 = r2;
        this.i1 = i1;
        this.i2 = i2;
        synchronized (unknown) {
            this.in.clear();
            this.out.clear();
            this.unknown.clear();
            for (MBMR t : threads) {
                t.isrunning = false;
                // fixme doesn't wait for threads to stop
            }
            image.getGraphics().setColor(Color.gray);
            image.getGraphics().drawRect(0, 0, image.getWidth(), image.getHeight());
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    unknown.add(new MBP(x, y, xtor(x), ytoi(y)));
                }
            }
            unknown.notifyAll();
        }
    }

    public void start() {
        for (int n = 0; n < 4; n++) {
            MBMR t = new MBMR(image.getRaster());
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            t.start();
            threads.add(t);
        }
    }

    public MBP nextp(MBP p) {
        synchronized (unknown) {
            if (p != null) {
                if (p.escape == 0) {
                    unknown.add(p);
                    unknown.notify();
                } else if (p.escape < 0) {
                    in.add(p);
                } else if (p.escape > 0) {
                    out.add(p);
                }
            }
            while ((p = unknown.poll()) == null) {
                try {
                    twait++;
                    unknown.wait();
                    twait--;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return p;
        }
    }

    private class MBMR extends Thread {
        public boolean isrunning;
        int[] c1 = new int[3], c2 = new int[3];
        long[] zhash = new long[256];
        WritableRaster raster;

        public MBMR(WritableRaster r) {
            this.raster = r;
        }

        public void run() {
            try {
                println("run");
                MBP p = null;
                while (true) {
                    p = nextp(p);
                    if (p != null) {
                        iter(p);
                        if (p.escape != 0) {
                            p = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            } finally {
                println("run stop");
            }
        }

        public int getistep(MBP p) {
            return Math.max((p.it >> 12) << 6, 256);
        }

        public void iter(MBP p) {
            int it = p.it, itstep = getistep(p);
            if (zhash.length != itstep) zhash = new long[itstep];
            isrunning = true;

            // z[n+1] = z[n]^2 + c
            // (a+bi)^2 = a^2 + 2abi - b^2
            double cr = p.cr, ci = p.ci, zr = p.zr, zi = p.zi;
            int e = 0, x = p.x, y = p.y;

            for (int n = 0; n < itstep && isrunning; n++) {
                double zr2 = ((zr * zr) - (zi * zi)) + cr;
                double zi2 = (2 * zr * zi) + ci;
                zr = zr2;
                zi = zi2;
                if (zr * zr + zi * zi > 4) {
                    e = it + n + 1;
                    break;
                }

                long zrhash = Double.doubleToRawLongBits(zr), zihash = Double.doubleToRawLongBits(zi);
                zhash[n] = zrhash ^ (zihash << 32) ^ (zihash >>> 32);
            }

            p.escape = e;
            p.zr = zr;
            p.zi = zi;

            if (e == 0) {
                Arrays.sort(zhash);
                for (int n = 0; n < itstep - 1; n++) {
                    if (zhash[n] == zhash[n + 1]) {
                        // *probably* in
                        // guess the cycle length
                        p.escape = e = 0 - zhash.length;
                        break;
                    }
                }
            }

            // update escaped pixel colour
            if (e > 0) {
                int v = (255 * e) / (it + itstep);
                c1[0] = v;
                c1[1] = 0;
                c1[2] = 0;
            } else if (e < 0) {
                //int v = (-255 * e) / (it + itstep);
                c1[0] = 255;
                c1[1] = 255;
                c1[2] = 255;
            } else {
                c1[0] = 127;
                c1[1] = 127;
                c1[2] = 127;
            }

            raster.getPixel(x, y, c2);
            if (c1[0] != c2[0] || c1[1] != c2[1] || c1[2] != c2[2]) {
                raster.setPixel(x, y, c1);
            }

            p.it += itstep;
        }
    }
}
