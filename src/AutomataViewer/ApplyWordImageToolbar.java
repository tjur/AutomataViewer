
package AutomataViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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


public class ApplyWordImageToolbar extends DockToolbar
{
    private JTextPane textPane;
    private final HashMap<Character, Integer> hashMap;
    private JCheckBox rangeCheckBox;
    private JCheckBox actionCheckBox;
    
    public ApplyWordImageToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        
        hashMap = new HashMap<>();
        for (int i = 0; i < automaton.getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);
        
        JPanel panel = getPanel();
        
        StyleContext cont = StyleContext.getDefaultStyleContext();
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
                
                showRange();
                showAction();
            }
            
            @Override
            public void remove (int offset, int len) throws BadLocationException 
            {
                setCharacterAttributes(offset, len, attrDefault, true);
                super.remove(offset, len);
                
                showRange();
                showAction();
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
        
        JButton imageButton = new JButton("Image");
        imageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String word = textPane.getText().replaceAll("\\s+","");
                if (check(word))
                    apply(word);
                else
                    JOptionPane.showMessageDialog(textPane, "Invalid word");
            }       
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.add(imageButton);
        
        rangeCheckBox = new JCheckBox("Range");
        rangeCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev)
            {
                showRange();
                if (!rangeCheckBox.isSelected())
                    firePropertyChange("showRange", false, true);
            }
        });
        
        actionCheckBox = new JCheckBox("Action");
        actionCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ev)
            {
                showAction();
                if (!actionCheckBox.isSelected())
                    firePropertyChange("showAction", false, true);
            }
        });
        
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.gridwidth = 1;
        outerPanel.add(buttonPanel, c);
        outerPanel.add(rangeCheckBox, c);
        c.gridwidth = GridBagConstraints.REMAINDER; 
        outerPanel.add(actionCheckBox, c);
        panel.add(outerPanel, BorderLayout.SOUTH);
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
    
    private void apply(String word)
    {
        getAutomaton().selectStates(getSubset(word, getAutomaton().getSelectedStates()));
    }
    
    private int[] getSubset(String word, int[] subset)
    {
        int N = getAutomaton().getN();
        for (char letter : word.toCharArray())
        {
            int[] newSubset = new int[N];
            for (int i = 0; i < N; i++)
            {
                if (subset[i] == 1)
                    newSubset[getAutomaton().getMatrix()[i][hashMap.get(letter)]] = 1;
            }
            subset = newSubset;
        }
        
        return subset;
    }
    
    private HashMap<Integer, ArrayList<Integer>> getActions(String word)
    {
        int[] subset = getAutomaton().getSelectedStates();
        HashMap<Integer, ArrayList<Integer>> actions = new HashMap<>();
        for (int i = 0; i < subset.length; i++)
        {
            if (subset[i] == 1)
            {
                int[] state = new int[getAutomaton().getN()];
                state[i] = 1;
                int[] states = getSubset(word, state);
                ArrayList<Integer> statesList = new ArrayList<>();
                for (int j = 0; j < states.length; j++)
                {
                    if (states[j] == 1)
                        statesList.add(j);
                }
                actions.put(i, statesList);
            }
        }
        return actions;
    }
    
    private void showRange()
    {
        if (rangeCheckBox.isSelected())
        {
            String word = textPane.getText().replaceAll("\\s+","");
            if (check(word))
                firePropertyChange("showRange", null, getSubset(word, getAutomaton().getSelectedStates()));
            else
                firePropertyChange("showRange", null, new int[getAutomaton().getN()]);
        }
    }
    
    private void showAction()
    {
        if (actionCheckBox.isSelected())
        {
            String word = textPane.getText().replaceAll("\\s+","");
            if (check(word))
                firePropertyChange("showAction", null, getActions(word));
            else
                firePropertyChange("showAction", null, new HashMap<>());
        }
    }
    
    public void rangeCheckBoxSetSelected(boolean b)
    {
        rangeCheckBox.setSelected(b);
    }
    
    public void actionCheckBoxSetSelected(boolean b)
    {
        actionCheckBox.setSelected(b);
    }

    @Override
    protected void update() 
    {
        hashMap.clear();
        for (int i = 0; i < getAutomaton().getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);
        
        StyledDocument doc = textPane.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE), true);
        textPane.setText(textPane.getText());
        
        showRange();
        showAction();
    }
}
