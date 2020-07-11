package steiner.HougardyProemel;

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
    }

    private Tree(Node node, double weight) {
        this.node = node;
        this.cost = weight;
        this.children = new HashSet<Tree>();
    }

    public boolean addChild(Node from, Tree to, double cost) {
        if (node.equals(from)) {
            to.cost = cost;
            children.add(to);
            return true;
        }
        for (Tree child : children) {
            if (child.addChild(from, to, cost))
                return true;
        }
        return false;
    }

    public boolean addChild(Edge edge) {
        if (node.equals(edge.first)) {
            children.add(new Tree(edge.second, edge.cost));
            return true;
        }
        if (node.equals(edge.second)) {
            children.add(new Tree(edge.first, edge.cost));
            return true;
        }
        for (Tree child : children) {
            if (child.addChild(edge))
                return true;
        }
        return false;
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
        out.write(node.id + (node.terminal ? "[color=red]" : "[color=black]"));
        out.newLine();
        for (Tree child : children) {
            child.writeDotRec(out);
            out.write(node.id + "->" + child.node.id + "[label=" + String.format("%.2f", child.cost) + "]");
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

    @Override
    public boolean equals(Object other) {
        if (other.getClass().equals(this.getClass())) {
            return this.node == ((Tree) other).node;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    public Tree makeRoot(Node n) {
        if (!containsNode(n))
            return this;
        if (this.node.equals(n))
            return this;
        for (Tree child : children) {
            Tree result = child.makeRootRec(n, this);
            if (!result.equals(this)) {
                return result;
            }
        }
        // should never happen. want it to throw an exception
        return null;
    }

    private Tree makeRootRec(Node n, Tree parent) {
        if (this.node.equals(n)) {
            double tmp = this.cost;
            cost = parent.cost;
            parent.cost = tmp;
            parent.children.remove(this);
            children.add(parent);
            return this;
        }
        for (Tree child : children) {
            Tree result = child.makeRootRec(n, this);
            if (!result.equals(this)) {
                double tmp = parent.cost;
                parent.cost = result.cost;
                result.cost = tmp;
                parent.children.remove(this);
                children.add(parent);
                return result;
            }
        }
        return parent;
    }

    public Tree findNode(Node n) {
        if (node.equals(n)) {
            return this;
        }
        for (Tree child : children) {
            Tree result = child.findNode(n);
            if (result != null)
                return result;
        }
        return null;
    }

    public double totalCost() {
        double total = cost;
        for (Tree child : children) {
            total += child.totalCost();
        }
        return total;
    }

    public HashSet<Edge> toEdgeSet() {
        HashSet<Edge> out = new HashSet<>();
        for (Tree child : children) {
            out.add(new Edge(node, child.node, child.cost));
            out.addAll(child.toEdgeSet());
        }
        return out;
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

    public boolean containsAllNodes(HashSet<Node> set) {
        for (Node n : set) {
            if (!containsNode(n))
                return false;
        }
        return true;
    }

    public Tree replace(Node n, Tree t) {
        if (node.equals(n)) {
            return t;
        }
        HashSet<Tree> newChildren = new HashSet<>();
        for (Tree child : children) {
            newChildren.add(child.replace(n, t));
        }
        children = newChildren;
        return this;
    }

    public void changeCostOfN(Node n, double cost) {
        if (node.equals(n)) {
            this.cost = cost;
            return;
        }
        for (Tree child : children) {
            child.changeCostOfN(n, cost);
        }
    }

    public Tree removeNode(Node n) {
        if (this.node.equals(n)) {
            return null;
        }
        Tree changed = null;
        for (Tree child : children) {
            Tree result = child.removeNode(n);
            if (result == null)
                changed = child;
            else
                child = result;
        }
        if (changed != null)
            children.remove(changed);
        return this;
    }

    public Tree copy() {
        Tree out = new Tree(this.node, this.cost);
        for (Tree child : children) {
            out.addChild(this.node, child.copy(), child.cost);
        }
        return out;
    }

    public boolean isFull() {
        if (node.terminal ^ children.isEmpty())
            return false;
        for (Tree c : children) {
            if (!c.isFull())
                return false;
        }
        return true;
    }

    public HashSet<Node> getNodes() {
        HashSet<Node> set = new HashSet<>();
        set.add(this.node);
        for (Tree child : children) {
            set.addAll(child.getNodes());
        }
        return set;
    }

    public HashSet<Node> getAllTerminalNodes() {
        HashSet<Node> set = new HashSet<>();
        if (node.terminal)
            set.add(this.node);
        for (Tree child : children) {
            set.addAll(child.getAllTerminalNodes());
        }
        return set;
    }

    public HashSet<Node> getSteinerNodes() {
        HashSet<Node> out = new HashSet<>();
        if (!node.terminal)
            out.add(node);
        for (Tree child : children) {
            out.addAll(child.getSteinerNodes());
        }
        return out;
    }
}