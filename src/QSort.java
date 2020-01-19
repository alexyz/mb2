public class QSort {

    public static void qsort(long[] a, int begin, int end) {
        if (begin < end) {
            int i = qpart(a, begin, end);
            qsort(a, begin, i - 1);
            qsort(a, i + 1, end);
        }
    }

    private static int qpart(long[] a, int begin, int end) {
        long p = a[end];
        int i = begin - 1;
        for (int j = begin; j < end; j++) {
            if (a[j] <= p) {
                i++;
                long t = a[i];
                a[i] = a[j];
                a[j] = t;
            }
        }
        long t = a[i + 1];
        a[i + 1] = a[end];
        a[end] = t;
        return i + 1;
    }
}
