package steiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

public class Tree {

    public Node node;
    public double cost;
    public HashSet<Tree> children;

    public Tree(Node node) {
        this.node = node;
        this.cost = 0;
        this.children = new HashSet<Tree>();
        for (Edge e : node.getEdges()) {
            Node opp = e.opposite(node);
            opp.removeEdge(e);
            children.add(new Tree(opp, e.getWeight()));
        }
    }

    private Tree(Node node, double weight) {
        this.node = node;
        this.cost = weight;
        this.children = new HashSet<Tree>();
        for (Edge e : node.getEdges()) {
            Node opp = e.opposite(node);
            opp.removeEdge(e);
            children.add(new Tree(opp, e.getWeight()));
        }
    }

    public boolean containsNode(Node n) {
        if (node.equals(n))
            return true;
        for (Tree child : children) {
            if (child.containsNode(n))
                return true;
        }
        return false;
    }

    public boolean addChild(Edge edge) {
        if (node.equals(edge.first)) {
            children.add(new Tree(edge.second, edge.getWeight()));
            return true;
        }
        if (node.equals(edge.second)) {
            children.add(new Tree(edge.first, edge.getWeight()));
            return true;
        }
        boolean bool = false;
        for (Tree child : children) {
            bool = bool && child.addChild(edge);
        }
        return bool;
    }

    public void writeDot() throws IOException {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("example/tree.dot")))) {
            out.write("digraph {");
            out.newLine();
            writeDotRec(out);
            out.write("}");
            out.close();
        }
    }

    private void writeDotRec(BufferedWriter out) throws IOException {
        out.write(node.getName() + (node.isTerminal() ? "[color=red]" : "[color=black]"));
        out.newLine();
        for (Tree child : children) {
            child.writeDotRec(out);
            out.write(
                    node.getName() + "->" + child.node.getName() + "[label=" + String.format("%.2f", child.cost) + "]");
            out.newLine();
        }
    }

    public void printGraph() throws IOException {
        writeDot();
        try (InputStream dot = new FileInputStream("example/tree.dot")) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(1920).render(Format.SVG).toFile(new File("example/tree.svg"));
        }
    }

    public HashSet<Node> getAllTerminalNodes() {
        HashSet<Node> set = new HashSet<>();
        if (node.isTerminal())
            set.add(node);
        for (Tree child : children) {
            set.addAll(child.getAllTerminalNodes());
        }
        return set;
    }

    public int getNumberOfTerminals() {
        int i = node.isTerminal() ? 1 : 0;
        for (Tree child : children) {
            i += child.getNumberOfTerminals();
        }
        return i;
    }

    private TreeEdge getMaxCostConnectingEdge(int numberofTotalTerminals, TreeEdge e) {
        for (Tree child : children) {
            if (numberofTotalTerminals > child.getNumberOfTerminals() && child.getNumberOfTerminals() > 0) {
                if (child.cost > e.cost)
                    e = new TreeEdge(this, child);

            }
            e = child.getMaxCostConnectingEdge(numberofTotalTerminals, e);
        }
        return e;
    }

    public TreeEdge getMaxCostConnectingEdge() {
        if (children.size() == 0)
            return null;
        TreeEdge e = getMaxCostConnectingEdge(getNumberOfTerminals(), new TreeEdge(this, children.iterator().next()));
        return e;
    }

    public Tree removeEdge(TreeEdge e) {
        if (e.from.equals(this)) {
            children.remove(e.to);
            return this;
        }
        for (Tree child : children) {
            child = child.removeEdge(e);
        }
        return this;
    }

    public Tree getRndTerminalTreeNode() {
        if (node.isTerminal())
            return this;
        for (Tree child : children) {
            Tree t = child.getRndTerminalTreeNode();
            if (t != null)
                return t;
        }
        return null;
    }
}