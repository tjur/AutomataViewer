
import java.awt.BorderLayout;
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
import javax.swing.JTextArea;


public class TextToolbar extends DockToolbar
{
    private Automaton automaton;
    private JTextArea textArea;
    private JPopupMenu popupMenu;
    
    public TextToolbar(String name)
    {
        super(name);
        
        textArea = new JTextArea();
        popupMenu = new JPopupMenu();
        
        // create popup menu for text area
        JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textArea.cut();
            }
        });
        menuItemCopy.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textArea.copy();
            }
        });
        menuItemPaste.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textArea.paste();
            }
        });

        popupMenu.add(menuItemCut);
        popupMenu.add(menuItemCopy);
        popupMenu.add(menuItemPaste);
        
        textArea.addMouseListener(new MouseAdapter() {
            
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
        
        panel.add(textArea, BorderLayout.CENTER);
        
        
        JButton buttonSetGraph = new JButton("OK");
        buttonSetGraph.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String matrix = textArea.getText().trim();
                try 
                {
                    if (automaton.toString().equals(matrix))
                        firePropertyChange("repaintGraph", false, true);
                    else
                    {
                        automaton.update(new Automaton(matrix));
                        firePropertyChange("setAutomaton", null, automaton);
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
    
    public JTextArea getTextArea()
    {
        return textArea;
    }
    
    public void setAutomaton(Automaton automaton)
    {
        this.automaton = automaton;
        updateTextArea();
    }
    
    public void updateTextArea()
    {
        textArea.setText(automaton.toString());
    }
}