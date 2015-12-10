package learn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import fsa.FSA;
import fsa.FSAEdge;
import fsa.FSANode;

public class Main {
    
    public static final String[] sentences = {"Mary eats pies",
                                              "John bakes cakes",
                                              "Mary bakes cakes"};
        //{"a b c", "e f g", "a f g"};
        //{"a a a a a b c c c", "a b c", "b c", "a a b c", "a a b c c", "a a a a a a a a a a a b c c", "b c c", "b"};
        //{"c a b", "c a a b", "c a a a a a a a a a a b", "c a d"};
        //{"a", "a a a", "a a a a a a a a"};
        //{"he listens", "he sleeps", "she listens", "she sleeps"};
        //{"a a a a a a a a", "a a a", "a", "b b b b", "b b", "b b b b b", "a a a a b b"};
        //{"0 1 1 0 1 0 1", "1 1 0 1", "0 0 1 0", "1 1 1 1 0", "1 1 1 1 1", "0"};
        //{"0 1 0 1 0", "0 1 0", "1 0 1 0", "0 1 0 1", "1"};
        // multiples of 3
        /*{"1 1", "1 1 0", "1 0 0 1", "1 1 0 1 1", "1 1 0 0 0 1 1 1 1", "1 0 0 1 0 1 1 0 0",
         "1 0 1 0 1"};*/
        // powers of 2
        //{"1", "0 0 0 0 1", "0 0 1 0 0 0", "0 1", "1 0 0 0 0 0", "0 0 0 0 0 0 1 0 0"};
            
        /*{"0 1 1 0 0 0", "1 0 1 1 1 1 0 0 0", "0 1 1 1 1 1 1 0", "0 0 0 0 0", "0 1 0 0 0 1 0", "1 0 0 0 1 0 0 1 1 0 1",
                "0 0", "1 1", "0 1 1", "1 0 0"};*/
            
   
    
    public static void main(String[] args) {
        FSA fsa = new FSA();
        
        for(String sentence : sentences) {
            fsa.ingestSentence(sentence);
        }
        
        generateGraphView(fsa);
    }
    
    private static void generateGraphView(FSA fsa) {
        Layout<FSANode, FSAEdge> layout = new ISOMLayout<FSANode, FSAEdge>(fsa.getVisualGraph());
        layout.setSize(new Dimension(650, 650));
        VisualizationViewer<FSANode,FSAEdge> v = new VisualizationViewer<FSANode,FSAEdge>(layout);
        v.setPreferredSize(new Dimension(700,700)); //Sets the viewing area size
        
        Transformer<FSANode,Paint> vertexColor = new Transformer<FSANode,Paint>() {
            public Paint transform(FSANode node) {
                if(node.isAccepting()) return Color.GREEN;
                if (node.getID().startsWith("0") || node.getID().contains(",0")) { //|| node.getID().endsWith("0")) {
                    return Color.BLUE;
                } else if (node.isAccepting()) return Color.GREEN;
                return Color.RED;
            }
        };
        
        v.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<FSANode>());
        v.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<FSAEdge>());
        v.getRenderContext().setVertexFillPaintTransformer(vertexColor);
        DefaultModalGraphMouse<FSANode, FSAEdge> gm = new DefaultModalGraphMouse<FSANode, FSAEdge>();
        gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        v.setGraphMouse(gm); 
        v.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {};

            @Override
            public void keyPressed(KeyEvent e) {
                fsa.merge();
                Layout<FSANode, FSAEdge> newLayout = new ISOMLayout<FSANode, FSAEdge>(fsa.getVisualGraph());
                v.setGraphLayout(newLayout);
                v.getRenderContext().setVertexFillPaintTransformer(vertexColor);
                v.repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {};
            
        });
        
        JFrame frame2 = new JFrame("Simple Graph View");
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.getContentPane().add(v); 
        frame2.pack();
        frame2.setVisible(true); 
    }
    
}
