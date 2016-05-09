import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

/***
 * Panel zawierający pole tekstowe opisujące automat
 * @author Tomasz Jurkiewicz
 */
public class TextPanel extends JInternalFrame
{
    private Automaton automaton;
    private JTextArea textArea;
    private JPopupMenu popupMenu;
    
    public TextPanel(int width)
    {
        super("Text Panel", true, true, false, true);
        setSize(new Dimension(300, 100));
        setMinimumSize(new Dimension(300, 100));
        setBounds(width + 10, 0, 300, 100);
        setVisible(true);
        
        JPanel panel = new JPanel(new FlowLayout());
        textArea = new JTextArea();
        popupMenu = new JPopupMenu();
        
        // create popup menu for text area
        JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener((ActionEvent ev) ->
        {
            textArea.cut();       
        });
        menuItemCopy.addActionListener((ActionEvent ev) ->
        {
            textArea.copy();       
        });
        menuItemPaste.addActionListener((ActionEvent ev) ->
        {
            textArea.paste();       
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
        
        textArea.setMinimumSize(new Dimension(200, 50));
        textArea.setPreferredSize(textArea.getMinimumSize());
        panel.add(textArea);
        
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
        panel.add(buttonSetGraph);
        add(panel);
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