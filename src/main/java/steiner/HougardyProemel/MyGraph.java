package steiner.HougardyProemel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyGraph {
    public String name;
    public Node[] nodes;
    public double[][] edges;
    public HashMap<Pair<Node, Node>, Pair<HashSet<Edge>, Double>> shortestPathMap;
    public double optimal;

    public MyGraph(String name, Node[] nodes, double[][] edges,
            HashMap<Pair<Node, Node>, Pair<HashSet<Edge>, Double>> shortestPathMap, double optimal) {
        this.name = name;
        this.nodes = nodes;
        this.edges = edges;
        this.shortestPathMap = shortestPathMap;
        this.optimal = optimal;
    }

    public MyGraph(String name, int size) {
        this.name = name;
        nodes = new Node[size];
        edges = new double[size][size];
        shortestPathMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                edges[i][j] = i == j ? 0 : Double.POSITIVE_INFINITY;
            }
        }
    }

    public MyGraph(MyGraph g, HashSet<Node> set) {
        this(g.name + set, g.nodes.length);
        int size = g.nodes.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                edges[i][j] = i == j ? 0 : Double.POSITIVE_INFINITY;
            }
        }
        for (Node n : set) {
            nodes[n.id] = n.copy();
            for (Edge e : g.getEdgesOfNode(n.id)) {
                int n1 = e.first.id;
                int n2 = e.second.id;
                edges[n1][n2] = e.cost;
                edges[n2][n1] = e.cost;
                HashSet<Edge> tmp = new HashSet<>();
                tmp.add(e);
                shortestPathMap.put(new Pair<Node, Node>(e.first, e.second),
                        new Pair<HashSet<Edge>, Double>(tmp, e.cost));
                shortestPathMap.put(new Pair<Node, Node>(e.second, e.first),
                        new Pair<HashSet<Edge>, Double>(tmp, e.cost));
            }
        }
        computeShortestPaths();
    }

    private HashSet<Edge> getEdgesOfNode(int nodeId) {
        HashSet<Edge> out = new HashSet<>();
        for (int i = 0; i < nodes.length; i++) {
            if (edges[nodeId][i] < Double.POSITIVE_INFINITY && i != nodeId)
                out.add(new Edge(nodes[nodeId], nodes[i], edges[nodeId][i]));
        }
        return out;
    }

    public HashSet<Node> getNodes() {
        HashSet<Node> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null)
                out.add(nodes[n1]);
        }
        return out;
    }

    public HashSet<Node> getTerminalNodes() {
        HashSet<Node> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null) {
                if (nodes[n1].terminal)
                    out.add(nodes[n1]);
            }
        }
        return out;
    }

    /*
     * public HashSet<Node> getSteinerNodes() { HashSet<Node> out = new HashSet<>();
     * for (int n1 = 0; n1 < nodes.length; n1++) { if (nodes[n1] != null) { if
     * (!nodes[n1].terminal) out.add(nodes[n1]); } } return out; }
     */

    /*
     * public Tree djikstra(Integer n1, Integer n2) { HashSet<Integer> todo = new
     * HashSet<>(); HashMap<Integer, Pair<Integer, Double>> map = new HashMap<>();
     * map.put(n1, new Pair<Integer, Double>(null, 0.0)); todo.add(n1); while
     * (!todo.isEmpty()) { HashSet<Integer> tmp = new HashSet<>(); for (Integer n :
     * todo) { for (Node node : getNodes()) { int i = node.id; double newCost =
     * map.get(n).second + edges[n][i]; if (map.containsKey(i)) { if
     * (map.get(i).second > newCost) { map.put(i, new Pair<Integer, Double>(n,
     * newCost)); } } else { map.put(i, new Pair<Integer, Double>(n, newCost));
     * tmp.add(i); } } } todo = tmp; } int i = n2; Integer j = null; Tree t1 = new
     * Tree(nodes[i]); if (!map.containsKey(n2)) return null; // n2 is in different
     * component-> unreachable if (map.get(n2).first != null) j = map.get(n2).first;
     * while (j != null) { Tree t2 = new Tree(nodes[j]); double cost =
     * map.get(i).second - map.get(j).second; t2.addChild(nodes[j], t1, cost); t1 =
     * t2; i = j; j = map.get(j).first; } return t1; }
     */

    public void computeShortestPaths() {
        for (Node n : nodes) {
            shortestPathMap.put(new Pair<Node, Node>(n, n), new Pair<HashSet<Edge>, Double>(new HashSet<>(), 0.0));
        }
        for (Node k : getNodes()) {
            for (Node i : getNodes()) {
                for (Node j : getNodes()) {
                    Pair<HashSet<Edge>, Double> pairIK = shortestPathMap.get(new Pair<Node, Node>(i, k));
                    Pair<HashSet<Edge>, Double> pairKJ = shortestPathMap.get(new Pair<Node, Node>(k, j));
                    Pair<HashSet<Edge>, Double> pairIJ = shortestPathMap.get(new Pair<Node, Node>(i, j));
                    double ik = pairIK == null ? Double.POSITIVE_INFINITY : pairIK.second;
                    double kj = pairKJ == null ? Double.POSITIVE_INFINITY : pairKJ.second;
                    double ij = pairIJ == null ? Double.POSITIVE_INFINITY : pairIJ.second;
                    if (ik + kj < ij) {
                        HashSet<Edge> newPath = new HashSet<>();
                        newPath.addAll(pairIK.first);
                        newPath.addAll(pairKJ.first);
                        shortestPathMap.put(new Pair<Node, Node>(i, j),
                                new Pair<HashSet<Edge>, Double>(newPath, ik + kj));
                        shortestPathMap.put(new Pair<Node, Node>(j, i),
                                new Pair<HashSet<Edge>, Double>(newPath, ik + kj));
                    }
                }
            }
        }
    }

    /*
     * public Tree MSTAlgorithm(HashSet<Node> set) throws Exception { return
     * MSTAlgorithm(set, getShortestPaths()); }
     */

    /*
     * public Tree MSTAlgorithm(HashSet<Node> set, HashSet<Edge> edges) throws
     * Exception { Forest f = kruskal(set, edges); if (f.size() > 1) throw new
     * Exception("cant construct mst. forest has " + f.size() + " components"); Tree
     * t = f.giveSingleTree(); t = unMetricTree(t); if
     * (!t.getSteinerNodes().isEmpty()) { t =
     * t.makeRoot(t.getSteinerNodes().iterator().next()); } return t; }
     */

    public ArrayList<Edge> sortEdges(HashSet<Edge> set) {
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

    public MyForest minimumSpanningForest(HashSet<Node> nodeSet, HashSet<Edge> edgeSet) throws Exception {
        MyForest forest = new MyForest();
        for (Node n : nodeSet) {
            forest.trees.add(new MyTree(n));
        }
        ArrayList<Edge> sortedEdges = sortEdges(edgeSet);
        Iterator<Edge> it = sortedEdges.iterator();
        while (forest.size() > 1 && it.hasNext()) {
            Edge e = it.next();
            int i = 0;
            int indexFirst = -1;
            int indexSecond = -1;
            for (MyTree t : forest.trees) {
                if (t.nodes.contains(e.first))
                    indexFirst = i;
                if (t.nodes.contains(e.second))
                    indexSecond = i;
                i++;
            }
            if (indexFirst >= 0 && indexSecond >= 0 && indexFirst != indexSecond) {
                for (Edge edge : shortestPathMap.get(new Pair<Node, Node>(e.first, e.second)).first)
                    forest.addEdge(edge);
            }
        }
        return forest;
    }

    public MyTree MstApproximation(HashSet<Node> terminals) throws Exception {
        MyForest f = minimumSpanningForest(terminals, getShortestPaths());
        if (f.trees.size() == 1) {
            MyTree t = f.trees.iterator().next();
            if (!t.getSteinerNodes(terminals).isEmpty())
                t.makeRoot(t.getSteinerNodes(terminals).iterator().next());
            return t;
        }
        return null;
    }

    public MyTree fullTreeApprox(HashSet<Node> terminals, HashSet<Node> nodeSet) throws Exception {
        MyForest f = fullForestApprox(terminals, nodeSet);
        if (f.trees.size() == 1)
            return f.giveSingleMyTree();
        else
            return null;
    }

    public MyForest fullForestApprox(HashSet<Node> terminals, HashSet<Node> nodeSet) {
        MyForest forest = new MyForest();
        for (Node n : nodeSet) {
            forest.trees.add(new MyTree(n));
        }
        ArrayList<Edge> sortedEdges = sortEdges(getShortestPaths());
        Iterator<Edge> it = sortedEdges.iterator();
        while (forest.size() > 1 && it.hasNext()) {
            Edge e = it.next();
            int i = 0;
            int indexFirst = -1;
            int indexSecond = -1;
            for (MyTree t : forest.trees) {
                if (t.nodes.contains(e.first))
                    indexFirst = i;
                if (t.nodes.contains(e.second))
                    indexSecond = i;
                i++;
            }
            if (indexFirst >= 0 && indexSecond >= 0 && indexFirst != indexSecond) {
                boolean makesTermInner = false;
                for (Edge edge : shortestPathMap.get(new Pair<Node, Node>(e.first, e.second)).first) {
                    if (nodeSet.contains(edge.first) && forest.degree(edge.first) > 0)
                        makesTermInner = true;
                    if (nodeSet.contains(edge.second) && forest.degree(edge.second) > 0)
                        makesTermInner = true;
                    if (terminals.contains(edge.first) && terminals.contains(edge.second) && e.cost > 0)
                        makesTermInner = true;
                }
                if (!makesTermInner) {
                    for (Edge edge : shortestPathMap.get(new Pair<Node, Node>(e.first, e.second)).first)
                        forest.addEdge(edge);
                    it = sortedEdges.iterator();
                }
            }
        }
        return forest;
    }
    /*
     * public HashSet<Edge> getEdges() { HashSet<Edge> out = new HashSet<>(); for
     * (int n1 = 0; n1 < nodes.length; n1++) { if (nodes[n1] != null) { for (int n2
     * = n1; n2 < nodes.length; n2++) { if (nodes[n2] != null) { if (edges[n1][n2]
     * != Double.POSITIVE_INFINITY) { out.add(new Edge(nodes[n1], nodes[n2],
     * edges[n1][n2])); } } } } } return out; }
     */

    public void contractSet(HashSet<Node> set) {
        for (Node n1 : set) {
            for (Node n2 : set) {
                edges[n1.id][n2.id] = 0;
                edges[n2.id][n1.id] = 0;
                Edge e = new Edge(n1, n2, 0);
                HashSet<Edge> tmp = new HashSet<>();
                tmp.add(e);
                shortestPathMap.put(new Pair<Node, Node>(e.first, e.second),
                        new Pair<HashSet<Edge>, Double>(tmp, e.cost));
                shortestPathMap.put(new Pair<Node, Node>(e.second, e.first),
                        new Pair<HashSet<Edge>, Double>(tmp, e.cost));
            }
        }
        computeShortestPaths();
    }

    public HashSet<Edge> getShortestPaths() {
        HashSet<Edge> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null) {
                for (int n2 = n1; n2 < nodes.length; n2++) {
                    if (nodes[n2] != null) {
                        if (shortestPathMap.get(new Pair<Node, Node>(nodes[n1], nodes[n2])) != null) {
                            out.add(new Edge(nodes[n1], nodes[n2],
                                    shortestPathMap.get(new Pair<Node, Node>(nodes[n1], nodes[n2])).second));
                        }
                    }
                }
            }
        }
        return out;
    }

    public void addNode(Node node) {
        nodes[node.id] = node;
    }

    public void addEdge(Edge e) {
        edges[e.first.id][e.second.id] = e.cost;
        edges[e.second.id][e.first.id] = e.cost;
        HashSet<Edge> tmp = new HashSet<>();
        tmp.add(e);
        shortestPathMap.put(new Pair<Node, Node>(e.first, e.second), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
        shortestPathMap.put(new Pair<Node, Node>(e.second, e.first), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
        computeShortestPaths();
    }

    public void addEdgeNoUpdate(Edge e) {
        edges[e.first.id][e.second.id] = e.cost;
        edges[e.second.id][e.first.id] = e.cost;
        HashSet<Edge> tmp = new HashSet<>();
        tmp.add(e);
        shortestPathMap.put(new Pair<Node, Node>(e.first, e.second), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
        shortestPathMap.put(new Pair<Node, Node>(e.second, e.first), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
    }

    public MyGraph copy() {
        HashMap<Pair<Node, Node>, Pair<HashSet<Edge>, Double>> newShortestPathMap = new HashMap<>();
        double[][] newEdges = new double[nodes.length][nodes.length];
        Node[] newNodes = new Node[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            newNodes[i] = nodes[i].copy();
            for (int j = 0; j < nodes.length; j++) {
                newEdges[i][j] = edges[i][j];
                Pair<Node, Node> nodePair = new Pair<>(nodes[i], nodes[j]);
                Pair<HashSet<Edge>, Double> resultPair = shortestPathMap.get(nodePair);
                if (resultPair != null) {
                    HashSet<Edge> tmp = new HashSet<>();
                    for (Edge e : resultPair.first) {
                        Edge newE = e.copy();
                        tmp.add(newE);
                    }
                    newShortestPathMap.put(nodePair, new Pair<HashSet<Edge>, Double>(tmp, resultPair.second));
                }
            }
        }
        return new MyGraph(name, newNodes, newEdges, newShortestPathMap, optimal);
    }

    /*
     * public Tree unMetricTree(Tree t) { Tree out = new Tree(t.node); for (Tree
     * child : t.children) { Tree djikstra = djikstra(t.node.id, child.node.id);
     * double cost = djikstra.findNode(child.node).cost; Tree leadingToChild =
     * djikstra.children.iterator().next(); Tree rec = unMetricTree(child);
     * rec.changeCostOfN(child.node, cost); HashSet<Node> dublicates =
     * leadingToChild.getNodes(); dublicates.retainAll(rec.getNodes());
     * dublicates.remove(child.node); if (!dublicates.isEmpty()) { for (Node n :
     * dublicates) { Tree inRec = rec.findNode(n); for (Tree c : inRec.children) {
     * leadingToChild.addChild(n, c, c.cost); } rec = rec.removeNode(n); } } Tree
     * both = leadingToChild.replace(child.node, rec); dublicates = out.getNodes();
     * dublicates.retainAll(both.getNodes()); dublicates.remove(child.node);
     * dublicates.remove(t.node); if (!dublicates.isEmpty()) { for (Node n :
     * dublicates) { if (both == null) { break; } Tree x = both.findNode(n); for
     * (Tree c : x.children) { out.addChild(n, c, c.cost); } both =
     * both.removeNode(n); } } if (both != null) out.addChild(t.node, both,
     * both.cost); } return out; }
     */
}