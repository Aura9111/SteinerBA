package steiner.BermanRamaiyer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class BermanRamaiyer {

    public static void main(String[] args) throws Exception {
        Graph g = GraphFactory.g043();

        Tree Smt = helpfulFunctions.smt(g, g.getAllTerminalNodes());
        Smt.printGraph("smt");
        System.out.println(Smt.totalCost());
        Tree mst = helpfulFunctions.mst(g, g.getAllTerminalNodes());
        mst.printGraph("mst");
        System.out.println(mst.totalCost());
        Tree trim = helpfulFunctions.trimmedMst(g, g.getAllTerminalNodes());
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

}