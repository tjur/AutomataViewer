
package AutomatonAlgorithms;

import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class ShortestCompressingWord
{
  
    public static ArrayList<Integer> find(Automaton automaton, InverseAutomaton inverseAutomaton, int[] subset) throws WordNotFoundException
    {
        int N = automaton.getN();
        int K = automaton.getK();
        
        boolean[] visited = new boolean[N*(N-1)];
        int[] fromWherePair = new int[N*(N-1)];
        int[] fromWhereTransition = new int[N*(N-1)];
        Arrays.fill(visited, false);
        Arrays.fill(fromWherePair, -1);
        Arrays.fill(fromWhereTransition, -1);
        
        int[] queue = new int[N*(N-1)/2];
        Arrays.fill(queue, -1);
        int start = 0;
        int end = 0;
        
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
                                if (subset[i1] == 1 && subset[i2] == 1)
                                {
                                    ArrayList<Integer> transitions = new ArrayList<>();
                                    transitions.add(k);
                                    return transitions;
                                }
                                
                                visited[i1*N + i2] = true;
                                fromWhereTransition[i1*N + i2] = k;
                                queue[end] = i1*N + i2;
                                end++;
                            }
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
                            fromWherePair[i*N + j] = q*N + p;
                            fromWhereTransition[i*N + j] = k;
                            queue[end] = i*N + j;
                            end++;
                            
                            if (subset[i1] == 1 && subset[i2] == 1)
                            {
                                int pair = i*N + j;
                                ArrayList<Integer> transitions = new ArrayList<>();
                                while (true)
                                {
                                    transitions.add(fromWhereTransition[pair]);
                                    pair = fromWherePair[pair];
                                    
                                    if (pair == -1)
                                        break;
                                }

                                return transitions;
                            }
                        }
                    }
                }
            }
        }

        throw new WordNotFoundException();
    }
}
