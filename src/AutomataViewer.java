
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;


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
    
    private SplitPane splitPane;
    private PaintPanel paintPanel;
    
    private JToolBar toolBar;
    private final String[] iconFiles = { 
        "icons/add_state.png", "icons/remove_state.png", "icons/replace_states.png",
        "icons/add_transition.png", "icons/change_color.png", "icons/move_state.png" 
    };
    private final String [] buttonLabels = { "Add state", "Remove state", "Replace states", "Add transition", "Change color", "Move state" };
    private final JButton [] toolBarButtons = new JButton[buttonLabels.length];
    
    private JPanel selectedColorPanel;
    private JPanel colorChoosersPanel;
    private JComboBox<String> transitions = new JComboBox<>();

    public AutomataViewer(JFrame frame) 
    {
        this.frame = frame;
        splitPane = new SplitPane();
        paintPanel = splitPane.getPaintPanel();
        
        paintPanel.addPropertyChangeListener("updateAutomaton", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                splitPane.getTextPanel().getTextArea().setText(splitPane.getAutomatonString());       
                updateTransitionsComboBox(); 
            }
        });
        
        splitPane.getTextPanel().addPropertyChangeListener("updateTransitions", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                updateTransitionsComboBox();
            }
        });
        
        createMenuBar();
        createToolBar();

        Container container = frame.getContentPane();
        container.setLayout(new BorderLayout());
        container.add(toolBar, BorderLayout.NORTH);
        container.add(splitPane, BorderLayout.CENTER);
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
        
        toolbarsMenu = new JMenu("Toolbars");
        for (DockToolbar dockToolbar : splitPane.getDockToolbars())
        {
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(dockToolbar.getName());
            menuItem.setSelected(true);
            toolbarsMenu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ev)
                {
                    dockToolbar.Dock();
                    dockToolbar.setVisible(menuItem.isSelected());
                }
            });
        }
        menuBar.add(toolbarsMenu);
        frame.setJMenuBar(menuBar);
    }
    
    private void createToolBar()
    {
        toolBar = new JToolBar("Tool Bar");
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.GRAY);
        
        Color noBackground = (new JButton()).getBackground();
        Color selectedButtonColor = Color.CYAN;
        ActionListener actionListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                for (PaintPanel.Operation op : PaintPanel.Operation.values())
                {
                    int i = op.getValue();
                    if (ev.getSource() == toolBarButtons[i])
                    {
                        if (paintPanel.getOperation() != i)
                        {
                            toolBarButtons[paintPanel.getOperation()].setBackground(noBackground);
                            paintPanel.setOperation(op);
                            paintPanel.repaint();
                            toolBarButtons[i].setBackground(selectedButtonColor);
                        }

                        if (paintPanel.getOperation() == PaintPanel.Operation.CHANGE_COLOR.getValue())
                        {
                            selectedColorPanel.setVisible(true);
                            colorChoosersPanel.setVisible(true);
                        }
                        else
                        {
                            selectedColorPanel.setVisible(false);
                            colorChoosersPanel.setVisible(false);
                        }

                        if (paintPanel.getOperation() == PaintPanel.Operation.ADD_TRANS.getValue())
                            transitions.setVisible(true);
                        else
                            transitions.setVisible(false);
                        
                        if (paintPanel.getOperation() != PaintPanel.Operation.REPLACE_STATES.getValue())
                            paintPanel.resetReplaceStatesFirstState();

                        break;
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
        
        toolBarButtons[paintPanel.getOperation()].setBackground(selectedButtonColor);
        
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
        
        updateTransitionsComboBox();
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
    
    private void updateTransitionsComboBox()
    {
        if (transitions.getItemCount() == 0) // init
        {
            for (int i = 0; i < splitPane.getAutomatonK(); i++)
                transitions.addItem(PaintPanel.TRANSITIONS_LETTERS[i % PaintPanel.TRANSITIONS_LETTERS.length]);
            transitions.addItem("Create new transition");
            transitions.setMaximumSize(new Dimension(100, 30));
            transitions.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    paintPanel.setSelectedTransition(transitions.getSelectedIndex());
                }
            });
            transitions.setPrototypeDisplayValue("Create new transition   ");
            transitions.setVisible(false);
            ComboBoxRenderer renderer = new ComboBoxRenderer(transitions);
            transitions.setRenderer(renderer);
        }
        else
        {
            int K = splitPane.getAutomatonK();
            if(transitions.getItemCount() == K && transitions.getSelectedIndex() == K - 1)
            {
                transitions.removeItemAt(K - 1);
                transitions.addItem(PaintPanel.TRANSITIONS_LETTERS[(K - 1) % PaintPanel.TRANSITIONS_LETTERS.length]);
                transitions.addItem("Create new transition");
                transitions.setSelectedIndex(K - 1);
            }
            else if (transitions.getItemCount() != K + 1)
            {
                transitions.removeAllItems();
                for (int i = 0; i < K; i++)
                    transitions.addItem(PaintPanel.TRANSITIONS_LETTERS[i % PaintPanel.TRANSITIONS_LETTERS.length]);
                transitions.addItem("Create new transition");
            }
        }
    }
    
    private class ComboBoxRenderer extends JPanel implements ListCellRenderer
    {
        JPanel textPanel;
        JLabel text;

        public ComboBoxRenderer(JComboBox combo) 
        {
            textPanel = new JPanel();
            text = new JLabel();
            text.setOpaque(true);
            textPanel.add(text);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) 
        {
            if (isSelected)
                setBackground(list.getSelectionBackground());
            else
                setBackground(Color.WHITE);

            text.setBackground(getBackground());

            text.setText(value.toString());
            if (index > -1)
                text.setForeground(PaintPanel.TRANSITIONS_COLORS[index]);
            
            return text;
        }
    }
}
