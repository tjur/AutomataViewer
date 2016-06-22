
package AutomatonAlgorithms;

import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;
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
        
        int[] queue = new int[N*(N-1)/2];
        Arrays.fill(queue, -1);
        int start = 0;
        int end = 0;
        
        for (int n = 0; n < N; n++)
        {
            for (int k = 0; k < K; k++)
            {
                if (k == ommitLetter)
                    continue;
                
                int[] states = inverseAutomaton.getMatrix()[n][k];
                for (int i1 = 0; i1 < states.length; i1++)
                {
                    for (int i2 = 0; i2 < states.length; i2++)
                    {
                        int a = states[i1];
                        int b = states[i2];
                        if (a >= b)
                            continue;
                        
                        if (!visited[a*N + b])
                        {
                            visited[a*N + b] = true;
                            queue[end] = a*N + b;
                            end++;
                        }
                    }
                }
            }
        }
        
        while (start < end) 
        {
            int q = queue[start] / N;
            int p = queue[start] % N;
            start++;
            
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
                        int a = states1[i1];
                        int b = states2[i2];
                        if (a > b)
                        {
                            a = states2[i2];
                            b = states1[i1];
                        }

                        if (visited[a*N + b])
                            continue;

                        visited[a*N + b] = true;
                        queue[end] = a*N + b;
                        end++;
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
        for (int k = 0; k < automaton.getK(); k++)
        {
            if (!visited[matrix[vertex][k]])
                dfs(matrix[vertex][k], visited, automaton);
        }
    }
    
    private static void dfsReversed(int vertex, boolean[] visited, Automaton automaton, InverseAutomaton inverseAutomaton)
    {
        visited[vertex] = true;
        int[][][] reversedMatrix = inverseAutomaton.getMatrix();
        for (int k = 0; k < automaton.getK(); k++)
        {
            for (int m = 0; m < reversedMatrix[vertex][k].length; m++)
            {
                if (!visited[reversedMatrix[vertex][k][m]])
                    dfsReversed(reversedMatrix[vertex][k][m], visited, automaton, inverseAutomaton);
            }
        }
    }
    
    public static boolean isConnected(Automaton automaton, InverseAutomaton inverseAutomaton)
    {
        if (automaton.getN() == 0)
            return false;
        
        int N = automaton.getN();
        int K = automaton.getK();
        
        boolean[] visited = new boolean[N];
        
        int vertex = 0; // choose arbitrary vertex
        dfsUndirected(vertex, visited, automaton, inverseAutomaton);
        for (boolean val : visited)
        {
            if (!val)
                return false;
        }
        
        return true;
    }
    
    private static void dfsUndirected(int vertex, boolean[] visited, Automaton automaton, InverseAutomaton inverseAutomaton)
    {
        visited[vertex] = true;
        for (int k = 0; k < automaton.getK(); k++)
        {
            if (!visited[automaton.getMatrix()[vertex][k]])
                dfsUndirected(automaton.getMatrix()[vertex][k], visited, automaton, inverseAutomaton);
        }
        int[][][] reversedMatrix = inverseAutomaton.getMatrix();
        for (int k = 0; k < automaton.getK(); k++)
        {
            for (int m = 0; m < reversedMatrix[vertex][k].length; m++)
            {
                if (!visited[reversedMatrix[vertex][k][m]])
                    dfsUndirected(reversedMatrix[vertex][k][m], visited, automaton, inverseAutomaton);
            }
        }
    }
}
