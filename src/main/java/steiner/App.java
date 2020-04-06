package steiner;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class App {

    public static void main(String[] args) throws Exception {
        MyGraph g = createConnectedGraph25N();
        g.printGraph();
        g = createConnectedGraphWithCircles25N();
        g.printGraph();
        g = createMaximumGraph10N();
        g.printGraph();
        g = createRandom25N();
        g.printGraph();
        g = createRandom50N();
        g.printGraph();
    }

    public static MyGraph createRandom50N() {
        MyGraph g = new MyGraph("Random50N");
        Random RNGesus = new Random();
        for (int i = 0; i < 50; i++) {
            g.addNode("" + i, RNGesus.nextBoolean());
        }
        for (int i = 0; i < 100; i++) {
            String n1 = "" + RNGesus.nextInt(50);
            String n2 = "" + RNGesus.nextInt(50);
            if (!g.getEdge(n1, n2).isPresent())
                g.addEdge(n1, n2, RNGesus.nextInt(25) + RNGesus.nextDouble());
        }
        return g;
    }

    public static MyGraph createRandom25N() {
        MyGraph g = new MyGraph("Random25N");
        Random RNGesus = new Random();
        for (int i = 0; i < 25; i++) {
            g.addNode("" + i, RNGesus.nextBoolean());
        }
        for (int i = 0; i < 50; i++) {
            String n1 = "" + RNGesus.nextInt(25);
            String n2 = "" + RNGesus.nextInt(25);
            if (!g.getEdge(n1, n2).isPresent())
                g.addEdge(n1, n2, RNGesus.nextInt(25) + RNGesus.nextDouble());
        }
        return g;
    }

    public static MyGraph createConnectedGraph25N() {
        MyGraph g = new MyGraph("ConnectedGraph25N");
        Random RNGesus = new Random();
        for (int i = 0; i < 25; i++) {
            g.addNode("" + i, RNGesus.nextBoolean());
        }
        while (g.numberOfComponents() > 1) {
            String node1 = "" + RNGesus.nextInt(25);
            HashSet<Node> set = g.getNodesNotinComponent(node1);
            int i = RNGesus.nextInt(set.size());
            Iterator<Node> it = set.iterator();
            String node2 = it.next().getName();
            for (int j = 0; j < i - 1; j++) {
                node2 = it.next().getName();
            }
            g.addEdge(node1, node2, RNGesus.nextInt(25) + RNGesus.nextDouble());
        }
        return g;
    }

    public static MyGraph createConnectedGraphWithCircles25N() {
        MyGraph g = new MyGraph("ConnectedGraphWithCircles25N");
        Random RNGesus = new Random();
        for (int i = 0; i < 25; i++) {
            g.addNode("" + i, RNGesus.nextBoolean());
        }
        while (g.numberOfComponents() > 1) {
            String node1 = "" + RNGesus.nextInt(25);
            HashSet<Node> set = g.getNodesNotinComponent(node1);
            int i = RNGesus.nextInt(set.size());
            Iterator<Node> it = set.iterator();
            String node2 = it.next().getName();
            for (int j = 0; j < i - 1; j++) {
                node2 = it.next().getName();
            }
            g.addEdge(node1, node2, RNGesus.nextInt(25) + RNGesus.nextDouble());
        }
        for (int i = 0; i < RNGesus.nextInt(25); i++) {
            String node1 = "" + RNGesus.nextInt(25);
            HashSet<Node> set = g.getNodesNotConnected(node1);
            String node2 = set.iterator().next().getName();
            g.addEdge(node1, node2, RNGesus.nextInt(25) + RNGesus.nextDouble());
        }
        return g;
    }

    public static MyGraph createMaximumGraph10N() {
        MyGraph g = new MyGraph("MaximumGraph10N");
        Random RNGesus = new Random();
        for (int i = 0; i < 10; i++) {
            g.addNode("" + i, RNGesus.nextBoolean());
        }
        for (int i = 0; i < 10; i++) {
            for (int j = i; j < 10; j++) {
                g.addEdge("" + i, "" + j, RNGesus.nextInt(25) + RNGesus.nextDouble());
            }
        }
        return g;
    }
}