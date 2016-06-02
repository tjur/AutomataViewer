
package AutomataViewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BasicPropertiesToolbar extends DockToolbar
{
    private final JLabel syncLabel;
    private final JLabel connectedLabel;
    
    private InverseAutomaton inverseAutomaton;
    private boolean[] visited;
    
    public BasicPropertiesToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        
        inverseAutomaton = new InverseAutomaton(automaton);
        visited = new boolean[getAutomaton().getN()];
        
        JPanel panel = getPanel();
        
        syncLabel = new JLabel();
        connectedLabel = new JLabel();
        Font font = syncLabel.getFont().deriveFont((float) getDeafultFont().getSize());
        syncLabel.setFont(font);
        connectedLabel.setFont(font);
        
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
        labelPanel.add(syncLabel);
        labelPanel.add(connectedLabel);
        panel.add(labelPanel, BorderLayout.CENTER);
    }
    
    private boolean isSynchronizing()
    {
        int N = getAutomaton().getN();
        int K = getAutomaton().getK();
        
        visited = new boolean[N*(N-1)];
        Arrays.fill(visited, false);
        int[] stackN1 = new int[N*(N-1)/2];
        int[] stackN2 = new int[N*(N-1)/2];
        int top = 0;
        
        for (int n = 0; n < N; n++)
        {
            for (int k = 0; k < K; k++)
            {
                int[] states = inverseAutomaton.getMatrix()[n][k];
                for (int i1 = 0; i1 < states.length; i1++)
                {
                    for (int i2 = i1+1; i2 < states.length; i2++)
                    {
                        if (states[i1] == 1 && states[i2] == 1)
                        {
                            if (!visited[i1*N + i2]) 
                            {
                                visited[i1*N + i2] = true;
                                stackN1[top] = i1;
                                stackN2[top] = i2;
                                top++;
                            }
                        }
                    }
                }
            }
        }
        
        while (top > 0) 
        {
            top--;
            int q = stackN1[top];
            int p = stackN2[top];
            for (int k = 0; k < K; k++)
            {
                int[] states1 = inverseAutomaton.getMatrix()[q][k];
                int[] states2 = inverseAutomaton.getMatrix()[p][k];

                for (int i1 = 0; i1 < states1.length; i1++)
                {
                    for (int i2 = 0; i2 < states2.length; i2++) 
                    {
                        if (states1[i1] == 1 && states2[i2] == 1)
                        {
                            int i = i1;
                            int j = i2;
                            if (i1 > i2)
                            {
                                i = i2;
                                j = i1;
                            }

                            if (visited[i*N + j]) 
                                continue;
                            visited[i*N + j] = true;
                            stackN1[top] = i;
                            stackN2[top] = j;
                            top++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < N-1; i++)
        {
            for (int j = i+1; j < N; j++)
            {
              if (!visited[i*N + j])
                return false;
            }
        }
        return true;
    }
    
    private boolean isStronglyConnected()
    {
        if (getAutomaton().getN() == 0)
            return false;
        
        visited = new boolean[getAutomaton().getN()];
        int vertex = 0; // choose arbitrary vertex
        
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
        int[][][] reversedMatrix = inverseAutomaton.getMatrix();
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
        
        int N = getAutomaton().getN();
        int K = getAutomaton().getK();
        
        visited = new boolean[N];
        
        int[][] matrix = new int[N][N];
        int vertex = 0; // choose arbitrary vertex
        
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

    @Override
    protected void update()
    {
        inverseAutomaton = new InverseAutomaton(getAutomaton());
        visited = new boolean[getAutomaton().getN()];
        
        if (isSynchronizing())
            syncLabel.setText("Synchronizing");
        else
            syncLabel.setText("Not synchronizing");
            
        
        if (isStronglyConnected())
            connectedLabel.setText("Strongly connected");
        else if (isConnected())
            connectedLabel.setText("Connected");
        else
            connectedLabel.setText("Not connected");
    }
}