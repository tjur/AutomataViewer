
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;


public class AutomataViewer
{

    public static void main(String[] args) 
    {
        JFrame frame = new JFrame("Automata viewer");
        AutomataViewer automataViewer = new AutomataViewer(frame);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1300,750);
        frame.setMinimumSize(new Dimension(700, 525));
        frame.setVisible(true);
    }

    private final JFrame frame;
    
    private SplitPane desktop;
    private PaintPanel paintPanel;
    
    private JToolBar toolBar;
    private final String[] iconFiles = { "icons/add_state.png", "icons/remove_state.png", "icons/add_transition.png", "icons/change_color.png" };
    private final String [] buttonLabels = { "Add state", "Remove state", "Add transition", "Change color" };
    private final JButton [] toolBarButtons = new JButton[buttonLabels.length];
    
    private JPanel selectedColorPanel;
    private JPanel colorChoosersPanel;
    private JComboBox<String> transitions;

    public AutomataViewer(JFrame frame) 
    {
        this.frame = frame;
        desktop = new SplitPane();
        paintPanel = desktop.getPaintPanel();
        
        paintPanel.addPropertyChangeListener("updateAutomaton", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                desktop.getTextPanel().getTextArea().setText(desktop.getAutomatonString());
                
                if (paintPanel.getOperation() == PaintPanel.Operation.ADD_TRANS.getValue())
                {
                    int K = desktop.getAutomatonK();
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
        
        desktop.getTextPanel().addPropertyChangeListener("updateTransitions", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                transitions.removeAllItems();
                int K = desktop.getAutomatonK();
                for (int i = 0; i < K; i++)
                    transitions.addItem(Integer.toString(i));
                transitions.addItem("Create new transition");
            }
        });
        
        createMenuBar();
        createToolBar();

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(toolBar, BorderLayout.NORTH);
        container.add(desktop, BorderLayout.CENTER);
    }

    private void createMenuBar()
    {
        JMenuBar menuBar;
        JMenu fileMenu;
        JMenu toolbarsMenu;
        JMenuItem saveMenuItem;
        JFileChooser fileChooser;
        
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        toolbarsMenu = new JMenu("Toolbars");
        menuBar.add(fileMenu);
        menuBar.add(toolbarsMenu);
        
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
                    
                    BufferedImage img = new BufferedImage(paintPanel.getWidth(),paintPanel.getHeight(),BufferedImage.TYPE_INT_RGB);
                    Graphics g = img.getGraphics();
                    paintPanel.paint(g);
                    g.dispose();
                    try {
                        if (name.lastIndexOf(".") != -1 && name.lastIndexOf(".") != 0)
                        {
                            String ext = name.substring(name.lastIndexOf(".") + 1);
                            if (!ext.equals("png"))
                                JOptionPane.showMessageDialog(frame, "Invalid file extension (.png expected)");
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

        frame.setJMenuBar(menuBar);
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
                if (ev.getSource() == transitions)
                    paintPanel.setSelectedTransition(transitions.getSelectedIndex());
                else
                {
                    for (int i = 0; i < PaintPanel.Operation.NONE.getValue(); i++)
                    {
                        if (ev.getSource() == toolBarButtons[i])
                        {
                            if(paintPanel.getOperation() == i)
                            {   
                                paintPanel.setOperation(PaintPanel.Operation.NONE);
                                toolBarButtons[i].setBackground(noBackground);
                            }
                            else
                            {
                                if(paintPanel.getOperation() != PaintPanel.Operation.NONE.getValue())
                                    toolBarButtons[paintPanel.getOperation()].setBackground(noBackground);
                                
                                paintPanel.setOperation(PaintPanel.Operation.values()[i]);
                                toolBarButtons[i].setBackground(Color.CYAN);
                            }
                            
                            if(paintPanel.getOperation() != PaintPanel.Operation.CHANGE_COLOR.getValue())
                            {
                                selectedColorPanel.setVisible(false);
                                colorChoosersPanel.setVisible(false);
                            }
                            else
                            {
                                selectedColorPanel.setVisible(true);
                                colorChoosersPanel.setVisible(true);
                            }
                            
                            if(paintPanel.getOperation() != PaintPanel.Operation.ADD_TRANS.getValue())
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
            ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource(iconFiles[i]));
            Image image = icon.getImage();  
            Image newimage = image.getScaledInstance(35, 35, java.awt.Image.SCALE_SMOOTH);  
            icon = new ImageIcon(newimage);
            toolBarButtons[i] = new JButton(icon);
            toolBarButtons[i].setToolTipText(buttonLabels[i]);
            toolBarButtons[i].addActionListener(actionListener);
            if (i != 0)
                toolBar.addSeparator();
            toolBar.add(toolBarButtons[i]);
        }
        
        toolBar.add(Box.createHorizontalGlue());
        
        int cols = 5;
        int rows = PaintPanel.STATES_COLORS.length / cols;
        if (PaintPanel.STATES_COLORS.length % cols != 0)
            rows++;
          
        selectedColorPanel = new JPanel(new BorderLayout());
        selectedColorPanel.setBorder(BorderFactory.createLineBorder(new Color(219, 219, 219), 10));
        selectedColorPanel.setVisible(false);
        toolBar.add(selectedColorPanel);
        Dimension dim = new Dimension(toolBar.getPreferredSize().height, toolBar.getPreferredSize().height);
        selectedColorPanel.setPreferredSize(dim);
        selectedColorPanel.setMinimumSize(dim);
        selectedColorPanel.setMaximumSize(dim);
        JPanel innerPanel = new JPanel();
        innerPanel.setBackground(paintPanel.getSelectedColor());
        innerPanel.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
        selectedColorPanel.add(innerPanel, BorderLayout.CENTER);
        toolBar.addSeparator(new Dimension(toolBar.getPreferredSize().height / 2, toolBar.getPreferredSize().height));
        
        colorChoosersPanel = new JPanel(new GridLayout(rows, cols));
        colorChoosersPanel.setVisible(false);
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                if (i*cols + j < PaintPanel.STATES_COLORS.length)
                {
                    Color stateColor = PaintPanel.STATES_COLORS[i*cols + j];
                    JButton chooseColorButton = new JButton(createIcon(stateColor, 15, 15));
                    chooseColorButton.addActionListener(new ActionListener()
                    {

                        @Override
                        public void actionPerformed(ActionEvent ev)
                        {
                            innerPanel.setBackground(stateColor);
                            paintPanel.setSelectedColor(stateColor);
                        }
                    });
                    colorChoosersPanel.add(chooseColorButton);
                }
            }
        }
        toolBar.add(colorChoosersPanel);
        dim = new Dimension(toolBar.getPreferredSize().width / 3, toolBar.getPreferredSize().height);
        colorChoosersPanel.setMaximumSize(dim);
        
        transitions = new JComboBox<>();
        for (int i = 0; i < desktop.getAutomatonK(); i++)
            transitions.addItem(Integer.toString(i));
        transitions.addItem("Create new transition");
        transitions.setMaximumSize(new Dimension(100, 30));
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
}
