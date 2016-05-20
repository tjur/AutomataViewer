
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class TestToolbar extends DockToolbar
{
    public TestToolbar(String name)
    {
        super(name);
        JPanel panel = getPanel();
        JTextArea ta = new JTextArea();
        ta.setPreferredSize(new Dimension(300, 300));
        ta.setMinimumSize(new Dimension(300, 300));
        panel.add(ta);
    }
}
