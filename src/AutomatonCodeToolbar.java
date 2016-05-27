
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;


public class AutomatonCodeToolbar extends DockToolbar
{
    private JTextPane textPane;
    
    public AutomatonCodeToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        
        JPanel panel = getPanel();
        textPane = new JTextPane();
        textPane.setPreferredSize(new Dimension(0, 55));
        
        // create popup menu for text area
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.cut();
            }
        });
        menuItemCopy.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.copy();
            }
        });
        menuItemPaste.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.paste();
            }
        });

        popupMenu.add(menuItemCut);
        popupMenu.add(menuItemCopy);
        popupMenu.add(menuItemPaste);
        
        textPane.addMouseListener(new MouseAdapter() {
            
            @Override 
            public void mousePressed(MouseEvent ev) 
            {
                if (ev.isPopupTrigger()) 
                    popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
            }

            @Override 
            public void mouseReleased(MouseEvent ev) 
            {
                if (ev.isPopupTrigger()) 
                    popupMenu.show(ev.getComponent(), ev.getX(), ev.getY());
            }
        });
        
        panel.add(textPane, BorderLayout.CENTER);
        
        
        JButton buttonSetGraph = new JButton("OK");
        buttonSetGraph.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String matrix = textPane.getText().trim();
                try 
                {
                    if (getAutomaton().toString().equals(matrix))
                        firePropertyChange("repaintAutomaton", false, true);
                    else
                    {
                        getAutomaton().update(new Automaton(matrix));
                        firePropertyChange("updateToolbars", false, true);
                        firePropertyChange("updateAndRepaintAutomaton", false, true);
                        firePropertyChange("updateTransitions", false, true);
                    }
                } 
                catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, e.toString());
                }
            }       
        });
        JPanel borderPanel = new JPanel();
        borderPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        borderPanel.add(buttonSetGraph);
        panel.add(borderPanel, BorderLayout.EAST);
    }
    
    public void setCode(String text)
    {
        textPane.setText(text);
    }

    @Override
    protected void update() 
    {
        textPane.setText(getAutomaton().toString());
    }
}