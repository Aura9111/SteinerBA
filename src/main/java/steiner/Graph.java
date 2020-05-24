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
import java.util.Optional;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class Graph {

    public HashSet<Component> components;
    private String path;

    public Graph(String path) {
        components = new HashSet<Component>();
        this.path = path;
    }

    public void changePath(String path) {
        this.path = path;
        for (Component c : components) {
            c.changePath(path);
        }
    }

    public boolean addEdge(String nodeName1, String nodeName2, double weight) {
        Component c1 = null;
        Component c2 = null;
        for (Component c : components) {
            if (c.containsNode(nodeName1) && c.containsNode(nodeName2))
                return c.addEdge(nodeName1, nodeName2, weight);
            if (c.containsNode(nodeName1))
                c1 = c;
            if (c.containsNode(nodeName2))
                c2 = c;
        }
        if (c1 == null || c2 == null)
            return false;
        HashMap<String, Node> newNodes = c1.nodes;
        newNodes.putAll(c2.nodes);
        HashMap<String, Edge> newEdges = c1.edges;
        newEdges.putAll(c2.edges);
        String edgeName = (nodeName1.compareTo(nodeName2) < 0) ? nodeName1 + "--" + nodeName2
                : nodeName2 + "--" + nodeName1;
        Node n1 = (nodeName1.compareTo(nodeName2) < 0) ? newNodes.get(nodeName1) : newNodes.get(nodeName2);
        Node n2 = (nodeName1.compareTo(nodeName2) >= 0) ? newNodes.get(nodeName1) : newNodes.get(nodeName2);
        Edge e = new Edge(edgeName, n1, n2, weight);
        newNodes.get(nodeName1).addEdge(e);
        newNodes.get(nodeName2).addEdge(e);
        newEdges.put(edgeName, e);
        components.remove(c1);
        components.remove(c2);
        components.add(new Component(newNodes, newEdges, path));
        return true;
    }

    public Optional<Node> getNode(String nodeName) {
        for (Component c : components) {
            Optional<Node> output = c.getNode(nodeName);
            if (output.isPresent())
                return output;
        }
        return Optional.empty();
    }

    public void removeNode(String nodeName) {
        for (Component c : components) {
            if (c.containsNode(nodeName)) {
                components.remove(c);
                components.addAll(c.removeNode(nodeName));
            }
        }
    }

    public Optional<Edge> getEdge(String nodeName1, String nodeName2) {
        for (Component c : components) {
            Optional<Edge> output = c.getEdge(nodeName1, nodeName2);
            if (output.isPresent())
                return output;
        }
        return Optional.empty();
    }

    public boolean removeEdge(String nodeName1, String nodeName2) {
        for (Component c : components) {
            if (c.containsEdge(nodeName1, nodeName2)) {
                Optional<Component> opt = c.removeEdge(nodeName1, nodeName2);
                if (opt.isPresent())
                    components.add(opt.get());
                return true;
            }
        }
        return false;
    }

    public void writeDot(BufferedWriter out) throws IOException {
        for (Component c : components) {
            c.writeDot(out);
        }
    }

    public void writeDotOnly() throws IOException {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("example/" + path + ".dot")))) {
            out.write("graph {");
            out.newLine();
            writeDot(out);
            out.write("}");
            out.close();
        }
    }

    public void printGraph() throws IOException {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("example/" + path + ".dot")))) {
            out.write("graph {");
            out.newLine();
            writeDot(out);
            out.write("}");
            out.close();
        }
        try (InputStream dot = new FileInputStream("example/" + path + ".dot")) {
            MutableGraph g = new Parser().read(dot);
            dot.close();
            Graphviz.fromGraph(g).width(1920).render(Format.SVG).toFile(new File("example/" + path + ".svg"));
        }
    }

    public void printGraphFromExistingDot() throws IOException {
        try (InputStream dot = new FileInputStream("example/" + path + ".dot")) {
            MutableGraph g = new Parser().read(dot);
            dot.close();
            Graphviz.fromGraph(g).width(1920).render(Format.SVG).toFile(new File("example/" + path + ".svg"));
        }
    }

    public boolean containsNode(String nodeName) {
        for (Component c : components) {
            if (c.containsNode(nodeName)) {
                return true;
            }
        }
        return false;
    }

    public void addComponent(Component component) {
        components.add(component);
    }

    public boolean isInSameComponent(String nodeName1, String nodeName2) {
        for (Component c : components) {
            if (c.containsNode(nodeName1) && c.containsNode(nodeName2)) {
                return true;
            }
        }
        return false;
    }

    public int numberOfComponents() {
        return components.size();
    }

    public Optional<Component> getComponent(String nodeName) {
        for (Component c : components) {
            if (c.containsNode(nodeName)) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public HashSet<Node> getNodesNotinComponent(String nodeName) {
        HashSet<Node> set = new HashSet<Node>();
        for (Component c : components) {
            if (!c.containsNode(nodeName)) {
                set.addAll(c.nodes.values());
            }
        }
        return set;
    }

    public HashSet<Node> getNeighborNodes(String nodeName) {
        for (Component c : components) {
            if (c.containsNode(nodeName)) {
                return c.getNeighborNodes(nodeName);
            }
        }
        return new HashSet<Node>();
    }

    public HashSet<Node> getAllTerminalNodes() {
        HashSet<Node> output = new HashSet<Node>();
        for (Component c : components) {
            output.addAll(c.getAllTerminalNodes());
        }
        return output;
    }

    public HashSet<Node> getAllSteinerNodes() {
        HashSet<Node> output = new HashSet<Node>();
        for (Component c : components) {
            output.addAll(c.getAllSteinerNodes());
        }
        return output;
    }

    public boolean hasCircles() {
        for (Component c : components) {
            if (c.hasCircles())
                return true;
        }
        return false;
    }

    public boolean containsEdge(String nodeName1, String nodeName2) {
        for (Component c : components) {
            if (c.containsEdge(nodeName1, nodeName2))
                return true;
        }
        return false;
    }

    public Node getHighestDegreeNode() {
        Node n = null;
        int degree = 0;
        for (Component c : components) {
            Node tmp = c.getHighestDegreeNode();
            if (tmp.getDegree() >= degree) {
                n = tmp;
                degree = tmp.getDegree();
            }
        }
        return n;
    }

    public void addNode(String nodeName, boolean isTerminal) {
        components.add(new Component(new Node(nodeName, isTerminal), path));
    }

    public HashSet<Node> getNonNeighborNodes(String nodeName) {
        for (Component c : components) {
            if (c.containsNode(nodeName)) {
                return c.getNonNeighborNodes(nodeName);
            }
        }
        return new HashSet<Node>();
    }

    public Edge getMaxCostConnectingEdge() {
        if (components.size() > 1 || hasCircles())
            return null;
        return components.iterator().next().getMaxCostConnectingEdge();
    }

	public HashMap<String, Node> getNodes() {
		HashMap<String, Node> output = new HashMap<>();
        for (Component c : components) {
            output.putAll(c.nodes);
        }
        return output;
	}

	public HashMap<String, Edge> getEdges() {
		HashMap<String, Edge> output = new HashMap<>();
        for (Component c : components) {
            output.putAll(c.edges);
        }
        return output;
	}
}