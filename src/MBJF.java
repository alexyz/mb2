import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MBJF extends JFrame {

    public static void main(String[] args) {
        System.out.println("maxmem=" + (Runtime.getRuntime().maxMemory()>>20) + "M");
        MBJF f = new MBJF();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.setResizable(false);
        f.setVisible(true);
        Timer t = new Timer(50, a -> SwingUtilities.invokeLater(() -> f.repaint()));
        t.start();
    }

    public static void println(String cat, Object msg) {
        DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        String datestr = df.format(new Date());
        String tname = Thread.currentThread().getName();
        System.out.println(datestr + ": " + tname + ": " + cat + ": " + msg);
    }

    public MBJF() {
        MBJC c = new MBJC();

        JPanel p = new JPanel(new BorderLayout());
        p.add(c, BorderLayout.CENTER);

        setTitle("a more honest mandelbrot");
        setContentPane(p);
        pack();
    }
}
