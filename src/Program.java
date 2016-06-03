
import AutomataViewer.AutomataViewer;
import java.awt.Dimension;
import javax.swing.JFrame;

public class Program
{

    public static void main(String[] args) 
    {
        JFrame frame = new JFrame("Automata viewer");
        AutomataViewer automataViewer = new AutomataViewer(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1250,750);
        frame.setMinimumSize(new Dimension(700, 525));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        automataViewer.repaint();
    }
}
