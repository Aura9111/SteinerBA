package steiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class MyGraph {

    public String path;
    public HashMap<String, Node> nodes;
    public HashMap<String, Edge> edges;
    public int edgeCount;
    public int nodeCount;

    public MyGraph() {
        this.path = "example/graph";
        this.nodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.edgeCount = 0;
        this.nodeCount = 0;
    }

    public MyGraph(HashMap<String, Node> nodes, HashMap<String, Edge> edges) {
        this.path = "example/graph";
        this.nodes = nodes;
        this.edges = edges;
        this.nodeCount = nodes.size();
        this.edgeCount = edges.size();
    }

    public void removeNode(String node) {

    }

    public void addNode(String node, boolean isTerminal) {
        Node n = new Node(node, isTerminal);
        nodes.put(node, n);
        nodeCount++;
    }

    public boolean addEdge(String node1, String node2, double weight) {
        if (nodes.containsKey(node1) && nodes.containsKey(node2) && !edges.containsKey(node1 + "--" + node2)
                && !edges.containsKey(node2 + "--" + node1)) {
            Node n1 = nodes.get(node1);
            Node n2 = nodes.get(node2);
            String edgeName = node1 + "--" + node2;
            Edge e = new Edge(edgeName, n1, n2, weight);
            n1.addEdge(e);
            n2.addEdge(e);
            edges.put(edgeName, e);
            edgeCount++;
        }
        return false;
    }

    public Optional<Edge> removeEdge(String node1, String node2) {
        if (edges.containsKey(node1 + "--" + node2)) {
            edgeCount--;
            return Optional.of(edges.remove(node1 + "--" + node2));
        }
        if (edges.containsKey(node2 + "--" + node1)) {
            edgeCount--;
            return Optional.of(edges.remove(node2 + "--" + node1));
        }
        return Optional.empty();
    }

    public Optional<Edge> getEdge(String node1, String node2) {
        if (edges.containsKey(node1 + "--" + node2)) {
            return Optional.of(edges.get(node1 + "--" + node2));
        }
        if (edges.containsKey(node2 + "--" + node1)) {
            return Optional.of(edges.get(node2 + "--" + node1));
        }
        return Optional.empty();
    }

    public String toString() {
        return "Hi I'm a Graph with " + edgeCount + " Edges and " + nodeCount + " Nodes\n";
    }

    public void writeDot() throws IOException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".dot")))) {
            out.write("graph {");
            out.newLine();
            for (Node n : nodes.values()) {
                out.write(n.getName() + (n.isTerminal() ? "[color=red]" : "[color=blue]"));
            }

            for (Edge e : edges.values()) {
                out.write(e.getName() + "[label=" + String.format("%.2f", e.getWeight()) + "]");
                out.newLine();
            }
            out.write("}");
            out.close();
        }
    }

    public void printGraph() throws IOException {
        writeDot();
        try (InputStream dot = new FileInputStream(path + ".dot")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(800).render(Format.SVG).toFile(new File(path + ".svg"));
        }
    }

    public boolean inSameComponent(String node1, String node2) {
        if (!nodes.containsKey(node1) || !nodes.containsKey(node2))
            return false;
        return nodes.get(node1).isInSameComponent(nodes.get(node2));
    }

    public int numberOfComponents() {
        int i = 0;
        HashSet<Node> tmp = new HashSet<Node>(nodes.values());
        while (!tmp.isEmpty()) {
            Iterator<Node> it = tmp.iterator();
            tmp.removeAll(it.next().getNodesInComponent(new HashSet<Node>()));
            i++;
        }
        return i;
    }

    public HashSet<Node> getNodesinComponent(String node) {
        return nodes.get(node).getNodesInComponent(new HashSet<Node>());
    }

    public HashSet<Node> getNodesNotinComponent(String node) {
        HashSet<Node> tmp = new HashSet<Node>(nodes.values());
        tmp.removeAll(nodes.get(node).getNodesInComponent(new HashSet<Node>()));
        return tmp;
    }

    public HashSet<Node> getNodesNotConnected(String node) {
        HashSet<Node> tmp = new HashSet<Node>(nodes.values());
        tmp.removeAll(nodes.get(node).getNeighbors());
        return tmp;
    }
}
