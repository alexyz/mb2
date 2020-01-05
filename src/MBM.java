import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MBM {

    private static void println(Object o) {
        MBJF.println("MBM", o);
    }

    public final BufferedImage image;
    public double r1, r2, i1, i2;
    public int in, out, iter;

    private final int width, height;
    // FIXME either make this an object, or totally inline it...
    private final double[][][] aaa; // { cr, ci, zr, zi, escape }
    private volatile boolean isrunning;
    private List<Thread> threads = new ArrayList<>();

    public MBM(int w, int h) {
        this.image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        this.width = w;
        this.height = h;
        this.aaa = new double[height][width][5];
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
        this.in = width * height;
        this.out = 0;
        this.iter = 0;
        int[] c = {255};
        for (int y = 0; y < height; y++) {
            double[][] aa = aaa[y];
            for (int x = 0; x < width; x++) {
                double[] a = aa[x];
                a[0] = xtor(x);
                a[1] = ytoi(y);
                a[2] = 0;
                a[3] = 0;
                a[4] = 0;
                image.getRaster().setPixel(x, y, c);
            }
        }
        println("aaa[0][0]=" + Arrays.toString(aaa[0][0]));
        println("aaa[h][w]=" + Arrays.toString(aaa[height - 1][width - 1]));
    }

    // 4 cores... update different rows
    public void start() {
        println("start iter=" + iter + " in=" + in + " out=" + out);
        if (isrunning) throw new RuntimeException();
        // doesn't really work for m>1... iter is contended, in points are not evenly distributed...
        // could randomly distribute the lines/points, but would murder the cache...
        // could get all the threads to sync after each iter block
        // or a line work queue... but needs constantly refilling
        int m = 1;
        int yr = height / m;
        for (int n = 0; n < m; n++) {
            int y1 = yr * n;
            int y2 = yr * (n + 1);
            Thread t = new Thread(() -> trun(y1, y2));
            t.setPriority(Thread.MIN_PRIORITY);
            t.setDaemon(true);
            t.start();
            threads.add(t);
        }
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

    public void trun(int y1, int y2) {
        try {
            println("trun y1, y2 = " + y1 + ", " + y2);
            int[] c = new int[1];
            while (isrunning) {
                trun2(y1, y2, c);
            }
        } catch (Exception e) {
            println("trun: " + e);
        } finally {
            println("trun stop");
        }
    }

    public void trun2(int y1, int y2, int[] c) {
        int istep = 256;
        WritableRaster raster = image.getRaster();

        for (int y = y1; isrunning && y < y2; y++) {
            double[][] aa = aaa[y];

            for (int x = 0; isrunning && x < width; x++) {
                double[] a = aa[x];
                double e = a[4];

                if (e == 0) {
                    // z[n+1] = z[n]^2 + c
                    // (a+bi)^2 = a^2 + 2abi - b^2
                    double cr = a[0], ci = a[1], zr = a[2], zi = a[3];
                    for (int n = 0; isrunning && n < istep; n++) {
                        double zr2 = ((zr * zr) - (zi * zi)) + cr;
                        double zi2 = (2 * zr * zi) + ci;
                        zr = zr2;
                        zi = zi2;
                        if (zr * zr + zi * zi >= 4) {
                            e = iter + n + 1;
                            in--;
                            out++;
//                            c[0] = 0;
//                            raster.setPixel(x, y, c);
                            break;
                        }
                    }
                    a[2] = zr;
                    a[3] = zi;
                    a[4] = e;
                }

                // update escaped pixel colour
                if (e > 0) {
                    int v = (int) ((255 * e) / (iter + istep));
                    raster.getPixel(x, y, c);
                    if (c[0] != v) {
                        c[0] = v;
                        raster.setPixel(x, y, c);
                    }
                }
                // distance from origin shading... very unstable
//                if (e == 0) {
//                    int v = (int) (Math.hypot(a[2], a[3]) * 128);
//                    c[0] = v;
//                    raster.setPixel(x, y, c);
//                }
            }
        }

        iter += istep;
    }
}
