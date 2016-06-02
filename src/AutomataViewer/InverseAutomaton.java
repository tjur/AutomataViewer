
package AutomataViewer;

public class InverseAutomaton
{
    
    private int K, N;
    private int [][][] matrix; // matrix[state][transition] - array of states

    public InverseAutomaton(Automaton automaton)
    {
        K = automaton.getK();
        N = automaton.getN();
        matrix = new int[N][K][N];
        
        for (int n = 0; n < N; n++)
        {
            for (int k = 0; k < K; k++)
            {
                for (int m = 0; m < N; m++)
                    matrix[n][k][m] = (automaton.getMatrix()[m][k] == n) ? 1 : 0;              
            }
        }
    }
    
    public int[][][] getMatrix()
    {
        return matrix;
    }
}