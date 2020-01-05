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
        //model.run();
        Graphics2D g2 = (Graphics2D) g;
        //int w = getWidth(), h = getHeight();
        g2.drawImage(model.image, null, 0, 0);
        g2.setColor(Color.green);
        int ys = 20;
        g2.drawString("it=" + model.iter + " in=" + model.in + " out=" + model.out, 20, ys += 20);
        g2.drawString("r1,i1=" + model.r1+", " + model.i1 + " r2,i2=" + model.r2 + ", " + model.i2, 20, ys += 20);
        if (mp != null) {
            g2.drawString("m=" + mp.x + ", " + mp.y + " => " + model.xtor(mp.x) +", " + model.ytoi(mp.y), 20, ys += 20);
        }
        if (p1 != null) {
            g2.drawString("p1=" + p1.x + ", " + p1.y + " => " + model.xtor(p1.x) +", " + model.ytoi(p1.y), 20, ys += 20);
        }
        if (p2 != null) {
            g2.drawString("p2=" + p2.x + ", " + p2.y + " => " + model.xtor(p2.x) +", " + model.ytoi(p2.y), 20, ys += 20);
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
            model.stop();
            model.reinit();
            model.start();
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
            model.stop();
            double r1 = model.xtor(Math.min(p1.x, p2.x)), r2 = model.xtor(Math.max(p1.x, p2.x));
            double i1 = model.ytoi(Math.min(p1.y, p2.y)), i2 = model.ytoi(Math.max(p1.y, p2.y));
            model.init(r1, i1, r2, i2);
            model.start();
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
