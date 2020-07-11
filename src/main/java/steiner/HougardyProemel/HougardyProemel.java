package steiner.HougardyProemel;

import java.util.HashSet;

public class HougardyProemel {

    public static void main(String[] args) throws Exception {
        // double alphas[] = { 0.360, 0.264, 0.183, 0.114, 0.0053, 0 };
        for (MyGraph g : MyGraphFactory.getAllGraphs()) {
            MyTree t = kRGHalpha(g, g.getTerminalNodes(), 6, 1);
            System.out.println(t.totalCost() + "/" + g.optimal);
        }

    }

    public static Tree hougardyProemel(MyGraph g, int k, double[] alphas) throws Exception {

        // TODO

        return null;
    }

    public static MyTree kRGHalpha(MyGraph g, HashSet<Node> terminals, int k, double alpha) throws Exception {
        MyGraph workingG = g.copy();
        HashSet<Node> workingSet = new HashSet<>();
        for (Node t : terminals) {
            workingSet.add(t);
        }
        HashSet<MyTree> LIST = new HashSet<>();
        MyTree mst = workingG.MstApproximation(terminals);
        while (mst.totalCost() > 0) {
            double min = Double.POSITIVE_INFINITY;
            MyTree minTree = null;
            HashSet<MyTree> classK = getAllKRestrictedSteinerTrees(workingG, workingSet, k);
            for (MyTree b : classK) {
                MyGraph tmpContracted = workingG.copy();
                tmpContracted.contractSet(b.nodes);
                MyTree mstWithBContracted = tmpContracted.MstApproximation(workingSet);
                double contractedCost = mstWithBContracted.totalCost();
                double f = b.totalCost() + (alpha * loss(workingG, b, workingSet)) / (mst.totalCost() - contractedCost);
                if (f < min && min >= 0) {
                    min = f;
                    minTree = b;
                }
            }
            if (minTree == null) {
                int i = 0;
            }
            LIST.add(minTree);
            workingG.contractSet(minTree.nodes);
            workingSet.removeAll(minTree.getTerminalNodes());
            if (workingSet.isEmpty())
                break;
            mst = workingG.MstApproximation(workingSet);
        }
        HashSet<Node> allCombined = new HashSet<>();
        for (MyTree tree : LIST) {
            allCombined.addAll(tree.nodes);
        }
        allCombined.addAll(terminals);
        return g.MstApproximation(allCombined);
    }

    private static HashSet<MyTree> getAllKRestrictedSteinerTrees(MyGraph g, HashSet<Node> terminals, int k)
            throws Exception {
        HashSet<MyTree> out = new HashSet<>();
        for (int x = 2; x <= k; x++) {
            for (HashSet<Node> set : getXElementSubsets(terminals, x)) {
                MyTree smt = g.MstApproximation(set);
                if (smt.isFull())
                    out.add(smt);
            }
        }
        return out;
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

    public static double loss(MyGraph g, MyTree t, HashSet<Node> terminals) throws Exception {
        MyForest f = g.minimumSpanningForest(t.nodes, g.getShortestPaths());
        return f.totalCost();
    }

}