package steiner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

public class App {

    public static void main(String[] args) throws Exception {
        Graph g = GraphFactory.g011();
        Tree t = bermanRamaiyer(g, g.getAllTerminalNodes(), 3);
        t.printGraph();
        System.out.println(t.totalCost());
    }

    public static Tree trimmedMst(Graph g, HashSet<Node> terminals) throws Exception {
        Tree t = mst(g);
        return recTrimHelper(t, terminals, false);
    }

    private static Tree recTrimHelper(Tree t, HashSet<Node> terminals, boolean parentHasTerminal) throws Exception {
        HashSet<Tree> newChildren = new HashSet<>();
        HashSet<Tree> removeSet = new HashSet<>();
        for (Tree child : t.children) {
            if (!child.leadsToNodeFromSet(terminals))
                removeSet.add(child);
        }
        for (Tree toRemove : removeSet) {
            t.children.remove(toRemove);
        }
        if ((parentHasTerminal ? 1 : 0) + t.children.size() >= 2 || terminals.contains(t.node)) {
            parentHasTerminal = true;
            for (Tree child : t.children) {
                newChildren.add(recTrimHelper(child, terminals, parentHasTerminal));
            }
            t.children = newChildren;
            return t;
        } else if (parentHasTerminal || t.children.size() == 0)
            throw new Exception("this doesnt lead to any node. why was it called");
        else {
            return recTrimHelper(t.children.iterator().next(), terminals, parentHasTerminal);
        }
    }

    public static SetPair prepareChange(Tree t, HashSet<Node> terminals, SetPair setPair) throws IOException {
        if (terminals.size() == 1)
            return setPair;
        TreeEdge e = t.getMaxCostConnectingEdge(terminals);
        HashSet<Node> terminalsTo = t.splitTermOnEdge(e);
        HashSet<Node> terminalsFrom = new HashSet<>();
        for (Node n : terminals) {
            if (!terminalsTo.contains(n))
                terminalsFrom.add(n);
        }
        setPair.add(prepareChange(t, terminalsTo, new SetPair()));
        setPair.add(prepareChange(t, terminalsFrom, new SetPair()));
        Node nFrom = terminalsFrom.iterator().next();
        Node nTo = terminalsTo.iterator().next();
        TreeEdge f = new TreeEdge(t, nFrom, nTo, e.cost);
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
                        int t1Size = t1.getNodes().size();
                        int t2Size = t2.getNodes().size();
                        forest.remove(t1);
                        forest.remove(t2);
                        t2 = t2.makeRoot(e.second);
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

    public static HashMap<Integer, Stack<myEvaluationResult>> evaluationPhase(Graph g, Tree t, int k,
            HashSet<Node> terminals) throws Exception {
        HashMap<Integer, Stack<myEvaluationResult>> stackMap = new HashMap<>();
        for (int j = 3; j <= k; j++) {
            stackMap.put(j, new Stack<>());
            HashSet<HashSet<Node>> sets = t.getXElementSubsets(j);
            for (HashSet<Node> nodeSet : sets) {
                SetPair setPair = prepareChange(t, nodeSet, new SetPair());
                double gain = cost(setPair.removeSet) - scost(g, nodeSet, k);
                if (gain > 0) {
                    Forest forest = new Forest(t);
                    for (TreeEdge e : setPair.removeSet) {
                        forest.removeEdge(e);
                    }
                    for (TreeEdge e : setPair.addSet) {
                        e.cost = e.cost - gain;
                        forest.addEdge(e);
                    }
                    t = forest.giveSingleTree();
                    stackMap.get(j).push(new myEvaluationResult(terminals, setPair));
                }
            }
        }
        return stackMap;
    }

    private static double scost(Graph g, HashSet<Node> terminals, int k) throws Exception {
        Tree Smt = bermanRamaiyer(g, terminals, k);
        return Smt.totalCost();
    }

    private static double cost(HashSet<TreeEdge> edgeSet) {
        double total = 0;
        for (TreeEdge e : edgeSet) {
            total += e.cost;
        }
        return total;
    }

    public static Tree bermanRamaiyer(Graph g, HashSet<Node> terminals, int k) throws Exception {
        System.out.println(terminals);
        if (terminals.size() == 1)
            return new Tree(terminals.iterator().next());
        if (terminals.size() == 2) 
        {
            Iterator<Node> it= terminals.iterator();
            Node n1=it.next();
            Node n2=it.next();
            return g.djikstra(n1, n2);
        }
        Tree M = trimmedMst(g, terminals);
        System.out.println(M.totalCost());
        // Evaluation Phase
        HashMap<Integer, Stack<myEvaluationResult>> stackMap = new HashMap<>();
        for (int j = 3; j <= k; j++) {
            stackMap.put(j, new Stack<>());
            HashSet<HashSet<Node>> sets = getXElementSubsets(terminals, j);
            for (HashSet<Node> nodeSet : sets) {
                if (!nodeSet.equals(terminals)) {
                    SetPair setPair = prepareChange(M, nodeSet, new SetPair());
                    double gain = cost(setPair.removeSet) - scost(g, nodeSet, k);
                    if (gain > 0) {
                        Forest forest = new Forest(M);
                        for (TreeEdge e : setPair.removeSet) {
                            forest.removeEdge(e);
                        }
                        for (TreeEdge e : setPair.addSet) {
                            e.cost = e.cost - gain;
                            forest.addEdge(e);
                        }
                        M = forest.giveSingleTree();
                        System.out.println(M.totalCost());
                        stackMap.get(j).push(new myEvaluationResult(terminals, setPair));
                    }
                }
            }
        }
        // Construction Phase
        Tree n = M;
        for (int j = k; j >= 3; j--) {
            while (!stackMap.get(j).empty()) {
                myEvaluationResult curr = stackMap.get(j).pop();
                Forest forest = new Forest(M);
                for (TreeEdge e : curr.addSet) {
                    forest.removeEdge(e);
                }
                for (TreeEdge e : curr.removeSet) {
                    forest.addEdge(e);
                }
                M = forest.giveSingleTree();
                System.out.println(M.totalCost());
                if (n.containsAllTreeEdges(curr.addSet)) {
                    forest = new Forest(n);
                    for (TreeEdge e : curr.addSet) {
                        forest.removeEdge(e);
                    }
                    for (TreeEdge e : bermanRamaiyer(g, curr.terminals, k).toEdgeSet()) {
                        forest.addEdge(e);
                    }
                    n = forest.giveSingleTree();
                    System.out.println(n.totalCost());
                } else {
                    for (TreeEdge e : curr.addSet) {
                        if (n.containsEdge(e)) {
                            TreeEdge f = minCostConnectingEdge(M, n, e, curr.terminals);
                            forest = new Forest(n);
                            forest.removeEdge(e);
                            forest.addEdge(f);
                            n = forest.giveSingleTree();
                        }
                    }
                }
            }

        }
        return n;
    }

    private static Tree constructionPhase(Graph g, Tree m, int k, HashMap<Integer, Stack<myEvaluationResult>> stackMap)
            throws Exception {
        Tree n = m;
        for (int j = k; k <= 3; k--) {
            while (!stackMap.get(j).empty()) {
                myEvaluationResult curr = stackMap.get(j).pop();
                Forest forest = new Forest(m);
                for (TreeEdge e : curr.addSet) {
                    forest.removeEdge(e);
                }
                for (TreeEdge e : curr.removeSet) {
                    forest.addEdge(e);
                }
                m = forest.giveSingleTree();
                if (n.containsAllTreeEdges(curr.addSet)) {
                    forest = new Forest(n);
                    for (TreeEdge e : curr.addSet) {
                        forest.removeEdge(e);
                    }
                    for (TreeEdge e : bermanRamaiyer(g, curr.terminals, k).toEdgeSet()) {
                        forest.addEdge(e);
                    }
                    n = forest.giveSingleTree();
                } else {
                    for (TreeEdge e : curr.addSet) {
                        if (n.containsEdge(e)) {
                            TreeEdge f = minCostConnectingEdge(m, n, e, curr.terminals);
                            forest = new Forest(n);
                            forest.removeEdge(e);
                            forest.addEdge(f);
                            n = forest.giveSingleTree();
                        }
                    }
                }
            }

        }
        return n;
    }

    // finds edge f element tree m of minimal cost such that Tree n - e + f connects
    // all terminals
    public static TreeEdge minCostConnectingEdge(Tree m, Tree n, TreeEdge e, HashSet<Node> terminals) throws Exception {
        Forest forest = new Forest(n);
        forest.removeEdge(e);
        double minCost = Double.POSITIVE_INFINITY;
        TreeEdge f = null;
        if (forest.isSetConnected(terminals))
            throw new Exception("no added edge needed");
        for (TreeEdge edge : m.toEdgeSet()) {
            if (edge.cost < minCost) {
                if (forest.wouldEdgeConnectSet(terminals, edge)) {
                    minCost = edge.cost;
                    f = edge;
                }
            }
        }
        if (f == null)
            throw new Exception("couldnt find valid Edge f");
        return f;
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

    public static Tree buildFromEdgeSet(HashSet<TreeEdge> edgeSet) {
        ArrayList<Tree> forest = new ArrayList<>();
        for (TreeEdge e : edgeSet) {
            Tree from = null;
            Tree to = null;
            if (forest.contains(e.from))
                from = forest.remove(forest.indexOf(e.from));
            else
                from = new Tree(e.from.node);
            if (forest.contains(e.to))
                to = forest.remove(forest.indexOf(e.to));
            else
                to = new Tree(e.to.node);
            to.cost = e.cost;
            from.children.add(to);
            forest.add(from);
        }
        if (forest.size() == 1)
            return forest.iterator().next();
        return null;
    }

    public static HashSet<HashSet<Node>> getXElementSubsets(HashSet<Node> in, int x) {
        return getXElementSubsets(x, new MyImmutableHashSet<Node>(in), new MyImmutableHashSet<Node>());
    }

    private static HashSet<HashSet<Node>> getXElementSubsets(int x, MyImmutableHashSet<Node> in,
            MyImmutableHashSet<Node> out) {
        HashSet<HashSet<Node>> setOfSets = new HashSet<>();
        if (out.getSize() == x) {
            setOfSets.add(out.getHashSet());
            return setOfSets;
        }
        for (Node n : in) {
            setOfSets.addAll(getXElementSubsets(x, in.remove(n), out.add(n)));
        }
        return setOfSets;
    }
}