package steiner.BermanRamaiyer;

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
        /*
         * for (Edge e : node.getEdges()) { Node opp = e.opposite(node);
         * opp.removeEdge(e); children.add(new Tree(opp, e.cost)); }
         */
    }

    private Tree(Node node, double weight) {
        this.node = node;
        this.cost = weight;
        this.children = new HashSet<Tree>();
        /*
         * for (Edge e : node.getEdges()) { Node opp = e.opposite(node);
         * opp.removeEdge(e); children.add(new Tree(opp, e.cost)); }
         */
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

    public boolean combineWith(Tree other, Edge e) {
        if (node.equals(e.first)) {
            other.setWeight(e.cost);
            children.add(other);
            return true;
        }
        if (node.equals(e.second)) {
            other.setWeight(e.cost);
            children.add(other);
            return true;
        }
        boolean bool = false;
        for (Tree child : children) {
            bool = bool || child.combineWith(other, e);
        }
        return bool;
    }

    private void setWeight(double weight) {
        this.cost = weight;
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
            Graphviz.fromGraph(g).width(600).render(Format.SVG).toFile(new File("example/tree.svg"));
        }
    }

    public void printGraph(String path) throws IOException {
        writeDot(path);
        try (InputStream dot = new FileInputStream(("example/" + path + ".dot"))) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(600).render(Format.SVG).toFile(new File("example/" + path + ".svg"));
        }
    }

    public HashSet<Node> getAllTerminalNodes() {
        HashSet<Node> set = new HashSet<>();
        if (node.isTerminal())
            set.add(this.node);
        for (Tree child : children) {
            set.addAll(child.getAllTerminalNodes());
        }
        return set;
    }

    public HashSet<Tree> getAllTerminalTreeNodes() {
        HashSet<Tree> set = new HashSet<>();
        if (node.isTerminal())
            set.add(this);
        for (Tree child : children) {
            set.addAll(child.getAllTerminalTreeNodes());
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

    public int getNumberOfNonTerminals() {
        int i = node.isTerminal() ? 0 : 1;
        for (Tree child : children) {
            i += child.getNumberOfTerminals();
        }
        return i;
    }

    private TreeEdge recMaxCostConnectingEdge(HashSet<Node> terminals, TreeEdge e) {
        for (Tree child : children) {
            HashSet<Node> childSet = child.getAllTerminalNodes();
            if (!childSet.containsAll(terminals)) {
                childSet.retainAll(terminals);
                if (!childSet.isEmpty()) {
                    if (e == null)
                        e = new TreeEdge(this, child);
                    if (child.cost > e.cost)
                        e = new TreeEdge(this, child);
                }
            }
            e = child.recMaxCostConnectingEdge(terminals, e);
        }
        return e;
    }

    public TreeEdge getMaxCostConnectingEdge(HashSet<Node> terminals) {
        if (children.size() == 0)
            return null;
        TreeEdge e = recMaxCostConnectingEdge(terminals, null);
        return e;
    }

    public Tree removeEdge(Node from, Node to) {
        if (node.equals(from)) {
            for (Tree child : children) {
                if (child.node.equals(to)) {
                    child.cost = 0;
                    children.remove(child);
                    return child;
                }
            }
            return null;
        }
        for (Tree child : children) {
            Tree out = child.removeEdge(from, to);
            if (out != null)
                return out;
        }
        return null;
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

    public HashSet<HashSet<Node>> getXElementSubsets(int k) {
        HashSet<Node> terminals = this.getAllTerminalNodes();
        return getXElementSubsets(k, new MyImmutableHashSet<Node>(terminals), new MyImmutableHashSet<Node>());
    }

    private HashSet<HashSet<Node>> getXElementSubsets(int k, MyImmutableHashSet<Node> in,
            MyImmutableHashSet<Node> out) {
        HashSet<HashSet<Node>> setOfSets = new HashSet<>();
        if (out.getSize() == k) {
            setOfSets.add(out.getHashSet());
            return setOfSets;
        }
        for (Node n : in) {
            setOfSets.addAll(getXElementSubsets(k, in.remove(n), out.add(n)));
        }
        return setOfSets;
    }

    public HashSet<Node> splitTermOnEdge(TreeEdge e, HashSet<Node> terminals) {
        if (e.to.equals(this)) {
            HashSet<Node> set = getAllTerminalNodes();
            HashSet<Node> out = new HashSet<>();
            for (Node n : set) {
                if (terminals.contains(n)) {
                    out.add(n);
                }
            }
            return out;
        }
        for (Tree child : children) {
            HashSet<Node> result = child.splitTermOnEdge(e, terminals);
            if (result != null)
                return result;
        }
        return null;
    }

    public String toString() {
        return "Tree: " + node.getName();
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

    public HashSet<Node> getNodes() {
        HashSet<Node> set = new HashSet<>();
        set.add(this.node);
        for (Tree child : children) {
            set.addAll(child.getNodes());
        }
        return set;
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

    public boolean containsAllTreeEdges(HashSet<TreeEdge> set) {
        for (TreeEdge e : set) {
            if (!containsTreeEdge(e))
                return false;
        }
        return true;
    }

    private boolean containsTreeEdge(TreeEdge e) {
        if (this.equals(e.from)) {
            return children.contains(e.to);
        }
        for (Tree child : children) {
            if (child.containsTreeEdge(e))
                return true;
        }
        return false;
    }

    public HashSet<TreeEdge> toTreeEdgeSet() {
        HashSet<TreeEdge> out = new HashSet<>();
        for (Tree child : children) {
            out.add(new TreeEdge(this, child));
            out.addAll(child.toTreeEdgeSet());
        }
        return out;
    }

    public HashSet<Edge> toEdgeSet() {
        HashSet<Edge> out = new HashSet<>();
        for (Tree child : children) {
            for (Edge e : node.getEdges()) {
                if (e.opposite(node).equals(child.node)) {
                    out.add(e);
                }
            }
            out.addAll(child.toEdgeSet());
        }
        return out;
    }

    public boolean containsEdge(TreeEdge e) {
        if (this.equals(e.from)) {
            for (Tree child : children) {
                if (child.equals(e.to))
                    return true;
            }
            return false;
        }
        for (Tree child : children) {
            if (child.containsEdge(e))
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

    public boolean leadsToNodeFromSet(HashSet<Node> set) {
        if (set.contains(this.node))
            return true;
        for (Tree child : children) {
            if (child.leadsToNodeFromSet(set))
                return true;
        }
        return false;
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

    public Tree copyWithSetContracted(HashSet<Node> set) {
        Tree newSelf = new Tree(node);
        for (Tree child : children) {
            Tree newChild = child.copyWithSetContracted(set);
            if (set.contains(node) && set.contains(child.node)) {

            } else {
                newChild.cost = child.cost;
            }
            newSelf.addChild(node, newChild, newChild.cost);
        }
        return newSelf;
    }

    public boolean isFull() {
        if (node.isTerminal() ^ children.isEmpty())
            return false;
        for (Tree c : children) {
            if (!c.isFull())
                return false;
        }
        return true;
    }
    /*
     * public Tree swapNodes(Node n1, Node n2) throws Exception { Tree t2 = null; if
     * (node.equals(n1)) { for (Tree c : children) { if (c.node.equals(n2)) { t2 =
     * c; } } } else if (node.equals(n2)) { for (Tree c : children) { if
     * (c.node.equals(n1)) { t2 = c; } } } else {
     * 
     * } if (t2 == null) throw new Exception("cant swap " + n1 + " and " + n2 +
     * " because they arent connected"); double tmp = t2.cost; t2.cost = this.cost;
     * this.cost = tmp; children.remove(t2); t2.children.add(this); return t2; }
     */

    public HashSet<Node> getSteinerNodes() {
        HashSet<Node> out = new HashSet<>();
        if (!node.isTerminal())
            out.add(node);
        for (Tree child : children) {
            out.addAll(child.getSteinerNodes());
        }
        return out;
    }

    public boolean containsEdge(Edge e) {
        if (this.node.equals(e.first)) {
            for (Tree child : children) {
                if (child.node.equals(e.second))
                    return true;
            }
        }
        if (this.node.equals(e.second)) {
            for (Tree child : children) {
                if (child.node.equals(e.first))
                    return true;
            }
        }
        for (Tree child : children) {
            if (child.containsEdge(e))
                return true;
        }
        return false;
    }
}