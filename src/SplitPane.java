
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;


public class SplitPane extends JSplitPane
{
    private Automaton automaton;
    private int K, N;
    
    public PaintPanel paintPanel;
    public TextToolbar textPanel;
    
    public SplitPane()
    {
        super(JSplitPane.HORIZONTAL_SPLIT);
        
        setBackground(new Color(224, 224, 224));
        
        paintPanel = new PaintPanel();
        setTopComponent(paintPanel);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JScrollPane(innerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        rightPanel.setMinimumSize(new Dimension(300, 0));
        setBottomComponent(rightPanel);
        setResizeWeight(1.0);
        
        textPanel = new TextToolbar("Text Toolbar");
        innerPanel.add(textPanel);
        innerPanel.add(new TestToolbar("Toolbar 2"));
        innerPanel.add(new TestToolbar("Toolbar 3"));
        
        setAutomaton(new Automaton("1 0"));
        textPanel.getTextArea().setText("2 4 1 0 3 0 0 1 1 2");
        
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
    
    public TextToolbar getTextPanel()
    {
        return textPanel;
    }
}
