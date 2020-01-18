
public class MBP {
    public final short x, y;
    public final double cr, ci;
    public double zr, zi;
    public int it, escape;
    public double distance;

    public MBP(int x, int y, double cr, double ci) {
        this.x = (short) x;
        this.y = (short) y;
        this.cr = cr;
        this.ci = ci;
    }

    @Override
    public String toString() {
        return new C(cr,ci).toString();
    }
}
