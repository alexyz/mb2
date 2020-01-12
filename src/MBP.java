import java.util.*;

public class MBP {
    public final short x, y;
    public final double cr, ci;
    public double zr, zi;
    public int escape;
    //public C[] zlist;
    //public HashMap<C,C> zset;
    public double distance;

    public MBP(int x, int y, double cr, double ci) {
        this.x = (short) x;
        this.y = (short) y;
        this.cr = cr;
        this.ci = ci;
        //this.zlist = new C[256];
    }

//    public void push(int i, C c) {
//        if (i >= zlist.length) {
//            zlist = Arrays.copyOf(zlist, zlist.length+256);
//        }
//        zlist[i] = c;
//    }

//    public boolean add(C c) {
//        if (zset == null) {
//            zset = new HashMap<>(256, 0.75f);
//        }
//        C d = zset.get(c);
//        if (d != null) {
//            System.out.println("cycle " + d.n + ", " + c.n + " => " + (c.n-d.n));
//            return true;
//        } else {
//            zset.put(c,c);
//            return false;
//        }
//    }
//
//    public void clear() {
//        if (zset != null) {
//            zset.clear();
//            zset = null;
//        }
//    }
//
//    public int size() {
//        return zset != null ? zset.size() : 0;
//    }

//    public boolean isloop() {
//        Arrays.sort(zlist, CCmp.instance);
//        for (int n = 0; n < zlist.length - 1; n++) {
//            C c1 = zlist[n];
//            C c2 = zlist[n + 1];
//            if (c1 == null) return false;
//            if (c1.equals(c2)) {
//                return true;
//            }
//        }
//        return false;
//    }

    @Override
    public String toString() {
        return new C(cr,ci).toString();
    }
}
