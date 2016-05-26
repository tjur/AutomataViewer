
public class ReversedAutomaton
{
    
    private int K, N;
    private boolean [][][] matrix; // matrix[state][transition] - array of states

    public ReversedAutomaton(Automaton automaton)
    {
        K = automaton.getK();
        N = automaton.getN();
        matrix = new boolean[N][K][N];
        
        for (int n = 0; n < N; n++)
        {
            for (int k = 0; k < K; k++)
            {
                for (int m = 0; m < N; m++)
                    matrix[n][k][m] = (automaton.getMatrix()[m][k] == n);
            }
        }
    }
}