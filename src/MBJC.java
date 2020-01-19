import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MBJC extends JComponent implements MouseListener, MouseMotionListener {
    private MBM model;
    private Point p1, p2, mp;

    public MBJC() {
        setPreferredSize(new Dimension(1024,768));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void paint(Graphics g) {
        if (model == null) {
            model = new MBM(getWidth(), getHeight());
            model.start();
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(model.image, null, 0, 0);
        g2.setColor(Color.white);
        int ys = 20;
        Runtime r = Runtime.getRuntime();
        long used = r.totalMemory()-r.freeMemory();
        g2.drawString("in=" + model.in.size() + " out=" + model.out.size() + " unknown=" + model.unknown.size(), 20, ys += 20);
        g2.drawString("maxit=" + model.maxit + " maxin=" + model.maxin + " maxout=" + model.maxout + " twait=" + model.twait + " mem=" + (used >> 20) + "M", 20, ys += 20);
        g2.drawString("c1=" + new C(model.r1, model.i1) + " c2=" + new C(model.r2, model.i2), 20, ys += 20);
        if (mp != null) {
            g2.drawString("mouse=" + mp.x + "," + mp.y + " => " + new C(model.xtor(mp.x), model.ytoi(mp.y)), 20, ys += 20);
        }
        if (p1 != null && p2 != null) {
            int minx = Math.min(p1.x, p2.x), maxx = Math.max(p1.x, p2.x);
            int miny = Math.min(p1.y, p2.y), maxy = Math.max(p1.y, p2.y);
            g2.setColor(Color.blue);
            g2.drawRect(minx, miny, maxx-minx, maxy-miny);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            model.reinit();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        p1 = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        p2 = e.getPoint();
        if (p1 != null && p1.x != p2.x && p1.y != p2.y) {
            double r1 = model.xtor(Math.min(p1.x, p2.x)), r2 = model.xtor(Math.max(p1.x, p2.x));
            double i1 = model.ytoi(Math.min(p1.y, p2.y)), i2 = model.ytoi(Math.max(p1.y, p2.y));
            model.init(r1, i1, r2, i2);
        }
        p1 = null;
        p2 = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        mp = e.getPoint();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        mp = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        p2 = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mp = e.getPoint();
    }
}
