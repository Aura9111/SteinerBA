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
        Graph g = GraphFactory.g043();

        Tree Smt = smt(g, g.getAllTerminalNodes());
        Smt.printGraph("smt");
        System.out.println(Smt.totalCost());
        Tree mst = mst(g, g.getAllTerminalNodes());
        mst.printGraph("mst");
        System.out.println(mst.totalCost());
        Tree trim = trimmedMst(g, g.getAllTerminalNodes());
        trim.printGraph("trim");
        System.out.println(trim.totalCost());
        Tree berman = bermanRamaiyer(g, g.getAllTerminalNodes(), g.getAllTerminalNodes().size());
        berman.printGraph("berman");
        System.out.println(berman.totalCost());
    }

    public static Tree bermanRamaiyer(Graph g, HashSet<Node> terminals, int k) throws Exception {
        if (terminals.size() == 1)
            return new Tree(terminals.iterator().next());
        if (terminals.size() == 2) {
            Iterator<Node> it = terminals.iterator();
            Node n1 = it.next();
            Node n2 = it.next();
            return g.djikstra(n1.getName(), n2.getName());
        }
        System.out.println(g.path);
        Tree M = smt(g, terminals);
        double ogCost = M.totalCost();
        // Evaluation Phase
        HashMap<Integer, Stack<Triple<HashSet<Node>, HashSet<TreeEdge>, HashSet<TreeEdge>>>> stackMap = new HashMap<>();
        for (int j = 3; j <= k; j++) {
            stackMap.put(j, new Stack<>());
            HashSet<HashSet<Node>> sets = getXElementSubsets(terminals, j);
            for (HashSet<Node> nodeSet : sets) {
                Pair<HashSet<TreeEdge>, HashSet<TreeEdge>> pair = prepareChange(M, nodeSet);
                HashSet<TreeEdge> removeSet = pair.first;
                HashSet<TreeEdge> addSet = pair.second;
                double gain = cost(removeSet) - scost(g, nodeSet, k);
                // System.out.println("gain: " + gain + "\ncostRemove: " + cost(removeSet));
                if (gain > 0) {
                    Forest forest = new Forest(M);
                    for (TreeEdge e : removeSet) {
                        forest.removeEdge(e);
                    }
                    for (TreeEdge e : addSet) {
                        e.cost = e.cost - gain;
                        forest.addEdge(e);
                    }
                    M = forest.giveSingleTree();
                    stackMap.get(j).push(new Triple<HashSet<Node>, HashSet<TreeEdge>, HashSet<TreeEdge>>(nodeSet,
                            removeSet, addSet));
                }
            }
        }
        // Construction Phase
        Tree n = M.copy();
        for (int j = k; j >= 3; j--) {
            while (!stackMap.get(j).empty()) {
                Triple<HashSet<Node>, HashSet<TreeEdge>, HashSet<TreeEdge>> triple = stackMap.get(j).pop();
                HashSet<Node> nodeSet = triple.first;
                HashSet<TreeEdge> removeSet = triple.second;
                HashSet<TreeEdge> addSet = triple.third;
                Forest forest = new Forest(M);
                for (TreeEdge e : addSet) {
                    forest.removeEdge(e);
                }
                for (TreeEdge e : removeSet) {
                    forest.addEdge(e);
                }
                M = forest.giveSingleTree();
                if (n.containsAllTreeEdges(addSet)) {
                    forest = new Forest(n);
                    for (TreeEdge e : addSet) {
                        forest.removeEdge(e);
                    }
                    Tree smt = smt(g, nodeSet);
                    for (TreeEdge e : smt.toEdgeSet()) {
                        forest.addEdgeSmt(e);
                    }
                    n = forest.giveSingleTree();
                } else {
                    for (TreeEdge e : addSet) {
                        if (n.containsEdge(e)) {
                            TreeEdge f = minCostConnectingEdge(M, n, e, nodeSet);
                            forest = new Forest(n);
                            forest.removeEdge(e);
                            forest.addEdge(f);
                            n = forest.giveSingleTree();
                        }
                    }
                }
            }

        }
        System.out.println(ogCost + "->" + n.totalCost());
        return n;
    }

    public static Pair<HashSet<TreeEdge>, HashSet<TreeEdge>> prepareChange(Tree t, HashSet<Node> terminals)
            throws IOException {
        if (terminals.size() == 1)
            return new Pair<HashSet<TreeEdge>, HashSet<TreeEdge>>(new HashSet<>(), new HashSet<>());
        TreeEdge e = t.getMaxCostConnectingEdge(terminals);
        HashSet<Node> terminalsTo = t.splitTermOnEdge(e, terminals);
        HashSet<Node> terminalsFrom = new HashSet<>();
        for (Node n : terminals) {
            if (!terminalsTo.contains(n))
                terminalsFrom.add(n);
        }
        Pair<HashSet<TreeEdge>, HashSet<TreeEdge>> resultFrom = prepareChange(t, terminalsFrom);
        Pair<HashSet<TreeEdge>, HashSet<TreeEdge>> resultTo = prepareChange(t, terminalsTo);
        Node nFrom = terminalsFrom.iterator().next();
        Node nTo = terminalsTo.iterator().next();
        TreeEdge f = new TreeEdge(t, nFrom, nTo, e.cost);
        resultFrom.first.addAll(resultTo.first);
        resultFrom.second.addAll(resultTo.second);
        resultFrom.first.add(e);
        resultFrom.second.add(f);
        return resultFrom;
    }

    public static Tree smt(Graph g, HashSet<Node> terminals) throws Exception {

        Tree t = mst(subMetricClosure(g, terminals));
        t = unMetricTree(g, t);
        return t;
    }

    public static Graph metricClosure(Graph g) {
        Graph gOut = new Graph("metricClosureOf" + g.path);
        for (Node n : g.getNodes().values()) {
            gOut.addNode(n.getName(), n.isTerminal());
        }
        for (Node n1 : g.getNodes().values()) {
            for (Node n2 : g.getNodes().values()) {
                if (!n1.equals(n2)) {
                    gOut.addEdge(n1.getName(), n2.getName(), g.djikstra(n1.getName(), n2.getName()).totalCost());
                }
            }
        }
        return gOut;
    }

    public static Graph subMetricClosure(Graph g, HashSet<Node> set) {
        Graph gOut = new Graph("subMetricOf" + g.path + set);
        for (Node n : set) {
            gOut.addNode(n.getName(), n.isTerminal());
        }
        for (Node n1 : set) {
            for (Node n2 : set) {
                if (!n1.equals(n2)) {
                    gOut.addEdge(n1.getName(), n2.getName(), g.djikstra(n1.getName(), n2.getName()).totalCost());
                }
            }
        }
        return gOut;
    }

    public static Tree unMetricTree(Graph g, Tree t) {
        Tree out = new Tree(t.node);
        for (Tree child : t.children) {
            Tree djikstra = g.djikstra(t.node.getName(), child.node.getName());
            double cost = djikstra.findNode(child.node).cost;
            Tree leadingToChild = djikstra.children.iterator().next();
            Tree rec = unMetricTree(g, child);
            rec.changeCostOfN(child.node, cost);
            HashSet<Node> dublicates = leadingToChild.getNodes();
            dublicates.retainAll(rec.getNodes());
            dublicates.remove(child.node);
            if (!dublicates.isEmpty()) {
                for (Node n : dublicates) {
                    Tree inRec = rec.findNode(n);
                    for (Tree c : inRec.children) {
                        leadingToChild.addChild(n, c, c.cost);
                    }
                    rec = rec.removeNode(n);
                }
            }
            Tree both = leadingToChild.replace(child.node, rec);
            dublicates = out.getNodes();
            dublicates.retainAll(both.getNodes());
            dublicates.remove(child.node);
            dublicates.remove(t.node);
            if (!dublicates.isEmpty()) {
                for (Node n : dublicates) {
                    Tree x = both.findNode(n);
                    for (Tree c : x.children) {
                        out.addChild(n, c, c.cost);
                    }
                    both = both.removeNode(n);
                }
            }
            if (both != null)
                out.addChild(t.node, both, both.cost);
        }
        return out;
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

    public static Tree mst(Graph g, HashSet<Node> terminals) throws Exception {
        if (g.numberOfComponents() > 1)
            throw new Exception("Graph unfit");
        HashSet<Tree> forest = new HashSet<>();
        for (Node n : terminals) {
            forest.add(new Tree(n));
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
                // first but not second
                if (nodeToComp.containsKey(e.first) && !nodeToComp.containsKey(e.second)) {
                    if (compToEdge.get(nodeToComp.get(e.first)) == null)
                        compToEdge.put(nodeToComp.get(e.first), e);
                    else if (e.getWeight() < compToEdge.get(nodeToComp.get(e.first)).getWeight())
                        compToEdge.put(nodeToComp.get(e.first), e);
                }
                // second but not first
                else if (nodeToComp.containsKey(e.second) && !nodeToComp.containsKey(e.first)) {
                    if (compToEdge.get(nodeToComp.get(e.second)) == null)
                        compToEdge.put(nodeToComp.get(e.second), e);
                    else if (e.getWeight() < compToEdge.get(nodeToComp.get(e.second)).getWeight())
                        compToEdge.put(nodeToComp.get(e.second), e);
                }
                // both
                else if (nodeToComp.containsKey(e.first) && nodeToComp.containsKey(e.second)) {
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
            }
            HashSet<Edge> alreadyDone = new HashSet<>();
            for (String c : compToEdge.keySet()) {
                if (compToEdge.get(c) != null) {
                    Edge e = compToEdge.get(c);
                    if (!alreadyDone.contains(e)) {
                        String root1 = nodeToComp.containsKey(e.first) ? nodeToComp.get(e.first)
                                : nodeToComp.get(e.second);
                        String root2 = nodeToComp.containsKey(e.first) && nodeToComp.containsKey(e.second)
                                ? nodeToComp.get(e.second)
                                : null;
                        Tree t1 = null;
                        Tree t2 = null;
                        for (Tree t : forest) {
                            if (t.node.getName().equals(root1))
                                t1 = t;
                            if (t.node.getName().equals(root2))
                                t2 = t;
                        }
                        int t1Size = t1.getNodes().size();
                        forest.remove(t1);
                        int t2Size;
                        if (t2 != null) {
                            t2Size = t2.getNodes().size();
                            forest.remove(t2);
                            t2 = t2.makeRoot(e.second);
                        } else {
                            t2Size = 1;
                            t2 = nodeToComp.containsKey(e.first) ? new Tree(e.second) : new Tree(e.first);
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

    private static double scost(Graph g, HashSet<Node> terminals, int k) throws Exception {
        Tree Smt = smt(g, terminals);
        return Smt.totalCost();
    }

    private static double cost(HashSet<TreeEdge> edgeSet) {
        double total = 0;
        for (TreeEdge e : edgeSet) {
            total += e.cost;
        }
        return total;
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