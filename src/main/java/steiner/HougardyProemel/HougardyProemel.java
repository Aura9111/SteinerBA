package steiner.HougardyProemel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class HougardyProemel {

    public static void main(String[] args) throws Exception {
        System.out.println(MyGraphFactory.makeMyGraphMethods("041").shortestPathString());
        /*
         * double[] alphas = { 0.698, 0.248, 0 }; for (MyGraph g :
         * MyGraphFactory.getAllGraphs()) { MyTree t = hougardyProemel(g, alphas, 3);
         * System.out.println(t.totalCost() + "/" + g.optimal); t.printGraph("hougardy"
         * + g.name); }
         */
    }

    public static void printHashSetOfEdge(steiner.BermanRamaiyer.Graph g, HashSet<steiner.BermanRamaiyer.Edge> set)
            throws IOException {
        HashSet<Edge> niceSet = new HashSet<>();
        for (steiner.BermanRamaiyer.Edge e : set) {
            niceSet.add(new Edge(new Node(e.first.id + 1, e.first.isTerminal()),
                    new Node(e.second.id + 1, e.second.isTerminal()), e.cost));
        }
        new MyTree(niceSet).printGraph(g.path + "crit");
    }

    public static MyTree hougardyProemel(MyGraph g, double[] alphas, int k) throws Exception {
        MyGraph og = g.copy();
        HashSet<Node> set = g.getTerminalNodes();
        for (double alpha : alphas) {
            MyForest result = kRGHalpha(g, set, k, alpha);
            set.addAll(result.toNodeSet());
        }
        return og.MstApproximation(set);
    }

    public static MyForest kRGHalpha(MyGraph g, HashSet<Node> terminals, int k, double alpha) throws Exception {
        HashSet<MyTree> LIST = new HashSet<>();
        MyTree mst = g.MstApproximation(terminals);
        while (mst.totalCost() > 0) {
            double min = Double.POSITIVE_INFINITY;
            MyTree minTree = null;
            HashSet<MyTree> classK = getAllKRestrictedTrees(g, terminals, k);
            for (MyTree b : classK) {
                MyGraph tmpContracted = g.copy();
                tmpContracted.contractSet(b.getTerminalNodes(terminals));
                MyTree mstWithBContracted = tmpContracted.MstApproximation(terminals);
                double m = mst.totalCost() - mstWithBContracted.totalCost();
                if (m < 0)
                    throw new Exception("constructing nodes somehow increased the treecost");
                double f = b.totalCost() + (alpha * loss(b, terminals)) / m;
                if (f < min) {
                    min = f;
                    minTree = b;
                }
            }
            if (minTree == null)
                minTree = mst;
            LIST.add(minTree);
            g.contractSet(minTree.getTerminalNodes(terminals));
            mst = g.MstApproximation(terminals);
        }

        MyForest allCombined = new MyForest();
        for (MyTree tree : LIST) {
            for (Edge e : tree.edges) {
                allCombined.addEdge(e);
            }
        }
        return allCombined;
    }

    private static HashSet<MyTree> getAllKRestrictedTrees(MyGraph g, HashSet<Node> terminals, int k) throws Exception {
        HashSet<MyTree> out = new HashSet<>();
        for (int i = k; i > 1; i--) {
            for (HashSet<Node> set : getXElementSubsets(terminals, i)) {
                MyTree t = g.fullTreeApprox(terminals, set);
                if (t != null)
                    out.add(t);
            }
        }
        return out;
    }

    /*
     * private static HashSet<MyTree> getAllKRestrictedSteinerTrees(MyGraph g,
     * HashSet<Node> terminals, int k) throws Exception { HashMap<HashSet<Node>,
     * MyTree> map = new HashMap<>(); for (int i = k; i > 1; i--) { for
     * (HashSet<Node> set : getXElementSubsets(terminals, i)) { if
     * (!map.containsKey(set)) { MyTree mst = g.MstApproximation(set); if
     * (mst.isFull(terminals)) { map.put(set, mst); } else { MyForest f = new
     * MyForest(mst); f.splitIntoFullComponents(terminals); for (MyTree t : f.trees)
     * { HashSet<Node> subset = new HashSet<>(); subset.addAll(t.nodes);
     * subset.retainAll(terminals); if (!map.containsKey(subset)) { map.put(subset,
     * t); } else if (map.get(subset).totalCost() > t.totalCost()) map.put(subset,
     * t);
     * 
     * } } } } } HashSet<MyTree> out = new HashSet<>(); out.addAll(map.values());
     * return out; }
     */

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

    public static double loss(MyTree t, HashSet<Node> terminals) throws Exception {
        MyForest forest = new MyForest();
        for (Node n : t.nodes) {
            forest.trees.add(new MyTree(n));
        }
        ArrayList<Edge> sortedEdges = sortEdges(t.edges);
        Iterator<Edge> it = sortedEdges.iterator();
        boolean done = false;
        while (!done && it.hasNext()) {
            Edge e = it.next();
            int i = 0;
            int indexFirst = -1;
            int indexSecond = -1;
            for (MyTree tree : forest.trees) {
                if (tree.nodes.contains(e.first))
                    indexFirst = i;
                if (tree.nodes.contains(e.second))
                    indexSecond = i;
                i++;
            }
            if (indexFirst >= 0 && indexSecond >= 0 && indexFirst != indexSecond) {
                forest.addEdge(e);
            }
            done = true;
            for (Node n : t.getSteinerNodes(terminals)) {
                MyTree tmp = forest.getTreeWithNode(n);
                if (tmp.getTerminalNodes(terminals).isEmpty())
                    done = false;
            }
        }
        return forest.totalCost();
    }

    public static ArrayList<Edge> sortEdges(HashSet<Edge> set) {
        ArrayList<Edge> out = new ArrayList<>();
        for (Edge e : set) {
            if (out.isEmpty())
                out.add(e);
            int index = -1;
            for (Edge comp : out) {
                if (comp.cost >= e.cost) {
                    index = out.indexOf(comp);
                    break;
                }
            }
            if (index < 0)
                out.add(e);
            else
                out.add(index, e);
        }
        return out;
    }
}