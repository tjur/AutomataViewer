
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class ShortestResetWordToolbar extends DockToolbar
{
    private final int MAX_STATES = 20; // max number of states in automaton
    private final JTextPane textPane;
    
    public ShortestResetWordToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        JPanel panel = getPanel();
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Arial", Font.ITALIC + Font.BOLD, 22));
        textPane.setPreferredSize(new Dimension(0, 75));
        
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemCopy;
        menuItemCopy = new JMenuItem("Copy");
        menuItemCopy.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.copy();
            }
        });
        popupMenu.add(menuItemCopy);
        
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
    }
    
    private ArrayList<Integer> findShortestResetWord(int[] subset) throws WordNotFoundException
    {
        boolean[] visited = new boolean[(int) Math.pow(2, getAutomaton().getN())];
        Pair[] fromWhere = new Pair[visited.length];
        Arrays.fill(visited, false);
        Arrays.fill(fromWhere, null);
        
        Queue<Integer> Q = new LinkedList<>();
        Q.add(subsetToValue(subset));
        while (!Q.isEmpty())
        {
            int subsetValue = Q.poll();    
            visited[subsetValue] = true;
            
            if (Integer.bitCount(subsetValue) == 1) // singleton is a power of two
            {
                ArrayList<Integer> transitions = new ArrayList<>();
                while (fromWhere[subsetValue] != null)
                {
                    transitions.add(fromWhere[subsetValue].transition);
                    subsetValue = fromWhere[subsetValue].subsetValue;
                }
                
                Collections.reverse(transitions);
                return transitions;
            }
            else
            {
                subset = valueToSubset(subsetValue);
                for (int trans = 0; trans < getAutomaton().getK(); trans++)
                {
                    int[] newSubset = new int[getAutomaton().getN()];
                    Arrays.fill(newSubset, 0);
                    for (int i = 0; i < subset.length; i++)
                    {
                        if (subset[subset.length - 1 - i] == 1)
                            newSubset[subset.length - 1 - getAutomaton().getMatrix()[i][trans]] = 1;
                    }
                    
                    int newSubsetValue = subsetToValue(newSubset);
                    if (!visited[newSubsetValue])
                    {
                        fromWhere[newSubsetValue] = new Pair(subsetValue, trans);
                        Q.add(newSubsetValue);
                    }
                }
            }
        }
        
        throw new WordNotFoundException();
    }
    
    private int subsetToValue(int[] subset)
    {
        int value = 0;
        for (int i = 0; i < subset.length; i++)
            value = 2 * value + subset[i];
        
        return value;
    }
    
    private int[] valueToSubset(int value)
    {
        int[] subset = new int[getAutomaton().getN()];
        Arrays.fill(subset, 0);
        for (int i = 0; i < subset.length; i++)
        {
            subset[subset.length - 1 - i] = (int) (value % 2);
            value /= 2;
            if (value == 0)
                break;
        }
        
        return subset;
    }
    
    private class Pair
    {
        public int subsetValue;
        public int transition;
        
        public Pair(int subsetValue, int transition)
        {
            this.subsetValue = subsetValue;
            this.transition = transition;
        }
    }
    
    private class WordNotFoundException extends Exception {}
    
    private void insertStringToTextPane(String text, Color color)
    {
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Style", null);
        StyleConstants.setForeground(style, color);

        try { 
            doc.insertString(doc.getLength(), text, style);
            textPane.removeStyle("Style");
        }
        catch (BadLocationException e) {}
    }

    @Override
    protected void update()
    {
        if (getAutomaton().getN() > MAX_STATES)
        {
            textPane.setText("");
            insertStringToTextPane(String.format("Automaton must have no more than %d states", MAX_STATES), Color.BLACK);
            return;
        }
        
        int[] subset = getAutomaton().getSelectedStates();
        try {
            ArrayList<Integer> transitions = findShortestResetWord(subset);
            textPane.setText("");
            for (int trans : transitions)
            {
                String letter = AutomatonHelper.TRANSITIONS_LETTERS[trans];
                Color color = AutomatonHelper.TRANSITIONS_COLORS[trans];
                insertStringToTextPane(letter, color);
            }
            insertStringToTextPane(String.format("%nLength: %d", transitions.size()), Color.BLACK);
        }
        catch(WordNotFoundException ex) {
            textPane.setText("");
            insertStringToTextPane("Word not found", Color.BLACK);
        }
    }
}
