package steiner.HougardyProemel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class MyTree {

    public Node root;
    public HashSet<Node> nodes;
    public HashSet<Edge> edges;
    public HashMap<Pair<Node, Node>, Pair<HashSet<Edge>, Double>> shortestPaths;

    public MyTree() {
        nodes = new HashSet<>();
        edges = new HashSet<>();
        shortestPaths = new HashMap<>();
    }

    public MyTree(Node n) {
        this();
        nodes.add(n);
        root = n;
    }

    public MyTree(HashSet<Edge> edges, Node root) {
        this();
        this.edges = edges;
        for (Edge e : edges) {
            nodes.add(e.first);
            nodes.add(e.second);
            HashSet<Edge> tmp = new HashSet<>();
            tmp.add(e);
            shortestPaths.put(new Pair<Node, Node>(e.first, e.second), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
            shortestPaths.put(new Pair<Node, Node>(e.second, e.first), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
        }
        for (Node n : nodes) {
            shortestPaths.put(new Pair<Node, Node>(n, n), new Pair<HashSet<Edge>, Double>(new HashSet<Edge>(), 0.0));
        }
        this.root = root;
        computeShortestPaths();
        orderTree();
    }

    public MyTree(HashSet<Edge> edges) {
        this();
        this.edges = edges;
        for (Edge e : edges) {
            nodes.add(e.first);
            nodes.add(e.second);
            HashSet<Edge> tmp = new HashSet<>();
            tmp.add(e);
            shortestPaths.put(new Pair<Node, Node>(e.first, e.second), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
            shortestPaths.put(new Pair<Node, Node>(e.second, e.first), new Pair<HashSet<Edge>, Double>(tmp, e.cost));
        }
        for (Node n : nodes) {
            shortestPaths.put(new Pair<Node, Node>(n, n), new Pair<HashSet<Edge>, Double>(new HashSet<Edge>(), 0.0));
        }
        if (!getSteinerNodes().isEmpty())
            root = getSteinerNodes().iterator().next();
        else
            root = nodes.iterator().next();
        computeShortestPaths();
        orderTree();
    }

    private void orderTree() {
        HashSet<Node> done = new HashSet<>();
        HashSet<Node> todo = new HashSet<>();
        todo.add(root);
        while (!todo.isEmpty()) {
            HashSet<Node> nextTodo = new HashSet<>();
            for (Node curr : todo) {
                for (Edge e : getEdgesOfNode(curr)) {
                    if (!done.contains(e.opposite(curr))) {
                        if (!e.first.equals(curr))
                            e.reverse();
                        nextTodo.add(e.second);
                        done.add(e.first);
                    }
                }
            }
            todo = nextTodo;
        }
    }

    private void computeShortestPaths() {
        for (Node k : nodes) {
            for (Node i : nodes) {
                for (Node j : nodes) {
                    Pair<HashSet<Edge>, Double> pairIK = shortestPaths.get(new Pair<Node, Node>(i, k));
                    Pair<HashSet<Edge>, Double> pairKJ = shortestPaths.get(new Pair<Node, Node>(k, j));
                    Pair<HashSet<Edge>, Double> pairIJ = shortestPaths.get(new Pair<Node, Node>(i, j));
                    double ik = pairIK == null ? Double.POSITIVE_INFINITY : pairIK.second;
                    double kj = pairKJ == null ? Double.POSITIVE_INFINITY : pairKJ.second;
                    double ij = pairIJ == null ? Double.POSITIVE_INFINITY : pairIJ.second;
                    if (ik + kj < ij) {
                        HashSet<Edge> newPath = new HashSet<>();
                        newPath.addAll(pairIK.first);
                        newPath.addAll(pairKJ.first);
                        shortestPaths.put(new Pair<Node, Node>(i, j),
                                new Pair<HashSet<Edge>, Double>(newPath, ik + kj));
                        shortestPaths.put(new Pair<Node, Node>(j, i),
                                new Pair<HashSet<Edge>, Double>(newPath, ik + kj));
                    }
                }
            }
        }
    }

    public double totalCost() {
        double out = 0;
        for (Edge e : edges) {
            out += e.cost;
        }
        return out;
    }

    public HashSet<Edge> getEdgesOfNode(Node n) {
        HashSet<Edge> out = new HashSet<>();
        for (Edge e : edges) {
            if (e.first.equals(n) || e.second.equals(n))
                out.add(e);
        }
        return out;
    }

    public HashSet<Node> getSteinerNodes() {
        HashSet<Node> out = new HashSet<>();
        for (Node n : nodes) {
            if (!n.terminal)
                out.add(n);
        }
        return out;
    }

    public HashSet<Node> getTerminalNodes() {
        HashSet<Node> out = new HashSet<>();
        for (Node n : nodes) {
            if (n.terminal)
                out.add(n);
        }
        return out;
    }

    public MyTree copy() {
        return new MyTree(edges);
    }

    public void writeDot(String path) throws IOException {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("example/" + path + ".dot")))) {
            out.write("digraph {");
            out.newLine();
            writeDotRec(out);
            out.write("}");
            out.close();
        }
    }

    public void writeDotRec(BufferedWriter out) throws IOException {
        for (Node n : nodes) {
            out.write(n.id + (n.terminal ? "[color=red]" : "[color=black]"));
            out.newLine();
        }
        for (Edge e : edges) {
            out.write(e.first.id + "->" + e.second.id + "[label=" + String.format("%.2f", e.cost) + "]");
            out.newLine();
        }
    }

    public void printGraph(String path) throws IOException {
        writeDot(path);
        try (InputStream dot = new FileInputStream(("example/" + path + ".dot"))) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(600).render(Format.SVG).toFile(new File("example/" + path + ".svg"));
        }
    }

    public void makeRoot(Node root) {
        this.root = root;
        orderTree();
    }

    public void addTree(Edge e, MyTree to) {
        if (this.equals(to))
            return;
        if (!e.second.equals(to.root))
            e.reverse();
        this.edges.add(e);
        this.edges.addAll(to.edges);
        this.nodes.addAll(to.nodes);
        this.shortestPaths.putAll(to.shortestPaths);
        computeShortestPaths();
    }

    public boolean isFull(HashSet<Node> terminals) {
        if (nodes.size() == 1)
            return terminals.contains(nodes.iterator().next());
        if (nodes.size() == 2)
            return false;
        for (Node n : nodes) {
            if (terminals.contains(n) && getEdgesOfNode(n).size() > 1)
                return false;
            if (!terminals.contains(n) && getEdgesOfNode(n).size() <= 1)
                return false;
        }
        return true;
    }

    public HashSet<Node> getSteinerNodes(HashSet<Node> terminals) {
        HashSet<Node> out = new HashSet<>();
        for (Node n : nodes) {
            if (!terminals.contains(n))
                out.add(n);
        }
        return out;
    }

    public MyTree removeEdge(Edge e) {
        if (!edges.contains(e))
            e.reverse();
        edges.remove(e);
        HashMap<Pair<Node, Node>, Pair<HashSet<Edge>, Double>> map = new HashMap<>();
        for (Pair<Node, Node> p : shortestPaths.keySet()) {
            if (!shortestPaths.get(p).first.contains(e))
                map.put(p, shortestPaths.get(p));
        }
        shortestPaths = map;
        computeShortestPaths();
        HashSet<Node> nodeSet = new HashSet<>();
        for (Node n : nodes) {
            if (shortestPaths.containsKey(new Pair<Node, Node>(e.second, n)))
                nodeSet.add(n);
        }
        nodes.removeAll(nodeSet);
        HashSet<Edge> edgeSet = new HashSet<>();
        for (Edge edge : edges) {
            if (nodeSet.contains(edge.first) || nodeSet.contains(edge.second))
                edgeSet.add(edge);
        }
        for (Edge remove : edgeSet) {
            edges.remove(remove);
        }
        if (nodeSet.size() == 1)
            return new MyTree(nodeSet.iterator().next());
        return new MyTree(edgeSet);
    }

    public HashSet<Node> getTerminalNodes(HashSet<Node> terminals) {
        HashSet<Node> out = new HashSet<>();
        for (Node n : nodes) {
            if (terminals.contains(n))
                out.add(n);
        }
        return out;
    }

}