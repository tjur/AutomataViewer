
package Viewer;

import AutomatonModels.Automaton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PaintPanel extends JPanel implements MouseListener, MouseMotionListener
{

    private static final long serialVersionUID = 1L;
  // ************************************************************************
    // Content

    private static final int VERTEX_RADIUS = 25;
    private static final int ARR_SIZE = 10;
    
    private boolean showRange;
    private boolean showAction;
    private int[] rangeStates;
    private HashMap<Integer, ArrayList<Integer>> actionStates;
    
    private class Transition
    {
        public int stateOut;
        public int stateIn;
        public int k;
        boolean inverse;
        
        public Transition(int stateOut, int stateIn, int k, boolean inverse)
        {
            this.stateOut = stateOut;
            this.stateIn = stateIn;
            this.k = k;
            this.inverse = inverse;
        }
    }
    
    public static enum Operation
    {
        MOVE_STATES(0), ADD_STATES(1), REMOVE_STATES(2), SWAP_STATES(3), ADD_TRANS(4), SELECT_STATES(5);
        
        private final int value;
        
        private Operation(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

    public static final Color[] STATES_COLORS =
    {
        Color.WHITE,
        new Color(96, 128, 255), new Color(255, 128, 96), new Color(96, 255, 96),
        new Color(255, 255, 96), new Color(96, 255, 255), new Color(255, 96, 255),
        Color.ORANGE, new Color(160, 0, 210), new Color(219, 112, 147)
    };

    private Automaton automaton;
    private Color[] colors;
    private Point[] vertices;
    private int[] orders;
    private int highlighted;
    
    private Operation operation;
    private Color unselectedStateColor;
    private Color selectedStateColor;
    private int selectedTransition;
    private int addTransFirstState;
    private int swapStatesFirstState;

    private int grabbed;
    private int grabX, grabY, grabShiftX, grabShiftY;

  // ************************************************************************
    // Initialization
    public PaintPanel(Automaton automaton)
    {
        setMinimumSize(new Dimension(550, 400));
        
        this.grabbed = -1;
        this.highlighted = -1;
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.operation = Operation.MOVE_STATES;
        this.unselectedStateColor = AutomatonHelper.defaultUnselectedStateColor;
        this.selectedStateColor = AutomatonHelper.defaultSelectedStateColor;
        
        showRange = false;
        showAction = false;
        
        setAutomaton(automaton);
        
        automaton.addPropertyChangeListener("automatonReset", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                updateAutomatonData();
                repaintCenterAutomaton();
                firePropertyChange("updateTransitions", false, true);
            }
        });
        
        automaton.addPropertyChangeListener("automatonChanged", new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent ev)
            {
                repaint();
            }
        });
        
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent ev) 
            {
                int width = PaintPanel.this.getSize().width;
                int height = PaintPanel.this.getSize().height;
                
                for (Point vertex : vertices)
                {
                    if (vertex.x + VERTEX_RADIUS > width)
                        vertex.x = width - VERTEX_RADIUS;
                    if (vertex.y + VERTEX_RADIUS > height)
                        vertex.y = height - VERTEX_RADIUS;
                }
                
                repaint();
            }
        });
    }
    
    private int getN()
    {
        return automaton.getN();
    }
    
    private int getK()
    {
        return automaton.getK();
    }
    
    public int getOperation()
    {
        return this.operation.getValue();
    }
    
    public void setOperation(Operation operation)
    {
        this.operation = operation;
    }
    
    public Color getSelectedStateColor()
    {
        return selectedStateColor;
    }
    
    public void setSelectedStateColor(Color color)
    {
        if (color.equals(unselectedStateColor))
        {
            JOptionPane.showMessageDialog(this, "Color 1 and Color 2 must be different.");
            return;
        }
        
        if (!color.equals(selectedStateColor))
        {
            int[] selectedStates = automaton.getSelectedStates();
            for (int i = 0; i < selectedStates.length; i++)
            {
                if (selectedStates[i] == 1)
                {
                    automaton.unselectState(i);
                    colors[i] = selectedStateColor;
                }
            }
            
            for (int i = 0; i < colors.length; i++)
            {
                if (colors[i].equals(color))
                {
                    automaton.selectState(i);
                    colors[i] = unselectedStateColor;
                }
            }
            
            selectedStateColor = color;
            repaint();
        }
    }
    
    public Color getUnselectedStateColor()
    {
        return unselectedStateColor;
    }
    
    public void setUnselectedStateColor(Color color)
    {
        if (color.equals(selectedStateColor))
            JOptionPane.showMessageDialog(this, "Color 1 and Color 2 must be different.");
        else
        {
            unselectedStateColor = color;
            repaint();
        }
    }
    
    public void setSelectedTransition(int trans)
    {
        selectedTransition = trans;
    }
    
    public void resetReplaceStatesFirstState()
    {
        swapStatesFirstState = -1;
    }
    
    public void setShowRange(boolean showRange)
    {
        this.showRange = showRange;
        repaint();
    }
    
    public void setShowAction(boolean showAction)
    {
        this.showAction = showAction;
        repaint();
    }
    
    public void showRange(int[] states)
    {
        showRange = true;
        rangeStates = states;
        repaint();
    }
    
    public void showAction(HashMap<Integer, ArrayList<Integer>> actionStates)
    {
        showAction = true;
        this.actionStates = actionStates;
        repaint();
    }

    private void setAutomaton(Automaton automaton)
    {
        this.automaton = automaton;
        int N = getN();
        
        this.vertices = new Point[N];
        this.orders = new int[N];
        this.colors = new Color[N];
        this.highlighted = -1;
        this.addTransFirstState = -1;
        this.swapStatesFirstState = -1;
        int[] selectedStates = new int[N];
        for (int n = 0; n < N; n++)
        {
            orders[n] = n;
            colors[n] = unselectedStateColor;
            selectedStates[n] = 1;
        }
        
        automaton.selectStates(selectedStates);
        repaintCenterAutomaton();
    }
    
    public void updateAutomatonData()
    {
        int N = getN();
        this.vertices = new Point[N];
        this.orders = new int[N];
        this.colors = new Color[N];
        this.highlighted = -1;
        this.addTransFirstState = -1;
        this.swapStatesFirstState = -1;
        for (int n = 0; n < N; n++)
        {
            orders[n] = n;
            colors[n] = unselectedStateColor;
        }
        
        automaton.clearSelectedStates();
    }
    
    // repaint automaton with states moved to center
    public void repaintCenterAutomaton()
    {   
        int width = this.getWidth();
        int height = this.getHeight();
        int r = ((width < height ? width : height) - VERTEX_RADIUS * 3) / 2;
        int cx = width / 2;
        int cy = height / 2;
        double angle = 0.0;
        int N = getN();
        for (int n = 0; n < N; n++)
        {
            vertices[n] = new Point(
                    (int) (Math.sin(angle) * r + cx),
                    (int) (-Math.cos(angle) * r + cy));
            angle += 2 * Math.PI / N;
        }
        repaint();
    }

  // ************************************************************************
    // Interface
    @Override
    public void mouseMoved(MouseEvent ev)
    {
        grabX = (ev.getX() >= 0 && ev.getX() <= this.getSize().width) ? ev.getX() : grabX;
        grabY = (ev.getY() >= 0 && ev.getY() <= this.getSize().height) ? ev.getY() : grabY;
        highlighted = -1;
        for (int i = getN() - 1; i >= 0; i--)
        {
            int v = orders[i];
            double d = (grabX - vertices[v].x) * (grabX - vertices[v].x) + (grabY - vertices[v].y) * (grabY - vertices[v].y);
            if (d <= VERTEX_RADIUS * VERTEX_RADIUS)
            {
                highlighted = v;
                break;
            }
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent ev)
    {
        grabX = (ev.getX() >= 0 && ev.getX() <= this.getSize().width) ? ev.getX() : grabX;
        grabY = (ev.getY() >= 0 && ev.getY() <= this.getSize().height) ? ev.getY() : grabY;
        mouseMoved(ev);
        
        if (grabbed == -1)
            return;
        
        if (operation == Operation.MOVE_STATES)
        {
            if (grabX + grabShiftX >= VERTEX_RADIUS && grabX + grabShiftX <= this.getSize().width - VERTEX_RADIUS)
                vertices[grabbed].x = grabX + grabShiftX;
            if (grabY + grabShiftY >= VERTEX_RADIUS && grabY + grabShiftY <= this.getSize().height - VERTEX_RADIUS)
                vertices[grabbed].y = grabY + grabShiftY;
            repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent ev)
    { 
        if (grabbed != -1)
            return;
        
        mouseMoved(ev);
        if (ev.getButton() == MouseEvent.BUTTON1)
        {
            if (operation == Operation.ADD_STATES)
            {
                automaton.addState();
                int N = getN();

                this.orders = new int[N];
                for (int n = 0; n < N; n++)
                    this.orders[n] = n;

                Color[] temp = new Color[N];
                System.arraycopy(this.colors, 0, temp, 0, N - 1);
                temp[N-1] = unselectedStateColor;
                this.colors = temp;

                Point[] temp2 = new Point[N];
                System.arraycopy(this.vertices, 0, temp2, 0, N - 1);
                temp2[N-1] = ev.getPoint();
                this.vertices = temp2;    
            }
            else if (highlighted >= 0 && operation == Operation.REMOVE_STATES)
            {   
                int N = getN();
                this.orders = new int[N - 1];
                for (int n = 0; n < N - 1; n++)
                    this.orders[n] = n;

                Color[] temp = new Color[N - 1];
                Point[] temp2 = new Point[N - 1];
                for (int n = 0; n < N - 1; n++)
                {
                    if (n < highlighted)
                    {
                        temp[n] = this.colors[n];
                        temp2[n] = this.vertices[n];
                    }
                    else
                    {
                        temp[n] = this.colors[n+1];
                        temp2[n] = this.vertices[n+1];
                    }
                }
                this.colors = temp;
                this.vertices = temp2;

                automaton.removeState(highlighted);
                highlighted = -1;
            }
            else if (highlighted >= 0 && operation == Operation.SWAP_STATES)
                swapStatesFirstState = highlighted;
            else if (highlighted >= 0 && operation == Operation.ADD_TRANS)
                addTransFirstState = highlighted;
            else if (highlighted >= 0 && operation == Operation.SELECT_STATES)
                automaton.selectState(highlighted);
            else if (highlighted >= 0)
            {         
                grabShiftX = (vertices[highlighted].x - grabX);
                grabShiftY = (vertices[highlighted].y - grabY);
                grabbed = highlighted;
                int N = getN();
                int i = N - 1;
                while (orders[i] != highlighted)
                    i--;
                
                for (int j = i; j < N - 1; j++)
                    orders[j] = orders[j + 1];

                orders[N - 1] = highlighted;
            }
        }
        else if (ev.getButton() == MouseEvent.BUTTON3)
        {
            if (highlighted >= 0 && operation == Operation.SELECT_STATES)
            {
                automaton.unselectState(highlighted);
                colors[highlighted] = unselectedStateColor;
            }
        }
        
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent ev)
    {
        if (operation == Operation.ADD_TRANS)
        {
            if (addTransFirstState >= 0 && highlighted >= 0 && getK() > 0)
            {
                automaton.addTransition(addTransFirstState, highlighted, selectedTransition);
                firePropertyChange("updateTransitions", false, true);
            }   
            addTransFirstState = -1;
        }
        else if (operation == Operation.SWAP_STATES)
        {
            if (swapStatesFirstState >= 0 && highlighted >= 0 && swapStatesFirstState != highlighted)
            {
                automaton.replaceStates(swapStatesFirstState, highlighted);
                Point temp = vertices[highlighted];
                vertices[highlighted] = vertices[swapStatesFirstState];
                vertices[swapStatesFirstState] = temp;
                int temp2 = orders[highlighted];
                orders[highlighted] = orders[swapStatesFirstState];
                orders[swapStatesFirstState] = temp2;
            }   
            swapStatesFirstState = -1;
        }
        else
        {
            grabbed = -1;
            mouseMoved(ev);
        }
        
        repaint();
    }

  // ************************************************************************
    // Drawing
    void drawEdge(Graphics2D g, int x1, int y1, int x2, int y2, int k, int transQuantity, boolean inverse, boolean marked, boolean centered)
    {
        final double K_SHIFT = (VERTEX_RADIUS * 2) / (transQuantity + 1);
        AffineTransform oldTransform = g.getTransform();
        double dx = x2 - x1, dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (centered) ? (int) Math.sqrt(dx * dx + dy * dy) : (int) Math.sqrt(dx * dx + dy * dy) - VERTEX_RADIUS;
        AffineTransform at = new AffineTransform(oldTransform);
        at.translate(x1, y1);
        at.concatenate(AffineTransform.getRotateInstance(angle));
        g.setTransform(at);

        int yshift = (int) ((k - transQuantity * 0.5) * K_SHIFT + K_SHIFT / 2);
        if (marked)
        {
            float dash1[] = { 8.0f, 14.0f };
            final BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            g.setStroke(dashed);
        }
        g.drawLine(0, yshift, len, yshift);
        g.setStroke(new BasicStroke());
        g.fillPolygon(
            new int[] { len, len - ARR_SIZE, len - ARR_SIZE },
            new int[] { yshift, -ARR_SIZE / 2 + yshift, ARR_SIZE / 2 + yshift }, 
            3);
        if (inverse)
        {
            g.fillPolygon(
            new int[] { VERTEX_RADIUS, VERTEX_RADIUS + ARR_SIZE, VERTEX_RADIUS + ARR_SIZE },
            new int[] { yshift, -ARR_SIZE / 2 + yshift, ARR_SIZE / 2 + yshift }, 
            3);
        }
        g.setTransform(oldTransform);
    }

    @Override
    public void paint(Graphics graphics)
    {
        Graphics2D g = (Graphics2D) graphics;
        int width = this.getWidth();
        int height = this.getHeight();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        int[][] matrix = automaton.getMatrix();
        int N = getN();
        int K = getK();
        
        // draw oval of range states at the beginning
        for (int i = 0; i < N; i++)
        {
            int n = orders[i];
            if (showRange && rangeStates[n] == 1)
            {
                g.setStroke(new BasicStroke(5));
                g.setColor(selectedStateColor);
                if (highlighted != -1 && highlighted == n)
                    g.setColor(g.getColor().brighter());
                g.drawOval(vertices[n].x - VERTEX_RADIUS - 3, vertices[n].y - VERTEX_RADIUS - 3, (VERTEX_RADIUS * 2) + 6, (VERTEX_RADIUS * 2) + 6);
                g.setStroke(new BasicStroke());
                if (highlighted != -1 && highlighted == n)
                    g.setColor(Color.LIGHT_GRAY);
                else
                    g.setColor(Color.BLACK);
                g.drawOval(vertices[n].x - VERTEX_RADIUS - 4, vertices[n].y - VERTEX_RADIUS - 4, (VERTEX_RADIUS * 2) + 8, (VERTEX_RADIUS * 2) + 8);
            }
        }
        
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke());
        
        // draw transitions
        for (int n = 0; n < N; n++)
        {
            for (int n2 = n + 1; n2 < N; n2++)
            {
                ArrayList<Transition> transitions = new ArrayList<>();
                for (int k = 0; k < K; k++)
                {
                    if (matrix[n][k] == n2)
                        transitions.add(new Transition(n, n2, k, matrix[n2][k] == n));
                }
                for (int k = 0; k < K; k++)
                {
                    if (matrix[n2][k] == n && matrix[n][k] != n2)
                        transitions.add(new Transition(n2, n, k, false));
                }
                
                if (showAction)
                {
                    if (actionStates.getOrDefault(n, new ArrayList<>()).contains(n2))
                    {
                        if (actionStates.getOrDefault(n2, new ArrayList<>()).contains(n))
                            transitions.add(new Transition(n, n2, -1, true));
                        else
                            transitions.add(new Transition(n, n2, -1, false));
                    }
                    else if (actionStates.getOrDefault(n2, new ArrayList<>()).contains(n))
                        transitions.add(new Transition(n2, n, -1, false));
                }

                int transNumber = transitions.size();
                for (int i = 0; i < transNumber; i++)
                {
                    Transition trans = transitions.get(i);
                    int j = (trans.stateOut == n) ? i : transNumber - i - 1;
                    if (trans.k == -1)
                    {
                        g.setStroke(new BasicStroke(2));
                        g.setColor(Color.BLACK);
                    }
                    else
                    {
                        g.setStroke(new BasicStroke());
                        g.setColor(AutomatonHelper.TRANSITIONS_COLORS[trans.k % AutomatonHelper.TRANSITIONS_COLORS.length]);
                    }
                    drawEdge(g, vertices[trans.stateOut].x, vertices[trans.stateOut].y,
                        vertices[trans.stateIn].x,
                        vertices[trans.stateIn].y, j, transNumber, trans.inverse, false, false);
                }
            }
        }
        
        // draw new transition when you are in ADD_TRANS mode
        if (addTransFirstState >= 0 && operation == Operation.ADD_TRANS && K > 0)
        {
            g.setColor(AutomatonHelper.TRANSITIONS_COLORS[selectedTransition]);
            drawEdge(g, vertices[addTransFirstState].x, vertices[addTransFirstState].y,
                grabX,
                grabY, 0, 1, false, true, true);
        }
        else if (swapStatesFirstState >= 0 && operation == Operation.SWAP_STATES)
        {
            g.setColor(new Color(0, 0, 0, 0.1f));
            g.setStroke(new BasicStroke(2));
            drawEdge(g, vertices[swapStatesFirstState].x, vertices[swapStatesFirstState].y,
                grabX,
                grabY, 0, 1, true, false, true);
        }     
        
        // draw states
        for (int i = 0; i < N; i++)
        {
            int n = orders[i];
            if (automaton.isSelected(n))
            {
                g.setColor(selectedStateColor);
                colors[n] = unselectedStateColor;
            }
            else
                g.setColor(colors[n]);
            
            if (highlighted != -1 && highlighted == n)
                g.setColor(g.getColor().brighter());
            
            g.fillOval(vertices[n].x - VERTEX_RADIUS, vertices[n].y - VERTEX_RADIUS, VERTEX_RADIUS * 2, VERTEX_RADIUS * 2);
                     
            g.setColor(Color.BLACK);
            if (highlighted != -1 && highlighted == n)
                g.setColor(Color.LIGHT_GRAY);
            
            if (operation == Operation.SWAP_STATES && swapStatesFirstState == n)
            {
                g.setColor(Color.RED.darker());
                g.setStroke(new BasicStroke(4));
            }
 
            g.drawOval(vertices[n].x - VERTEX_RADIUS, vertices[n].y - VERTEX_RADIUS, VERTEX_RADIUS * 2, VERTEX_RADIUS * 2);           
            g.setStroke(new BasicStroke());
            g.setColor(Color.BLACK);
            String label = Integer.toString(n);
            g.drawString(label, vertices[n].x - g.getFontMetrics().stringWidth(label) / 2, vertices[n].y + 5);
        }
    }

    @Override
    public void mouseClicked(MouseEvent ev) {}

    @Override
    public void mouseEntered(MouseEvent ev) {}

    @Override
    public void mouseExited(MouseEvent ev) {}
}
