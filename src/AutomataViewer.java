import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.JToolBar;


public class AutomataViewer implements MouseListener 
{

    public static void main(String[] args) 
    {
        JFrame frameMain = new JFrame("Automata viewer");
        new AutomataViewer(frameMain);
        frameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frameMain.setSize(900,675);
        frameMain.setMinimumSize(new Dimension(600, 450));
        frameMain.setVisible(true);
    }

    private JFrame frameMain;
    private AutomatonPanel automatonPanel;
    private JPanel panelControls;
    private JTextArea textAreaMatrix;
    private JButton buttonSetGraph;

    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem saveMenuItem;
    private JFileChooser fileChooser;
    
    private JToolBar toolBar;
    private final String[] iconFiles = { "add_state.png", "remove_state.png", "add_transition.png", "change_color.png" };
    private final String [] buttonLabels = { "Add state", "Remove state", "Add transition", "Change color" };
    private final JButton [] toolBarButtons = new JButton[buttonLabels.length];
    
    private JButton chooseColorButton;
    private JComboBox<String> transitions;

    private JPopupMenu popupMenuMatrix;
    private JMenuItem menuItemCut, menuItemCopy, menuItemPaste;

    public AutomataViewer(JFrame frameMain) 
    {
        this.frameMain = frameMain;
        automatonPanel = new AutomatonPanel();
        
        frameMain.addComponentListener(new ComponentAdapter() {
            
            @Override
            public void componentResized(ComponentEvent e) {
                automatonPanel.repaintGraph();
            }
        });
        
        automatonPanel.addPropertyChangeListener("update", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                if(automatonPanel.getOperation() == AutomatonPanel.Operation.ADD_STATE.getValue() ||
                   automatonPanel.getOperation() == AutomatonPanel.Operation.REMOVE_STATE.getValue())
                {
                    textAreaMatrix.setText(automatonPanel.getAutomatonString());
                }
                else if (automatonPanel.getOperation() == AutomatonPanel.Operation.ADD_TRANS.getValue())
                {
                    textAreaMatrix.setText(automatonPanel.getAutomatonString());
                    int K = automatonPanel.getAutomatonK();
                    if(transitions.getItemCount() == K && transitions.getSelectedIndex() == K - 1)
                    {
                        transitions.removeItemAt(K - 1);
                        transitions.addItem(Integer.toString(K - 1));
                        transitions.addItem("Create new transition");
                        transitions.setSelectedIndex(K - 1);
                    }
                }    
            }
        });

        panelControls = new JPanel();
        panelControls.setLayout(new FlowLayout());
        
        // create text area
        textAreaMatrix = new JTextArea();
        textAreaMatrix.addMouseListener(this);
        textAreaMatrix.setMinimumSize(new Dimension(500,50));
        textAreaMatrix.setPreferredSize(textAreaMatrix.getMinimumSize());
        textAreaMatrix.setText("2 4 1 0 3 0 0 1 1 2");
        panelControls.add(textAreaMatrix);
        
        buttonSetGraph = new JButton("OK");
        buttonSetGraph.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                String matrix = textAreaMatrix.getText().trim();
                try {
                    if (!automatonPanel.automatonIsNull() && automatonPanel.getAutomatonString().equals(matrix))
                        automatonPanel.repaintGraph();
                    else
                    {
                        Automaton automaton = new Automaton(matrix);
                        automatonPanel.setGraph(automaton);

                        transitions.removeAllItems();
                        int K = automatonPanel.getAutomatonK();
                        for (int i = 0; i < K; i++)
                            transitions.addItem(Integer.toString(i));
                        transitions.addItem("Create new transition");
                    }
                } 
                catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(frameMain, e.toString());
                }
            }       
        });
        panelControls.add(buttonSetGraph);
        
        // create popup menu for text area
        menuItemCut = new JMenuItem("Cut");
        menuItemCopy = new JMenuItem("Copy");
        menuItemPaste = new JMenuItem("Paste");
        menuItemCut.addActionListener((ActionEvent ev) ->
        {
            textAreaMatrix.cut();       
        });
        menuItemCopy.addActionListener((ActionEvent ev) ->
        {
            textAreaMatrix.copy();       
        });
        menuItemPaste.addActionListener((ActionEvent ev) ->
        {
            textAreaMatrix.paste();       
        });
        
        popupMenuMatrix = new JPopupMenu();
        popupMenuMatrix.add(menuItemCut);
        popupMenuMatrix.add(menuItemCopy);
        popupMenuMatrix.add(menuItemPaste);

        createMenuBar();
        createToolBar();

        Container container = frameMain.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(toolBar, BorderLayout.NORTH);
        container.add(automatonPanel, BorderLayout.CENTER);
        container.add(panelControls, BorderLayout.SOUTH);
    }

    private void createMenuBar()
    {
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        
        fileChooser = new JFileChooser();

        saveMenuItem = new JMenuItem("Save Image... ");
        fileMenu.add(saveMenuItem);
        saveMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ev)
            {
                int option = fileChooser.showSaveDialog(fileMenu);
                if (option == JFileChooser.APPROVE_OPTION)
                {
                    File file = fileChooser.getSelectedFile();
                    String path = file.getPath();
                    String name = file.getName();
                    
                    BufferedImage img = new BufferedImage(automatonPanel.getWidth(),automatonPanel.getHeight(),BufferedImage.TYPE_INT_RGB);
                    Graphics g = img.getGraphics();
                    automatonPanel.paint(g);
                    g.dispose();
                    try {
                        if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0)
                        {
                            String ext = name.substring(name.lastIndexOf(".") + 1);
                            if (!ext.equals("png"))
                                JOptionPane.showMessageDialog(frameMain, "Invalid file extension (.png expected)");
                            else
                                ImageIO.write(img, ext, fileChooser.getSelectedFile());
                        }
                        else
                            ImageIO.write(img, "png", new File(path + ".png"));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }   
        });

        frameMain.setJMenuBar(menuBar);
    }
    
    private void createToolBar()
    {
        toolBar = new JToolBar("Tool Bar");
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.GRAY);
        ActionListener actionListener = new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                Color noBackground = new JButton().getBackground();
                if(ev.getSource() == chooseColorButton)
                {
                    Color selectedColor = JColorChooser.showDialog(null, "Choose color", automatonPanel.getSelectedColor());
                    selectedColor = (selectedColor == null) ? automatonPanel.getSelectedColor() : selectedColor;
                    automatonPanel.setSelectedColor(selectedColor);
                    chooseColorButton.setIcon(createIcon(automatonPanel.getSelectedColor(), 25, 25));
                }
                else if (ev.getSource() == transitions)
                {
                    automatonPanel.setSelectedTransition(transitions.getSelectedIndex());
                    
                }
                else
                {
                    for (int i = 0; i < AutomatonPanel.Operation.NONE.getValue(); i++)
                    {
                        if (ev.getSource() == toolBarButtons[i])
                        {
                            if(automatonPanel.getOperation() == i)
                            {   
                                automatonPanel.setOperation(AutomatonPanel.Operation.NONE);
                                toolBarButtons[i].setBackground(noBackground);
                            }
                            else
                            {
                                if(automatonPanel.getOperation() != AutomatonPanel.Operation.NONE.getValue())
                                    toolBarButtons[automatonPanel.getOperation()].setBackground(noBackground);
                                
                                automatonPanel.setOperation(AutomatonPanel.Operation.values()[i]);
                                toolBarButtons[i].setBackground(Color.CYAN);
                            }
                            
                            if(automatonPanel.getOperation() != AutomatonPanel.Operation.CHANGE_COLOR.getValue())
                                chooseColorButton.setVisible(false);
                            else
                                chooseColorButton.setVisible(true);
                            
                            if(automatonPanel.getOperation() != AutomatonPanel.Operation.ADD_TRANS.getValue())
                                transitions.setVisible(false);
                            else
                                transitions.setVisible(true);
                            
                            break;
                        }
                    }
                }
            }
        };
        
        toolBar.addSeparator(new Dimension(0, 50));
        for (int i = 0; i < buttonLabels.length; i++) 
        {
            ImageIcon icon = new ImageIcon("icons/" + iconFiles[i]);
            Image image = icon.getImage();  
            Image newimage = image.getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH);  
            icon = new ImageIcon(newimage);
            toolBarButtons[i] = new JButton(icon);
            toolBarButtons[i].setToolTipText(buttonLabels[i]);
            toolBarButtons[i].addActionListener(actionListener);
            if (i != 0)
                toolBar.addSeparator();
            toolBar.add(toolBarButtons[i]);
        }
        
        toolBar.add(Box.createHorizontalGlue());
        chooseColorButton = new JButton(createIcon(automatonPanel.getSelectedColor(), 25, 25));
        chooseColorButton.setToolTipText("Choose color");
        chooseColorButton.addActionListener(actionListener);
        chooseColorButton.setVisible(false);
        toolBar.add(chooseColorButton);
        
        transitions = new JComboBox<>();
        transitions.setMaximumSize(new Dimension(100,30));
        transitions.addActionListener(actionListener);
        transitions.setPrototypeDisplayValue("Create new transition");
        transitions.setVisible(false);
        toolBar.add(transitions);
        toolBar.addSeparator();
    }
    
    private ImageIcon createIcon(Color color, int width, int height) 
    {
        BufferedImage image = new BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width - 1, height - 1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }

    @Override 
    public void mousePressed(MouseEvent ev) 
    {
        if (ev.isPopupTrigger()) 
            popupMenuMatrix.show(ev.getComponent(), ev.getX(), ev.getY());
    }

    @Override 
    public void mouseReleased(MouseEvent ev) 
    {
        if (ev.isPopupTrigger()) 
            popupMenuMatrix.show(ev.getComponent(), ev.getX(), ev.getY());
    }

    @Override 
    public void mouseClicked(MouseEvent ev) {}
    
    @Override 
    public void mouseEntered(MouseEvent e) {}
    
    @Override 
    public void mouseExited(MouseEvent e) {}
}
