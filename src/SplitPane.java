
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;


public class SplitPane extends JSplitPane
{
    private final Automaton automaton;
    private int K, N;
    
    private PaintPanel paintPanel;
    
    private final AutomatonCodeToolbar codeToolbar;
    
    private ArrayList<DockToolbar> dockToolbars = new ArrayList<>();
    
    public SplitPane()
    {
        super(JSplitPane.HORIZONTAL_SPLIT);
        
        setBackground(new Color(224, 224, 224));
        
        automaton = new Automaton("0 0");
        paintPanel = new PaintPanel(automaton);
        setTopComponent(paintPanel);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JScrollPane(innerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        Dimension rightPanelMinimumSize = new Dimension(300, 0);
        rightPanel.setMinimumSize(rightPanelMinimumSize);
        setBottomComponent(rightPanel);
        setResizeWeight(1.0);
        
        codeToolbar = new AutomatonCodeToolbar("Automaton code", automaton);
        innerPanel.add(codeToolbar);
        
        ShortestResetWordToolbar resetWordToolbar = new ShortestResetWordToolbar("Shortest reset word", automaton);
        innerPanel.add(resetWordToolbar);
        
        /*ApplyWordToolbar applyWordToolbar = new ApplyWordToolbar("Apply word", automaton);
        innerPanel.add(applyWordToolbar);*/
        
        ShortestChangingWordToolbar changeWordToolbar = new ShortestChangingWordToolbar("Shortest decreasing/increasing word", automaton);
        innerPanel.add(changeWordToolbar);
        
        dockToolbars.add(codeToolbar);
        dockToolbars.add(resetWordToolbar);
        //dockToolbars.add(applyWordToolbar);
        dockToolbars.add(changeWordToolbar);
        
        updateToolbars();
        codeToolbar.setCode("2 4 1 0 3 0 0 1 1 2");
        
        codeToolbar.addPropertyChangeListener("repaintCenterAutomaton", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                paintPanel.repaintCenterAutomaton();
            }
        });
        
        codeToolbar.addPropertyChangeListener("updateAndRepaintCenterAutomaton", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                paintPanel.updateAutomatonData();
                paintPanel.repaintCenterAutomaton();
            }
        });
        
        innerPanel.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e)
            {
                if (innerPanel.getComponents().length == 1)
                {
                    rightPanel.setMinimumSize(rightPanelMinimumSize);
                    SplitPane.this.setDividerLocation(-1);
                    SplitPane.this.setEnabled(true);
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e)
            {
                if (innerPanel.getComponents().length == 0)
                {
                    rightPanel.setMinimumSize(new Dimension(0, 0));
                    SplitPane.this.setDividerLocation(1.0);
                    SplitPane.this.setEnabled(false);                   
                }
            }
        });
        
        for (DockToolbar dockToolbar : dockToolbars)
        {
            dockToolbar.addPropertyChangeListener("setVisible", new PropertyChangeListener() {
            
                @Override
                public void propertyChange(PropertyChangeEvent ev)
                {
                    int visibleToolbars = 0;
                    for (DockToolbar dt : dockToolbars)
                    {
                        if (dt.isVisible())
                            visibleToolbars++;
                    }
                    
                    if (visibleToolbars == 0)
                    {
                        rightPanel.setMinimumSize(new Dimension(0, 0));
                        SplitPane.this.setDividerLocation(1.0);
                        SplitPane.this.setEnabled(false);                
                    }
                    else if (visibleToolbars == 1 && (boolean) ev.getNewValue())
                    {
                        rightPanel.setMinimumSize(rightPanelMinimumSize);
                        SplitPane.this.setDividerLocation(-1);
                        SplitPane.this.setEnabled(true);                        
                    }
                }
            });
        }
    }
    
    private void updateToolbars()
    { 
        for (DockToolbar dockToolbar : dockToolbars)
            dockToolbar.updateToolbar();
    }
    
    public Automaton getAutomaton()
    {
        return automaton;
    }
    
    public int getAutomatonK()
    {
        return automaton.getK();
    }
    
    public String getAutomatonString()
    {
        return automaton.toString();
    }
    
    public int getSelectedStatesNumber()
    {
        return automaton.getSelectedStatesNumber();
    }
    
    public PaintPanel getPaintPanel()
    {
        return paintPanel;
    }
    
    public AutomatonCodeToolbar getTextPanel()
    {
        return codeToolbar;
    }
    
    public ArrayList<DockToolbar> getDockToolbars()
    {
        return dockToolbars;
    }
}
