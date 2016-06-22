
package Viewer;

import AutomatonAlgorithms.BasicProperties;
import AutomatonModels.Automaton;
import AutomatonModels.InverseAutomaton;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BasicPropertiesToolbar extends DockToolbar
{
    private final JLabel syncLabel;
    private final JLabel connectedLabel;
    
    private InverseAutomaton inverseAutomaton;
    
    public BasicPropertiesToolbar(String name, boolean visibleOnStart, Automaton automaton)
    {
        super(name, visibleOnStart, automaton);
        
        inverseAutomaton = new InverseAutomaton(automaton);
        
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

    @Override
    protected void update()
    {
        inverseAutomaton = new InverseAutomaton(getAutomaton());
        
        if (BasicProperties.isIrreduciblySynchronizing(getAutomaton(), inverseAutomaton))
            syncLabel.setText("Irreducibly synchronizing");
        else if (BasicProperties.isSynchronizing(getAutomaton(), inverseAutomaton))
            syncLabel.setText("Synchronizing");
        else
            syncLabel.setText("Not synchronizing");
            
        if (BasicProperties.isStronglyConnected(getAutomaton(), inverseAutomaton))
            connectedLabel.setText("Strongly connected");
        else if (BasicProperties.isConnected(getAutomaton(), inverseAutomaton))
            connectedLabel.setText("Connected");
        else
            connectedLabel.setText("Not connected");
    }
}
