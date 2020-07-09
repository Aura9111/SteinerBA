package steiner;

import java.util.HashSet;

public class HougardyProemel {

    public static void main(String[] args) throws Exception {
        // double alphas[] = { 0.360, 0.264, 0.183, 0.114, 0.0053, 0 };
        for (MyGraph g : MyGraphFactory.getAllGraphs()) {
            Tree t = kRGHalpha(g, g.getTerminalNodes(), 6, 0);
            System.out.println(t.totalCost() + "/" + g.optimal);
        }

    }

    public static Tree hougardyProemel(Graph g, int k, double[] alphas) throws Exception {

        // TODO

        return null;
    }

    public static Tree kRGHalpha(MyGraph g, HashSet<Node> terminals, int k, double alpha) throws Exception {
        MyGraph workingG = g.copy();
        HashSet<Node> workingSet = new HashSet<>();
        for (Node t : terminals) {
            workingSet.add(t);
        }
        HashSet<Tree> LIST = new HashSet<>();
        Tree mst = workingG.MSTAlgorithm(terminals);
        while (mst.totalCost() > 0) {
            double min = Double.POSITIVE_INFINITY;
            Tree minTree = null;
            HashSet<Tree> classK = getAllKRestrictedSteinerTrees(workingG, workingSet, k);
            for (Tree b : classK) {
                MyGraph tmpContracted = workingG.copy();
                tmpContracted.contractSet(b.getNodes());
                Tree mstWithBContracted = tmpContracted.MSTAlgorithm(workingSet);
                double contractedCost = mstWithBContracted.totalCost();
                double f = b.totalCost()
                        + (alpha * helpfulFunctions.loss(workingG, b, workingSet)) / (mst.totalCost() - contractedCost);
                if (f < min && min >= 0) {
                    min = f;
                    minTree = b;
                }
            }
            if (minTree == null) {
                int i = 0;
            }
            LIST.add(minTree);
            workingG.contractSet(minTree.getNodes());
            workingSet.removeAll(minTree.getAllTerminalNodes());
            if (workingSet.isEmpty())
                break;
            mst = workingG.MSTAlgorithm(workingSet);
        }
        HashSet<Node> allCombined = new HashSet<>();
        for (Tree tree : LIST) {
            allCombined.addAll(tree.getNodes());
        }
        allCombined.addAll(terminals);
        return g.MSTAlgorithm(allCombined);
    }

    private static HashSet<Tree> getAllKRestrictedSteinerTrees(MyGraph g, HashSet<Node> terminals, int k)
            throws Exception {
        HashSet<Tree> out = new HashSet<>();
        /*
         * for(Node n: terminals){ out.add(new Tree(n)); }
         */
        for (int x = 2; x <= k; x++) {
            for (HashSet<Node> set : helpfulFunctions.getXElementSubsets(terminals, x)) {
                Tree smt = g.MSTAlgorithm(set);
                if (smt.isFull())
                    out.add(smt);
            }
        }
        return out;
    }

}