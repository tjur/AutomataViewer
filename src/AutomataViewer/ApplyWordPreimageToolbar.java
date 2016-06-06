
package AutomataViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;


public class ApplyWordPreimageToolbar extends DockToolbar
{
    private JTextPane textPane;
    private InverseAutomaton inverseAutomaton;
    private final HashMap<Character, Integer> hashMap;
    
    public ApplyWordPreimageToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        inverseAutomaton = new InverseAutomaton(automaton);
        
        hashMap = new HashMap<>();
        for (int i = 0; i < automaton.getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);
        
        JPanel panel = getPanel();
        
        StyleContext cont = StyleContext.getDefaultStyleContext();
        AttributeSet attrBlack = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, Color.BLACK);
        AttributeSet attrGray = cont.addAttribute(cont.getEmptySet(), StyleConstants.Background, Color.LIGHT_GRAY);
        AttributeSet attrDefault = cont.getStyle(StyleContext.DEFAULT_STYLE);
        DefaultStyledDocument doc = new DefaultStyledDocument() {
            
            @Override
            public void insertString (int offset, String str, AttributeSet a)
            {
                try {
                    super.insertString(offset, str, a);
                } 
                catch (BadLocationException ex) {}
                
                for (int i = 0; i < str.length(); i++)
                {
                    char letter = str.charAt(i);
                    
                    if (hashMap.containsKey(letter))
                    {
                        AttributeSet attr = cont.addAttribute(cont.getEmptySet(), StyleConstants.Foreground, AutomatonHelper.TRANSITIONS_COLORS[hashMap.get(letter)]);
                        setCharacterAttributes(offset + i, 1, attr, true);
                    }
                    else if (!Character.isWhitespace(letter))
                        setCharacterAttributes(offset + i, 1, attrGray, true);
                    else
                        setCharacterAttributes(offset + i, 1, attrDefault, true);
                }
            }
            
            @Override
            public void remove (int offset, int len) throws BadLocationException 
            {
                setCharacterAttributes(offset, len, attrDefault, true);
                super.remove(offset, len);
            }
        };  
        
        textPane = new JTextPane(doc);
        textPane.setFont(getDeafultFont());
        textPane.setPreferredSize(new Dimension(0, 60));
        
        // create popup menu for text pane
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
        
        JButton preimageButton = new JButton("Preimage");
        preimageButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String word = textPane.getText().replaceAll("\\s+","");
                if (check(word))
                    applyReversed(word);
                else
                    JOptionPane.showMessageDialog(textPane, "Invalid word");
            }       
        });
        
        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.X_AXIS));
        borderPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        borderPanel.add(preimageButton);
        panel.add(borderPanel, BorderLayout.SOUTH);
    }
    
    private boolean check(String word)
    {
        for (char letter : word.toCharArray())
        {
            if (!hashMap.containsKey(letter))
                return false;
        }
        
        return true;
    }
    
    private void applyReversed(String word)
    {
        int[] subset = getAutomaton().getSelectedStates();
        int N = getAutomaton().getN();
        for (char letter : word.toCharArray())
        {
            int[] newSubset = new int[N];
            for (int i = 0; i < N; i++)
            {
                if (subset[i] == 1)
                {
                    int[] subset2 = inverseAutomaton.getMatrix()[i][hashMap.get(letter)];
                    for (int j = 0; j < N; j++)
                    {
                        if (subset2[j] == 1)
                            newSubset[j] = 1;
                    }
                }
            }
            subset = newSubset;
        }
        getAutomaton().selectStates(subset);
    }

    @Override
    protected void update() 
    {
        inverseAutomaton = new InverseAutomaton(getAutomaton());
        hashMap.clear();
        for (int i = 0; i < getAutomaton().getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);
        
        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE), true);
        textPane.setText(textPane.getText());
    }
}