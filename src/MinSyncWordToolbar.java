
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class MinSyncWordToolbar extends DockToolbar
{
    private final int MAX_STATES = 20; // max number of states in automaton
    private final JTextPane textPane;
    
    public MinSyncWordToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        JPanel panel = getPanel();
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Arial", Font.ITALIC + Font.BOLD, 22));
        panel.add(textPane, BorderLayout.CENTER);
    }
    
    private ArrayList<Integer> findMinSyncWord(int[] subset) throws MinSyncWordNotFoundException
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
        
        throw new MinSyncWordNotFoundException();
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
    
    private class MinSyncWordNotFoundException extends Exception {}
    
    private void insertLetterToTextPane(int trans)
    {
        String letter = AutomatonHelper.TRANSITIONS_LETTERS[trans];
        Color color = AutomatonHelper.TRANSITIONS_COLORS[trans];
        
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Style", null);
        StyleConstants.setForeground(style, color);

        try { 
            doc.insertString(doc.getLength(), letter, style);
            textPane.removeStyle("Style");
        }
        catch (BadLocationException e) {}
    }

    @Override
    protected void update()
    {
        if (getAutomaton().getN() > MAX_STATES)
        {
            textPane.setText(String.format("Automaton must have no more than %d states", MAX_STATES));
            return;
        }
        
        int[] subset = getAutomaton().getSelectedStates();
        try {
            ArrayList<Integer> transitions = findMinSyncWord(subset);
            textPane.setText("");
            for (int trans : transitions)
                insertLetterToTextPane(trans);
        }
        catch(MinSyncWordNotFoundException ex) {
            textPane.setText("");
            try {
                textPane.getDocument().insertString(0, "Word not found", null);
            } 
            catch (BadLocationException ex2) {}
        }
    }
}