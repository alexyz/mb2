import java.util.Random;

public class CSort {
    public static void main(String[] args) {
        Random r = new Random();
        double[] a1 = new double[60];
        double[] a2 = new double[a1.length];
        for (int n = 0; n < a1.length; n++) {
            a1[n] = n == 0 || r.nextBoolean() ? r.nextDouble() : a1[r.nextInt(n)];
        }
        //printa(a1);
        sort(a1, 0, a1.length, a2);
        printa(a1);
    }

    private static void printa(double[] v) {
        for (int n = 0; n < v.length; n+=2) {
            System.out.println((n/2) + ": " + v[n] + ", " + v[n+1]);
        }
    }

    private static void sort(double[] array, int start, int end, double[] temparray) {
        if ((start&1)==1 || (end&1)==1) throw new RuntimeException();
        if (end - start > 4) {
            int mid = pivot(start, end);
            System.out.println("sort " + start + ", " + mid + ", " + end);
            sort(array, start, mid, temparray);
            sort(array, mid, end, temparray);
            merge(array, start, mid, end, temparray);

        } else if (end - start == 4) {
            System.out.println("leafsort " + start);
            if (cmp(array, start,start+2) > 0) {
                swap(array, start, start+2);
            }
        } else if (end - start == 2) {
            //
        } else {
            throw new RuntimeException();
        }

    }

    private static int pivot(int start, int end) {
        int mid = start+((end-start)>>1);
        if ((mid&1)==1) mid++;
        return mid;
    }

    private static void merge(double[] a, int start, int mid, int end, double[] temp) {
        int x = start, y = mid, z = 0;
        while (z < (end-start)) {
            int c = x == mid ? 1 : y == end ? -1 : cmp (a, x, y);
            if (c <= 0) {
                set(temp, z, a, x);
                z+=2;
                x+=2;
            }
            if (c >= 0) {
                set (temp, z, a, y);
                z+=2;
                y+=2;
            }
        }
        for (int n = 0; n < (end-start); n+=2) {
            set(a, start+n, temp, n);
        }
    }

    private static void swap(double[] a, int i, int j) {
        double x = a[i], y = a[i+1];
        a[i] = a[j];
        a[i+1] = a[j+1];
        a[j] = x;
        a[j+1] = y;
    }

    private static void set(double[] a, int i, double[] b, int j) {
        a[i] = b[j];
        a[i+1] = b[j+1];
    }

    private static int cmp (double[] a, int i, int j) {
        if (a[i] > a[j]) {
            return 1;
        } else if (a[i] < a[j]) {
            return -1;
        } else if (a[i+1] > a[j+1]) {
            return 1;
        } else if (a[i+1] < a[j+1]) {
            return -1;
        } else {
            return 0;
        }
    }

}
