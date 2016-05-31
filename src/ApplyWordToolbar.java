
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;


public class ApplyWordToolbar extends DockToolbar
{
    private JTextPane textPane;
    private ReversedAutomaton reversedAutomaton;
    private final HashMap<Character, Integer> hashMap;
    
    public ApplyWordToolbar(String name, Automaton automaton)
    {
        super(name, automaton);
        reversedAutomaton = new ReversedAutomaton(automaton);
        
        hashMap = new HashMap<>();
        for (int i = 0; i < automaton.getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);
        
        JPanel panel = getPanel();
        textPane = new JTextPane();
        textPane.setFont(getDeafultFont());
        textPane.setPreferredSize(new Dimension(0, 60));
        
        // create popup menu for text pane
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemCut, menuItemCopy, menuItemPaste;
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.cut();
            }
        });
        menuItemCopy.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.copy();
            }
        });
        menuItemPaste.addActionListener(new ActionListener()
        {
       
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                textPane.paste();
            }
        });

        popupMenu.add(menuItemCut);
        popupMenu.add(menuItemCopy);
        popupMenu.add(menuItemPaste);
        
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
        
        JButton imageButton = new JButton("Image");
        imageButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String word = textPane.getText().replaceAll("\\s+","");
                if (check(word))
                apply(word);
                else
                    JOptionPane.showMessageDialog(textPane, "Invalid word");
            }       
        });
        JButton preimageButton = new JButton("Preimage");
        preimageButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String word = textPane.getText().replaceAll("\\s+","");
                if (check(word))
                    applyReversed(word);
                else
                    JOptionPane.showMessageDialog(textPane, "Invalid word");
            }       
        });
        
        imageButton.setMaximumSize(preimageButton.getPreferredSize());
        
        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));
        borderPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        borderPanel.add(imageButton);
        borderPanel.add(new Separator());
        borderPanel.add(preimageButton);
        panel.add(borderPanel, BorderLayout.EAST);
    }
    
    private boolean check(String word)
    {
        for (char letter : word.toCharArray())
        {
            if (!hashMap.containsKey(letter))
                return false;
        }
        
        return true;
    }
    
    private void apply(String word)
    {
        int[] subset = getAutomaton().getSelectedStates();
        int N = getAutomaton().getN();
        for (char letter : word.toCharArray())
        {
            int[] newSubset = new int[N];
            for (int i = 0; i < N; i++)
            {
                if (subset[i] == 1)
                    newSubset[getAutomaton().getMatrix()[i][hashMap.get(letter)]] = 1;
            }
            subset = newSubset;
        }
        
        getAutomaton().selectStates(subset);
    }
    
    private void applyReversed(String word)
    {
        int[] subset = getAutomaton().getSelectedStates();
        int N = getAutomaton().getN();
        for (char letter : word.toCharArray())
        {
            int[] newSubset = new int[N];
            for (int i = 0; i < N; i++)
            {
                if (subset[i] == 1)
                {
                    int[] subset2 = reversedAutomaton.getMatrix()[i][hashMap.get(letter)];
                    for (int j = 0; j < N; j++)
                    {
                        if (subset2[j] == 1)
                            newSubset[j] = 1;
                    }
                }
            }
            subset = newSubset;
        }
        getAutomaton().selectStates(subset);
    }

    @Override
    protected void update() 
    {
        reversedAutomaton = new ReversedAutomaton(getAutomaton());
        hashMap.clear();
        for (int i = 0; i < getAutomaton().getK(); i++)
            hashMap.put(AutomatonHelper.TRANSITIONS_LETTERS[i], i);
    }
}