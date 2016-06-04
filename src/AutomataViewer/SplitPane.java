
package AutomataViewer;

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
    
    private final int MIN_WIDTH = 320;
    
    public SplitPane()
    {
        super(JSplitPane.HORIZONTAL_SPLIT);
        
        setBackground(new Color(224, 224, 224));
        
        automaton = new Automaton("2 5 1 0 2 1 3 2 4 3 0 0");
        paintPanel = new PaintPanel(automaton);
        setTopComponent(paintPanel);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        rightPanel.add(new JScrollPane(innerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        Dimension rightPanelMinimumSize = new Dimension(MIN_WIDTH, 0);
        rightPanel.setMinimumSize(rightPanelMinimumSize);
        setBottomComponent(rightPanel);
        setResizeWeight(1.0);
        
        codeToolbar = new AutomatonCodeToolbar("Automaton code", automaton);
        innerPanel.add(codeToolbar);
        
        ApplyWordToolbar applyWordToolbar = new ApplyWordToolbar("Apply word", automaton);
        innerPanel.add(applyWordToolbar);
        
        ShortestResetWordToolbar resetWordToolbar = new ShortestResetWordToolbar("Shortest reset word", automaton);
        innerPanel.add(resetWordToolbar);
        
        ShortestWordForSubsetToolbar shortestWordSubsetToolbar = new ShortestWordForSubsetToolbar("Shortest word for subset", automaton);
        innerPanel.add(shortestWordSubsetToolbar);
        
        BasicPropertiesToolbar basicPropertiesToolbar = new BasicPropertiesToolbar("Basic properties", automaton);
        innerPanel.add(basicPropertiesToolbar);
        
        dockToolbars.add(codeToolbar);
        dockToolbars.add(applyWordToolbar);
        dockToolbars.add(resetWordToolbar);
        dockToolbars.add(shortestWordSubsetToolbar);
        dockToolbars.add(basicPropertiesToolbar);
        
        updateToolbars();
        
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
    
    public AutomatonCodeToolbar getCodeToolbar()
    {
        return codeToolbar;
    }
    
    public ArrayList<DockToolbar> getDockToolbars()
    {
        return dockToolbars;
    }
    
    public void realign()
    {
        codeToolbar.realign();
    }
}
