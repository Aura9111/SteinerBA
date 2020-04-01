package steiner;

import java.util.Random;

/* import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph; */

public class App {

    public static void main(String[] args) throws Exception {
        MyGraph g= createRandom50N100E();
        g.printGraph();
    }

    /* public static void mini(){
        Graph g = new Graph();
        g.addNode(true);
        g.addNode(false);
        g.connectNodes(0, 1, 5);
        System.out.println(g.printGraph());
    } */

    public static MyGraph createRandom50N100E(){
        MyGraph g= new MyGraph();
        Random RNGesus = new Random();
        for (int i = 0; i < 50; i++) {
            g.addNode(""+i, RNGesus.nextBoolean());
        }
        for (int i = 0; i < 100; i++) {
            String n1 = ""+RNGesus.nextInt(50);
            String n2 = ""+RNGesus.nextInt(50);
            if (!g.getEdge(n1,n2).isPresent())
            g.addEdge(n1, n2, RNGesus.nextInt(25)+RNGesus.nextDouble());
        }
        return g;
    }

    /* public static void createRandomGraph() {
        Graph g = new Graph();
        Random RNGesus = new Random();
        for (int i = 0; i < 50; i++) {
            g.addNode(RNGesus.nextInt(2) == 0);
        }
        for (int i = 0; i < RNGesus.nextInt(500); i++) {
            int n1 = RNGesus.nextInt(50);
            int n2 = RNGesus.nextInt(50);
            if (!g.getEdge(n1, n2).isPresent())
                g.connectNodes(n1, n2, RNGesus.nextInt(25));
        }
        System.out.println(g.printGraph());
    } */
}