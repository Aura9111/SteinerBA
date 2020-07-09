package steiner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyGraph {
    public String name;
    public Node[] nodes;
    public double[][] edges;
    public double[][] shortestPaths;
    public double optimal;

    public MyGraph(String name, int size) {
        this.name = name;
        nodes = new Node[size];
        edges = new double[size][size];
        shortestPaths = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                edges[i][j] = i == j ? 0 : Double.POSITIVE_INFINITY;
                shortestPaths[i][j] = i == j ? 0 : Double.POSITIVE_INFINITY;
            }
        }
    }

    public MyGraph(MyGraph g, HashSet<Node> set) {
        this.name = g.name + set;
        int size = g.nodes.length;
        nodes = new Node[size];
        edges = new double[size][size];
        shortestPaths = new double[size][size];
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
            }
        }
        floydWarshal();
    }

    private HashSet<Edge> getEdgesOfNode(int nodeId) {
        HashSet<Edge> out = new HashSet<>();
        for (int i = 0; i < nodes.length; i++) {
            if (edges[nodeId][i] < Double.POSITIVE_INFINITY && i != nodeId)
                out.add(new Edge((nodeId + 1) + "-" + (i + 1), nodes[nodeId], nodes[i], edges[nodeId][i]));
        }
        return out;
    }

    public MyGraph(Graph g) {
        this.name = g.path;
        Collection<Node> nodeSet = g.getNodes().values();
        int size = nodeSet.size();
        nodes = new Node[size];
        edges = new double[size][size];
        shortestPaths = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                edges[i][j] = i == j ? 0 : Double.POSITIVE_INFINITY;
            }
        }
        for (Node n : nodeSet) {
            nodes[n.id] = n;
            for (Edge e : n.getEdges()) {
                int n1 = e.first.id;
                int n2 = e.second.id;
                edges[n1][n2] = e.cost;
                edges[n2][n1] = e.cost;
            }
        }
        floydWarshal();
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
                if (nodes[n1].isTerminal())
                    out.add(nodes[n1]);
            }
        }
        return out;
    }

    public HashSet<Node> getSteinerNodes() {
        HashSet<Node> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null) {
                if (!nodes[n1].isTerminal())
                    out.add(nodes[n1]);
            }
        }
        return out;
    }

    public double shortestPathCost(Integer n1, Integer n2) {
        if (djikstra(n1, n2) != null) {
            return djikstra(n1, n2).totalCost();
        }
        return 0;
    }

    public Tree djikstra(Integer n1, Integer n2) {
        HashSet<Integer> todo = new HashSet<>();
        HashMap<Integer, Pair<Integer, Double>> map = new HashMap<>();
        map.put(n1, new Pair<Integer, Double>(null, 0.0));
        todo.add(n1);
        while (!todo.isEmpty()) {
            HashSet<Integer> tmp = new HashSet<>();
            for (Integer n : todo) {
                for (Node node : getNodes()) {
                    int i = node.id;
                    double newCost = map.get(n).second + edges[n][i];
                    if (map.containsKey(i)) {
                        if (map.get(i).second > newCost) {
                            map.put(i, new Pair<Integer, Double>(n, newCost));
                        }
                    } else {
                        map.put(i, new Pair<Integer, Double>(n, newCost));
                        tmp.add(i);
                    }
                }
            }
            todo = tmp;
        }
        int i = n2;
        Integer j = null;
        Tree t1 = new Tree(nodes[i]);
        if (!map.containsKey(n2))
            return null; // n2 is in different component-> unreachable
        if (map.get(n2).first != null)
            j = map.get(n2).first;
        while (j != null) {
            Tree t2 = new Tree(nodes[j]);
            double cost = map.get(i).second - map.get(j).second;
            t2.addChild(nodes[j], t1, cost);
            t1 = t2;
            i = j;
            j = map.get(j).first;
        }
        return t1;
    }

    public void floydWarshal() {
        shortestPaths = new double[nodes.length][nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            for (int j = 0; j < nodes.length; j++) {
                shortestPaths[i][j] = edges[i][j];
            }
        }
        for (int k = 0; k < nodes.length; k++) {
            for (int i = 0; i < nodes.length; i++) {
                for (int j = 0; j < nodes.length; j++) {
                    if (shortestPaths[i][k] + shortestPaths[k][j] < shortestPaths[i][j])
                        shortestPaths[i][j] = shortestPaths[i][k] + shortestPaths[k][j];
                }
            }
        }
    }

    public Tree MSTAlgorithm(HashSet<Node> set) throws Exception {
        return MSTAlgorithm(set, getShortestPaths());
    }

    public Tree MSTAlgorithm(HashSet<Node> set, HashSet<Edge> edges) throws Exception {
        Forest f = kruskal(set, edges);
        if (f.size() > 1)
            throw new Exception("cant construct mst. forest has " + f.size() + " components");
        Tree t = f.giveSingleTree();
        t = helpfulFunctions.unMetricTree(this, t);
        if (!t.getSteinerNodes().isEmpty()) {
            t = t.makeRoot(t.getSteinerNodes().iterator().next());
        }
        return t;
    }

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

    public Forest kruskal(HashSet<Node> nodeSet, HashSet<Edge> edgeSet) throws Exception {
        Forest forest = new Forest();
        for (Node n : nodeSet) {
            forest.addTree(new Tree(n));
        }
        ArrayList<Edge> sortedEdges = sortEdges(edgeSet);
        Iterator<Edge> it = sortedEdges.iterator();
        while (forest.size() > 1 && it.hasNext()) {
            Edge e = it.next();
            int i = 0;
            int indexFirst = -1;
            int indexSecond = -1;
            for (Tree t : forest.getTrees()) {
                if (t.containsNode(e.first))
                    indexFirst = i;
                if (t.containsNode(e.second))
                    indexSecond = i;
                i++;
            }
            if (indexFirst >= 0 && indexSecond >= 0 && indexFirst != indexSecond) {
                forest.addEdgeWithNewNode(e);
            }
        }
        return forest;
    }

    public Tree minimumSpanningTree(HashSet<Node> nodeSet, HashSet<Edge> edgeSet) throws Exception {
        Forest f = kruskal(nodeSet, edgeSet);
        if (f.size() > 1)
            throw new Exception("cant construct mst. forest has " + f.size() + " components");
        return f.giveSingleTree();
    }

    public HashSet<Edge> getEdges() {
        HashSet<Edge> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null) {
                for (int n2 = n1; n2 < nodes.length; n2++) {
                    if (nodes[n2] != null) {
                        if (edges[n1][n2] != Double.POSITIVE_INFINITY) {
                            out.add(new Edge("", nodes[n1], nodes[n2], edges[n1][n2]));
                        }
                    }
                }
            }
        }
        return out;
    }

    public void printEdges() {
        String out = "";
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                for (int j = 0; j < nodes.length; j++) {
                    if (nodes[j] != null) {
                        if (edges[i][j] < Double.POSITIVE_INFINITY)
                            out = out + (i + 1) + "-" + (j + 1) + ": " + edges[i][j] + ", ";
                    }
                }
            }
        }
        System.out.println(out);
    }

    public void printShortestPaths() {
        String out = "";
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                for (int j = 0; j < nodes.length; j++) {
                    if (nodes[j] != null) {
                        if (shortestPaths[i][j] < Double.POSITIVE_INFINITY)
                            out = out + (i + 1) + "-" + (j + 1) + ": " + shortestPaths[i][j] + ", ";
                    }
                }
            }
        }
        System.out.println(out);

    }

    public void contractSet(HashSet<Node> set) {
        for (Node n1 : set) {
            for (Node n2 : set) {
                edges[n1.id][n2.id] = 0;
            }
        }
        floydWarshal();
    }

    public HashSet<Edge> getShortestPaths() {
        HashSet<Edge> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null) {
                for (int n2 = n1; n2 < nodes.length; n2++) {
                    if (nodes[n2] != null) {
                        if (shortestPaths[n1][n2] != Double.POSITIVE_INFINITY) {
                            out.add(new Edge("", nodes[n1], nodes[n2], shortestPaths[n1][n2]));
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
        floydWarshal();
    }

    public void addEdgeNoUpdate(Edge e) {
        edges[e.first.id][e.second.id] = e.cost;
        edges[e.second.id][e.first.id] = e.cost;
    }

    public HashSet<Edge> getShortestPathsContracted(HashSet<Node> set) {
        HashSet<Edge> out = new HashSet<>();
        for (int n1 = 0; n1 < nodes.length; n1++) {
            if (nodes[n1] != null) {
                for (int n2 = n1; n2 < nodes.length; n2++) {
                    if (nodes[n2] != null) {
                        if (shortestPaths[n1][n2] != Double.POSITIVE_INFINITY) {
                            if (!set.contains(nodes[n1]) || !set.contains(nodes[n2]))
                                out.add(new Edge("", nodes[n1], nodes[n2], shortestPaths[n1][n2]));
                            else
                                out.add(new Edge("", nodes[n1], nodes[n2], 0));
                        }
                    }
                }
            }
        }
        return out;
    }

    public MyGraph copy() {
        return new MyGraph(this, getNodes());
    }
}