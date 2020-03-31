package steiner;

import java.util.Iterator;
import java.util.Random;

/* import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph; */

public class App {

    public static void main(String[] args) throws Exception {
        //createRandom50N100E();
    }

    /* public static void mini(){
        Graph g = new Graph();
        g.addNode(true);
        g.addNode(false);
        g.connectNodes(0, 1, 5);
        System.out.println(g.printGraph());
    } */

/*     public static void createRandom50N100E(){
        Graph g= new SingleGraph("test1");
        Random RNGesus = new Random();
        for (int i = 0; i < 50; i++) {
            g.addNode(""+i);
        }
        for (int i = 0; i < 100; i++) {
            int n1 = RNGesus.nextInt(50);
            int n2 = RNGesus.nextInt(50);
            if (g.getEdge(""+n1+"<>"+n2)==null&&g.getEdge(""+n2+"<>"+n1)==null)
            g.addEdge(""+n1+"<>"+n2, ""+n1,""+n2);
        }
        Iterator<? extends Edge> i=g.getEachEdge().iterator();
        while(i.hasNext()){
            Edge e=i.next();
            e.addAttribute("ui.label", RNGesus.nextInt(25));;
        }
        g.display();
    } */

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