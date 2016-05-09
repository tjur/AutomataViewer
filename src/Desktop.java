import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 * Główny pulpit, który zarządza wszystkimi panelami dla automatu
 * @author Tomasz Jurkiewicz
 */
public class Desktop extends JDesktopPane
{
    private Automaton automaton;
    private int K, N;
    
    private PaintPanel paintPanel;
    private TextPanel textPanel;
    
    public Desktop()
    {
        this.setDesktopManager(new ImmovableDesktopManager());
        setBackground(new Color(224, 224, 224));
        
        paintPanel = new PaintPanel();
        add(paintPanel);
        
        int width = paintPanel.getSize().width;
        
        textPanel = new TextPanel(width);
        add(textPanel);
        
        setAutomaton(new Automaton("1 0"));
        textPanel.getTextArea().setText("2 4 1 0 3 0 0 1 1 2");
        
        for (JInternalFrame frame : getAllFrames())
        {
            try
            {
                if (!(frame instanceof PaintPanel))
                    frame.setIcon(true);
            } 
            catch (PropertyVetoException ex)
            {
                System.err.println(ex.getMessage());
            }
        }
        
        textPanel.addPropertyChangeListener("repaintGraph", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                paintPanel.repaintGraph();
            }
        });
        
        textPanel.addPropertyChangeListener("setAutomaton", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                setAutomaton(automaton);
            }
        });
    }
    
    private void setAutomaton(Automaton automaton)
    {
        this.automaton = automaton;
        paintPanel.setAutomaton(automaton);
        textPanel.setAutomaton(automaton);
    }
    
    public int getAutomatonK()
    {
        return automaton.getK();
    }
    
    public String getAutomatonString()
    {
        return automaton.toString();
    }
    
    public PaintPanel getPaintPanel()
    {
        return paintPanel;
    }
    
    public TextPanel getTextPanel()
    {
        return textPanel;
    }
    
    private class ImmovableDesktopManager extends DefaultDesktopManager 
    {       
        @Override
        public void dragFrame(JComponent f, int x, int y) 
        {
            if (!(f instanceof PaintPanel))
                super.dragFrame( f, x, y );
        }
    }
}
