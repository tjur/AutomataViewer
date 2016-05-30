
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class BasicPropertiesToolbar extends DockToolbar
{
    private final JTextPane textPane;
    
    private ReversedAutomaton reversedAutomaton;
    private boolean[] visited;
    
    public BasicPropertiesToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        
        reversedAutomaton = new ReversedAutomaton(automaton);
        visited = new boolean[getAutomaton().getN()];
        
        JPanel panel = getPanel();
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Arial", Font.ITALIC + Font.BOLD, 14));
        textPane.setPreferredSize(new Dimension(0, 55));

        panel.add(textPane, BorderLayout.CENTER);
    }
    
    private boolean isStronglyConnected()
    {
        if (getAutomaton().getN() == 0)
            return false;
        
        int vertex = 0; // choose arbitrary vertex
        Arrays.fill(visited, false);
        
        dfs(vertex);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        Arrays.fill(visited, false);
        dfsReversed(vertex);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        return true;
    }
    
    private void dfs(int vertex)
    {
        visited[vertex] = true;
        int[][] matrix = getAutomaton().getMatrix();
        for (int i = 0; i < getAutomaton().getK(); i++)
        {
            if (!visited[matrix[vertex][i]])
                dfs(matrix[vertex][i]);
        }
    }
    
    private void dfsReversed(int vertex)
    {
        visited[vertex] = true;
        int[][][] reversedMatrix = reversedAutomaton.getMatrix();
        for (int i = 0; i < getAutomaton().getK(); i++)
        {
            for (int j = 0; j < getAutomaton().getN(); j++)
            {
                if (reversedMatrix[vertex][i][j] == 1 && !visited[j])
                    dfsReversed(j);
            }
        }
    }
    
    private boolean isConnected()
    {
        if (getAutomaton().getN() == 0)
            return false;
        
        int vertex = 0; // choose arbitrary vertex
        Arrays.fill(visited, false);
        
        int N = getAutomaton().getN();
        int K = getAutomaton().getK();
        int[][] matrix = new int[N][N];
        
        // make undirected graph
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < K; j++)
            {
                matrix[i][getAutomaton().getMatrix()[i][j]] = 1;
                matrix[getAutomaton().getMatrix()[i][j]][i] = 1;
            }
        }
        
        dfsUndirected(vertex, matrix);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        return true;
    }
    
    private void dfsUndirected(int vertex, int[][] matrix)
    {
        visited[vertex] = true;
        for (int i = 0; i < getAutomaton().getN(); i++)
        {
            if (matrix[vertex][i] == 1 && !visited[i])
                dfsUndirected(i, matrix);
        }
    }
    
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
        reversedAutomaton = new ReversedAutomaton(getAutomaton());
        visited = new boolean[getAutomaton().getN()];
        
        textPane.setText("");
        
        if (isStronglyConnected())
            insertStringToTextPane("Strongly connected", Color.BLACK);
        else if (isConnected())
            insertStringToTextPane("Connected", Color.BLACK);
        else
            insertStringToTextPane("Not connected", Color.BLACK);
    }
}