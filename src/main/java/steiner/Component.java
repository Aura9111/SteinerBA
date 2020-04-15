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

public class Component {

    private String path;
    public HashMap<String, Node> nodes;
    public HashMap<String, Edge> edges;

    public Component(Node node, String path) {
        this.path = path;
        this.nodes = new HashMap<>();
        this.nodes.put(node.getName(), node);
        this.edges = new HashMap<>();
    }

    public Component(HashMap<String, Node> nodes, HashMap<String, Edge> edges, String path) {
        this.nodes = nodes;
        this.edges = edges;
        this.path = path;
    }

    public Optional<Node> getNode(String nodeName) {
        if (nodes.containsKey(nodeName)) {
            return Optional.of(nodes.get(nodeName));
        }
        return Optional.empty();
    }

    public HashSet<Component> removeNode(String nodeName) {
        if (!nodes.containsKey(nodeName)) {
            HashSet<Component> set = new HashSet<Component>();
            set.add(this);
            return set;
        }
        Node n = nodes.get(nodeName);
        HashSet<Component> set = new HashSet<Component>();
        for (Edge e : n.getEdges()) {
            Optional<Component> c = removeEdge(nodeName, e.opposite(n).getName());
            if (c.isPresent())
                set.add(c.get());
        }
        nodes.remove(nodeName);
        set.add(this);
        return set;
    }

    public boolean addEdge(String nodeName1, String nodeName2, double weight) {
        if (!nodes.containsKey(nodeName1) || !nodes.containsKey(nodeName2))
            return false;
        String edgeName = (nodeName1.compareTo(nodeName2) < 0) ? nodeName1 + "--" + nodeName2
                : nodeName2 + "--" + nodeName1;
        if (edges.containsKey(edgeName))
            return false;
        Node n1 = (nodeName1.compareTo(nodeName2) < 0) ? nodes.get(nodeName1) : nodes.get(nodeName2);
        Node n2 = (nodeName1.compareTo(nodeName2) >= 0) ? nodes.get(nodeName1) : nodes.get(nodeName2);
        Edge e = new Edge(edgeName, n1, n2, weight);
        n1.addEdge(e);
        n2.addEdge(e);
        edges.put(edgeName, e);
        return true;
    }

    public Optional<Edge> getEdge(String nodeName1, String nodeName2) {
        String edgeName = (nodeName1.compareTo(nodeName2) < 0) ? nodeName1 + "--" + nodeName2
                : nodeName2 + "--" + nodeName1;
        if (edges.containsKey(edgeName))
            return Optional.of(edges.get(edgeName));
        return Optional.empty();
    }

    public Optional<Component> removeEdge(String nodeName1, String nodeName2) {
        String edgeName = (nodeName1.compareTo(nodeName2) < 0) ? nodeName1 + "--" + nodeName2
                : nodeName2 + "--" + nodeName1;
        Edge e = edges.get(edgeName);
        nodes.get(nodeName1).removeEdge(e);
        nodes.get(nodeName2).removeEdge(e);
        edges.remove(edgeName);
        if (nodes.get(nodeName1).isInSameComponent(nodes.get(nodeName2)))
            return Optional.empty();
        HashMap<String, Node> newNodes = new HashMap<String, Node>();
        HashMap<String, Edge> newEdges = new HashMap<String, Edge>();
        HashSet<String> workSet = new HashSet<String>();
        workSet.add(nodeName2);
        while (!workSet.isEmpty()) {
            HashSet<String> tmp = new HashSet<String>();
            for (String name : workSet) {
                Node n = nodes.get(name);
                for (Edge edge : n.getEdges()) {
                    newEdges.put(edge.getName(), edges.remove(edge.getName()));
                    String oppName = edge.opposite(n).getName();
                    if (!newNodes.containsKey(oppName))
                        tmp.add(oppName);
                }
                newNodes.put(name, nodes.remove(name));
            }
            workSet = tmp;
        }
        return Optional.of(new Component(newNodes, newEdges, path));
    }

    public void writeDot(BufferedWriter out) throws IOException {
        for (Node n : nodes.values()) {
            out.write(n.getName() + (n.isTerminal() ? "[color=red]" : "[color=blue]"));
            out.newLine();
        }
        for (Edge e : edges.values()) {
            out.write(e.getName() + "[label=" + String.format("%.2f", e.getWeight()) + "]");
            out.newLine();
        }
    }

    public void printGraph() throws IOException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".dot")))) {
            out.write("graph {");
            out.newLine();
            writeDot(out);
            out.write("}");
            out.close();
        }
        try (InputStream dot = new FileInputStream(path + ".dot")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(1920).render(Format.SVG).toFile(new File(path + ".svg"));
        }
    }

    public boolean containsNode(String nodeName) {
        return nodes.containsKey(nodeName);
    }

    public int numberOfComponents() {
        return 1;
    }

    public HashSet<Node> getNeighborNodes(String nodeName) {
        if (!nodes.containsKey(nodeName))
            return new HashSet<Node>();
        return nodes.get(nodeName).getNeighbors();
    }

    public HashSet<Node> getNonNeighborNodes(String nodeName) {
        if (!nodes.containsKey(nodeName))
            return new HashSet<Node>(nodes.values());
        HashSet<Node> set = new HashSet<Node>(nodes.values());
        set.removeAll(nodes.get(nodeName).getNeighbors());
        return set;
    }

    public HashSet<Node> getAllTerminalNodes() {
        HashSet<Node> output = new HashSet<Node>();
        Iterator<Node> it = nodes.values().iterator();
        while (it.hasNext()) {
            Node n = it.next();
            if (n.isTerminal())
                output.add(n);
        }
        return output;
    }

    public HashSet<Node> getAllSteinerNodes() {
        HashSet<Node> output = new HashSet<Node>();
        Iterator<Node> it = nodes.values().iterator();
        while (it.hasNext()) {
            Node n = it.next();
            if (!n.isTerminal())
                output.add(n);
        }
        return output;
    }

    public boolean hasCircles() {
        return (edges.size() >= nodes.size());
    }

    public boolean containsEdge(String nodeName1, String nodeName2) {
        String edgeName = (nodeName1.compareTo(nodeName2) < 0) ? nodeName1 + "--" + nodeName2
                : nodeName2 + "--" + nodeName1;
        return edges.containsKey(edgeName);
    }

    public Node getHighestDegreeNode() {
        Node n = null;
        int degree = 0;
        for (Node tmp : nodes.values()) {
            if (tmp.getDegree() > degree) {
                n = tmp;
                degree = tmp.getDegree();
            }
        }
        return n;
    }
}