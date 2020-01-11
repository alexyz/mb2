import java.util.Comparator;

public class CCmp implements Comparator<C> {
    public static final CCmp instance = new CCmp();
    @Override
    public int compare(C c1, C c2) {
        if (c1 != null) {
            if (c2 != null) {
                int c = (int) Math.signum(c1.r-c2.r);
                if (c == 0) c =(int) Math.signum(c1.i-c2.i);
                return c;
            } else {
                return -1;
            }
        } else if (c2 != null) {
            return 1;
        } else {
            return 0;
        }
    };
}
