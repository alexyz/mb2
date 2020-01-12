import java.util.Arrays;
import java.util.Comparator;

public class C {
    public static void main(String[] args) {
        //C[] a = new C[] { new C(1,1), new C(1,0), new C(0,1), new C(0,0), null };
        C[] a = new C[256];
        Arrays.sort(a, CCmp.instance);
        System.out.println(Arrays.toString(a));
    }

    public final double r, i;
    public final int n;

    public C(double r, double i) {
        this(r, i, 0);
    }

    public C(double r, double i, int n) {
        this.r = r;
        this.i = i;
        this.n = n;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(r) + Double.hashCode(i);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof C) {
            C c = (C) obj;
            return r==c.r && i == c.i;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(r);
        if (i >= 0) sb.append("+");
        sb.append(i).append("i");
        if (n > 0) sb.append("[").append(n).append("]");
        return sb.toString();
    }
}

