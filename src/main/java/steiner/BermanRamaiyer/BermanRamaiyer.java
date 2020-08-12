package steiner.BermanRamaiyer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import steiner.HougardyProemel.HougardyProemel;

public class BermanRamaiyer {

    public static void main(String[] args) throws Exception {
        Graph g = GraphFactory.g041();
        // for (Graph g : GraphFactory.allGraphs()) {
        Tree t = bermanRamaiyer(g, g.getAllTerminalNodes(), 6);
        t.printGraph(g.path + "default");
        HashSet<Edge> set = new HashSet<>();
        HashSet<Edge> treeSet = t.toEdgeSet();
        for (Edge e : treeSet) {
            double upper = upperBoundForBinarySearch(e, g);
            double crit = binarySearch(e, g, upper);
            System.out.println("ogCost: " + e.cost + " -> crit: " + crit);
            Edge criticalEdge = new Edge(e.getName(), e.first, e.second, crit);
            set.add(criticalEdge);
        //}
        HougardyProemel.printHashSetOfEdge(g, set, t.node.id+1);
    }

    }

    public static double upperBoundForBinarySearch(Edge edge, Graph original) throws Exception {
        Graph g = original.copy();
        double out = edge.cost;
        Tree t;
        do {
            out = 2 * out;
            g.removeEdge(edge);
            if (g.containsEdge(edge))
                System.out.println("rip");
            g.addEdge(edge.first.name, edge.second.name, out);
            t = bermanRamaiyer(g, g.getAllTerminalNodes(), 6);
        } while (t.containsEdge(edge));
        return out;
    }

    public static double binarySearch(Edge e, Graph original, double upper) throws Exception {
        Graph g = original.copy();
        double l = e.cost;
        double r = upper;
        while (l < r) {
            double m = Math.floor((l + r) / 2);
            g.removeEdge(e);
            g.addEdge(e.first.name, e.second.name, m);
            if (bermanRamaiyer(g, g.getAllTerminalNodes(), 6).containsEdge(e))
                l = m + 1;
            else
                r = m;
        }
        // l==r==the price at which edge isnt included anymore
        return l - 1; // <- this is the price at which it is still included
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
        Tree M = helpfulFunctions.smt(g, terminals);
        double ogCost = M.totalCost();
        // Evaluation Phase
        HashMap<Integer, Stack<Triple<HashSet<Node>, HashSet<TreeEdge>, HashSet<TreeEdge>>>> stackMap = new HashMap<>();
        for (int j = 3; j <= k; j++) {
            stackMap.put(j, new Stack<>());
            HashSet<HashSet<Node>> sets = helpfulFunctions.getXElementSubsets(terminals, j);
            for (HashSet<Node> nodeSet : sets) {
                Pair<HashSet<TreeEdge>, HashSet<TreeEdge>> pair = prepareChange(M, nodeSet);
                HashSet<TreeEdge> removeSet = pair.first;
                HashSet<TreeEdge> addSet = pair.second;
                double gain = helpfulFunctions.cost(removeSet) - helpfulFunctions.scost(g, nodeSet, k);
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
                    Tree smt = helpfulFunctions.smt(g, nodeSet);
                    for (TreeEdge e : smt.toTreeEdgeSet()) {
                        forest.addEdgeWithNewNode(e);
                    }
                    n = forest.giveSingleTree();
                } else {
                    for (TreeEdge e : addSet) {
                        if (n.containsEdge(e)) {
                            TreeEdge f = helpfulFunctions.minCostConnectingEdge(M, n, e, nodeSet);
                            forest = new Forest(n);
                            forest.removeEdge(e);
                            forest.addEdge(f);
                            n = forest.giveSingleTree();
                        }
                    }
                }
            }

        }
        // System.out.println(ogCost + "->" + n.totalCost());
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
}