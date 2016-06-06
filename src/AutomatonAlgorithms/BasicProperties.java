
package AutomatonAlgorithms;

import AutomataViewer.Automaton;
import AutomataViewer.InverseAutomaton;
import java.util.Arrays;


public abstract class BasicProperties
{
    
    public static boolean isIrreduciblySynchronizing(Automaton automaton, InverseAutomaton inverseAutomaton)
    {
        int K = automaton.getK();
        
        if (!isSynchronizing(automaton, inverseAutomaton))
            return false;
        
        for (int ommitLetter = 0; ommitLetter < K; ommitLetter++)
        {
            if (isSynchronizing(automaton, inverseAutomaton, ommitLetter))
                return false;
        }
        return true;
    }
    
    public static boolean isSynchronizing(Automaton automaton, InverseAutomaton inverseAutomaton)
    {
        return isSynchronizing(automaton, inverseAutomaton, -1);
    }
    
    private static boolean isSynchronizing(Automaton automaton, InverseAutomaton inverseAutomaton, int ommitLetter)
    {
        int N = automaton.getN();
        int K = automaton.getK();
        
        boolean[] visited = new boolean[N*(N-1)];
        Arrays.fill(visited, false);
        int[] stackN1 = new int[N*(N-1)/2];
        int[] stackN2 = new int[N*(N-1)/2];
        int top = 0;
        
        for (int n = 0; n < N; n++)
        {
            for (int k = 0; k < K; k++)
            {
                if (k == ommitLetter)
                    continue;
                
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
                if (k == ommitLetter)
                    continue;
                
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
    
    public static boolean isStronglyConnected(Automaton automaton, InverseAutomaton inverseAutomaton)
    {     
        if (automaton.getN() == 0)
            return false;
        
        boolean[] visited = new boolean[automaton.getN()];
        int vertex = 0; // choose arbitrary vertex
        
        dfs(vertex, visited, automaton);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        Arrays.fill(visited, false);
        dfsReversed(vertex, visited, automaton, inverseAutomaton);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        return true;
    }
    
    private static void dfs(int vertex, boolean[] visited, Automaton automaton)
    {
        visited[vertex] = true;
        int[][] matrix = automaton.getMatrix();
        for (int i = 0; i < automaton.getK(); i++)
        {
            if (!visited[matrix[vertex][i]])
                dfs(matrix[vertex][i], visited, automaton);
        }
    }
    
    private static void dfsReversed(int vertex, boolean[] visited, Automaton automaton, InverseAutomaton inverseAutomaton)
    {
        visited[vertex] = true;
        int[][][] reversedMatrix = inverseAutomaton.getMatrix();
        for (int i = 0; i < automaton.getK(); i++)
        {
            for (int j = 0; j < automaton.getN(); j++)
            {
                if (reversedMatrix[vertex][i][j] == 1 && !visited[j])
                    dfsReversed(j, visited, automaton, inverseAutomaton);
            }
        }
    }
    
    public static boolean isConnected(Automaton automaton)
    {
        if (automaton.getN() == 0)
            return false;
        
        int N = automaton.getN();
        int K = automaton.getK();
        
        boolean[] visited = new boolean[N];
        
        int[][] matrix = new int[N][N];
        int vertex = 0; // choose arbitrary vertex
        
        // make undirected graph
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < K; j++)
            {
                matrix[i][automaton.getMatrix()[i][j]] = 1;
                matrix[automaton.getMatrix()[i][j]][i] = 1;
            }
        }
        
        dfsUndirected(vertex, matrix, visited);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        return true;
    }
    
    private static void dfsUndirected(int vertex, int[][] matrix, boolean[] visited)
    {
        visited[vertex] = true;
        for (int i = 0; i < matrix[vertex].length; i++)
        {
            if (matrix[vertex][i] == 1 && !visited[i])
                dfsUndirected(i, matrix, visited);
        }
    }
}
