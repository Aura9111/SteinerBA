package steiner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class App {

    public static void main(String[] args) throws Exception {
        Graph g = createConnectedGraph25N();
        g.printGraph();
        SetPair setPair = prepareChange(g, new SetPair(new HashSet<Edge>(), new HashSet<Edge>()));
        System.out.println(setPair.addSet + "\n" + setPair.removeSet);
        g.changePath("edited");
        g.printGraph();
        Tree t = new Tree(g.getHighestDegreeNode());
        t.printGraph();
    }

    public static SetPair prepareChange(Graph g, SetPair setPair) throws IOException {
        if (g.numberOfComponents() > 1 || g.hasCircles())
            return null;
        if (g.getAllTerminalNodes().size() == 1)
            return setPair;
        Edge e = g.getMaxCostConnectingEdge();
        g.removeEdge(e.first.getName(), e.second.getName());
        Iterator<Component> it = g.components.iterator();
        Component c1 = it.next();
        Component c2 = it.next();
        Graph g1 = new Graph("tmp");
        g1.addComponent(c1);
        setPair.add(prepareChange(g1, new SetPair()));
        Graph g2 = new Graph("tmp");
        g2.addComponent(c2);
        setPair.add(prepareChange(g2, new SetPair()));
        String nodeName1 = c1.getAllTerminalNodes().iterator().next().getName();
        String nodeName2 = c2.getAllTerminalNodes().iterator().next().getName();
        g.addEdge(nodeName1, nodeName2, e.getWeight());
        Edge f = g.getEdge(nodeName1, nodeName2).get();
        setPair.addSet.add(f);
        setPair.removeSet.add(e);
        return setPair;
    }

    public static Graph createRandom50N() {
        Graph g = new Graph("Random50N");
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

    public static Graph createRandom25N() {
        Graph g = new Graph("Random25N");
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

    public static Graph createConnectedGraph25N() {
        Graph g = new Graph("ConnectedGraph25N");
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

    public static Graph createConnectedGraphWithCircles25N() {
        Graph g = new Graph("ConnectedGraphWithCircles25N");
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
            HashSet<Node> set = g.getNonNeighborNodes(node1);
            String node2 = set.iterator().next().getName();
            g.addEdge(node1, node2, RNGesus.nextInt(25) + RNGesus.nextDouble());
        }
        return g;
    }

    public static Graph createMaximumGraph10N() {
        Graph g = new Graph("MaximumGraph10N");
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