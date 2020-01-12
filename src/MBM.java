import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.*;

public class MBM {

    private static void println(Object o) {
        MBJF.println("MBM", o);
    }

    public final List<MBP> unknown = new LinkedList<>();
    public final List<MBP> out = new ArrayList<>(), in = new ArrayList<>();
    public final BufferedImage image;
    public double r1, r2, i1, i2;
    public int it;
    public long size;

    private final int width, height;
    private volatile boolean isrunning;
    private List<Thread> threads = new ArrayList<>();

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
        if (isrunning) throw new RuntimeException();
        if (r1 >= r2 || i1 >= i2) throw new RuntimeException();
        this.r1 = r1;
        this.r2 = r2;
        this.i1 = i1;
        this.i2 = i2;
        this.in.clear();
        this.out.clear();
        this.unknown.clear();
        this.it = 0;
        int[] c = new int[] { 127, 127, 127 };
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                unknown.add(new MBP(x, y, xtor(x), ytoi(y)));
                image.getRaster().setPixel(x, y, c);
            }
        }
        println("aaa[0][0]=" + unknown.get(0));
        println("aaa[h][w]=" + unknown.get(unknown.size() - 1));
    }

    // 4 cores... update different rows
    public void start() {
        println("start it=" + it);
        if (isrunning) throw new RuntimeException();
        Thread t = new Thread(() -> trun());
        t.setPriority(Thread.MIN_PRIORITY);
        t.setDaemon(true);
        t.start();
        threads.add(t);
        isrunning = true;
    }

    public void stop() {
        println("stop");
        if (!isrunning) throw new RuntimeException();
        try {
            isrunning = false;
            for (Thread t : threads) {
                t.join();
            }
            println("stopped");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void trun() {
        try {
            println("trun");
            long t1 = System.nanoTime();
            int prevout = 0;
            while (isrunning) {
                if (unknown.size() > 0) {
                    //boolean findin = prevout > 0 && out.size() + 16 >= prevout && it > 1024 && unknown.size() < 100000;
                    boolean findin = true;
                    trun2(findin);
                    prevout = out.size();
                } else {
                    Thread.sleep(1000);
                }
                long t2 = System.nanoTime();
                if (t2 > t1 + 2_000_000_000) {
                    t1 = t2;
                    System.gc();
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            println("trun stop");
        }
    }

    public int getistep() {
        return Math.max(it >> 7, 32);
    }

    public void trun2(boolean findin) {
        int[] c1 = new int[3], c2 = new int[3];
        int it = this.it, itstep = getistep();
        WritableRaster raster = image.getRaster();
        long size = 0;

        Iterator<MBP> i = unknown.iterator();
        while (i.hasNext() && isrunning) {
            MBP p = i.next();

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
                    //System.out.println("out at " + (p.distance / (it+itstep)));
                    p.escape = e = it + n + 1;
                    p.clear();
                    i.remove();
                    out.add(p);
                    break;
                }
                //p.distance = p.distance + Math.hypot(p.zr-zr,p.zi-zi);
                //p.push(it + n, new C(zr, zi));
                if (findin && p.add(new C(zr, zi, it+n))) {
                    //System.out.println("in at " + (p.distance / (it+itstep)));
                    p.escape = e = 0 - it - itstep - 1;
                    p.clear();
                    i.remove();
                    in.add(p);
                    break;
                }
            }

            p.zr = zr;
            p.zi = zi;

//            if (e == 0) {
//                if (p.isloop()) {
//                    p.escape = e = 0 - it - itstep - 1;
//                    p.clear();
//                    i.remove();
//                    in.add(p);
//                }
//            }

            // update escaped pixel colour
            if (e > 0) {
                //int v = (255 * e) / (it + itstep);
                c1[0] = 0;
                //c1[0] = (int) Math.min(255, 255.0*(p.distance/e));
                c1[1] = 0;
                c1[2] = 0;
            } else if (e < 0) {
                //int v = (-255 * e) / (it + itstep);
                c1[0] = 0;
                c1[1] = 255;
                //c1[1] = (int) Math.min(255, 255.0*(p.distance/e));
                c1[2] = 0;
            } else {
                c1[0] = 0;
                c1[1] = 0;
                c1[2] = 255;
            }

            raster.getPixel(x, y, c2);
            if (c1[0] != c2[0] || c1[1] != c2[1] || c1[2] != c2[2]) {
                raster.setPixel(x, y, c1);
            }

            size += p.size();
        }

        this.size = size;
        this.it += itstep;
    }

}
