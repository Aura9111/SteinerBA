package steiner;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class App {

    public static void main(String[] args) throws Exception {
        Graph g = GraphFactory.g011();
        Tree t = mst(g);
        t.printGraph();
    }

    public static SetPair prepareChange(Tree t, HashSet<Tree> terminals, SetPair setPair) throws IOException {
        if (terminals.size() == 1)
            return setPair;
        TreeEdge e = t.getMaxCostConnectingEdge(terminals);
        HashSet<Tree> terminalsTo = t.splitTermOnEdge(e);
        HashSet<Tree> terminalsFrom = terminals;
        terminalsFrom.removeAll(terminalsTo);
        setPair.add(prepareChange(t, terminalsTo, new SetPair()));
        setPair.add(prepareChange(t, terminalsFrom, new SetPair()));
        Tree nFrom = terminalsFrom.iterator().next();
        Tree nTo = terminalsTo.iterator().next();
        TreeEdge f = new TreeEdge(nFrom, nTo, e.cost);
        setPair.addSet.add(f);
        setPair.removeSet.add(e);
        return setPair;
    }

    public static Tree mst(Graph g) throws Exception {
        if (g.numberOfComponents() > 1)
            throw new Exception();
        HashMap<String, Node> nodes = g.getNodes();
        HashSet<Tree> forest = new HashSet<>();
        for (String n : nodes.keySet()) {
            forest.add(new Tree(nodes.get(n)));
        }
        HashMap<String, Edge> compToEdge;
        HashMap<Node, String> nodeToComp;
        while (forest.size() > 1) {
            compToEdge = new HashMap<>();
            nodeToComp = new HashMap<>();
            for (Tree t : forest) {
                String compName = t.node.getName();
                compToEdge.put(compName, null);
                for (Node n : t.getNodes()) {
                    nodeToComp.put(n, compName);
                }
            }
            for (Edge e : g.getEdges().values()) {
                if (!nodeToComp.get(e.first).equals(nodeToComp.get(e.second))) {
                    if (compToEdge.get(nodeToComp.get(e.first)) == null)
                        compToEdge.put(nodeToComp.get(e.first), e);
                    else if (e.getWeight() < compToEdge.get(nodeToComp.get(e.first)).getWeight())
                        compToEdge.put(nodeToComp.get(e.first), e);
                    if (compToEdge.get(nodeToComp.get(e.second)) == null)
                        compToEdge.put(nodeToComp.get(e.second), e);
                    else if (e.getWeight() < compToEdge.get(nodeToComp.get(e.second)).getWeight())
                        compToEdge.put(nodeToComp.get(e.second), e);
                }
            }
            HashSet<Edge> alreadyDone = new HashSet<>();
            for (String c : compToEdge.keySet()) {
                if (compToEdge.get(c) != null) {
                    Edge e = compToEdge.get(c);
                    if (!alreadyDone.contains(e)) {
                        String root1 = nodeToComp.get(e.first);
                        String root2 = nodeToComp.get(e.second);
                        Tree t1 = null;
                        Tree t2 = null;
                        for (Tree t : forest) {
                            if (t.node.getName().equals(root1))
                                t1 = t;
                            if (t.node.getName().equals(root2))
                                t2 = t;
                        }
                        if (root1.equals("18") && root2.equals("2")) {
                            t1.printGraph("t1");
                            t2.printGraph("t2");
                        }
                        int t1Size = t1.getNodes().size();
                        int t2Size = t2.getNodes().size();
                        forest.remove(t1);
                        forest.remove(t2);
                        t2 = t2.makeRoot(e.second);
                        if (root1.equals("18") && root2.equals("2")) {
                            t1.printGraph("t1");
                            t2.printGraph("t2_2");
                        }
                        if (t2.getNodes().size() != t2Size)
                            throw new Exception("wir haben einen verloren");
                        if (!t1.combineWith(t2, e)) {
                            System.out.println(t1 + " " + t2 + " " + e);
                        }
                        if (t1.getNodes().size() != t1Size + t2Size)
                            throw new Exception("wir haben einen verloren");
                        for (Node n : t2.getNodes()) {
                            nodeToComp.put(n, t1.node.getName());
                        }
                        forest.add(t1);
                        alreadyDone.add(e);
                    }
                } else
                    throw new Exception("Graph nicht zusammenhängend");
            }
        }
        return forest.iterator().next();
    }

    public static void evaluationPhase(Tree t, int k) {
        for (int j = 3; j < k; j++) {

        }
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