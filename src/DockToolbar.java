
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;


public abstract class DockToolbar extends JToolBar
{
    private JPanel panel; // content panel
    private final String name;
    private Automaton automaton;
    
    private boolean floating;
    private Dimension dockSize;
    private Dimension floatSize;
    
    DockToolbar(String name)
    {
        super(name);
        this.name = name;
        floating = false;
        setLayout(new FlowLayout());
        setOrientation(javax.swing.SwingConstants.HORIZONTAL);
        
        panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 3), name));
        add(panel);
        
        addAncestorListener(new AncestorListener() {
            
            @Override
            public void ancestorAdded(AncestorEvent event)
            {
                if (SwingUtilities.getWindowAncestor(DockToolbar.this) instanceof JDialog) 
                {
                    floating = true;
                    JDialog toolBarDialog = (JDialog) SwingUtilities.getWindowAncestor(DockToolbar.this);
                    toolBarDialog.setResizable(true);
                    if (dockSize == null)
                        dockSize = floatSize = panel.getSize();
                    panel.setSize(floatSize);
                    DockToolbar.this.setVisible(false);
                    DockToolbar.this.remove(panel);
                    toolBarDialog.add(panel);
                    toolBarDialog.addComponentListener(new ComponentAdapter()
                    {
                        @Override
                        public void componentResized(ComponentEvent ev) 
                        {
                            toolBarDialog.setPreferredSize(toolBarDialog.getSize());
                        }
                    });
                    toolBarDialog.addWindowListener(new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent e) 
                        {
                            floating = false;
                            toolBarDialog.remove(panel);
                            DockToolbar.this.setVisible(true);
                            DockToolbar.this.add(panel);
                            floatSize = DockToolbar.this.getSize();
                            panel.setSize(dockSize);
                        }
                    });
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {}

            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });
        
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent ev) {
                
                Dimension dim = new Dimension(
                    DockToolbar.this.getSize().width - 30,
                    panel.getSize().height
                );
                panel.setPreferredSize(dim);
                revalidate();
            }
        });
    }
    
    protected JPanel getPanel()
    {
        return panel;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    public Automaton getAutomaton()
    {
        return automaton;
    }
    
    public void setAutomaton(Automaton automaton)
    {
        this.automaton = automaton;
    }
    
    @Override
    public void setVisible(boolean b)
    {
        boolean oldValue = isVisible();
        super.setVisible(b);
        
        if (isVisible())
            update();
        
        firePropertyChange("setVisible", oldValue, b);
    }
    
    public void Dock()
    {
        if (floating)
        {
            JDialog toolBarDialog = (JDialog) SwingUtilities.getWindowAncestor(this);
            toolBarDialog.dispatchEvent(new WindowEvent(toolBarDialog, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    // updates toolbar only if it is visible
    public void updateToolbar()
    {
        if (isVisible())
            update();
    }
    
    protected abstract void update();
}