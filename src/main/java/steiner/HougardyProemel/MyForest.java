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

public class MyForest {

    public HashSet<MyTree> trees;

    public MyForest() {
        trees = new HashSet<>();
    }

    public MyForest(MyTree t) {
        trees = new HashSet<>();
        trees.add(t.copy());
    }

    public boolean contains(Node n) {
        for (MyTree t : trees) {
            if (t.nodes.contains(n))
                return true;
        }
        return false;
    }

    public void addMyTree(MyTree t) {
        trees.add(t);
    }

    public MyTree getTreeWithNode(Node n) {
        for (MyTree t : trees) {
            if (t.nodes.contains(n))
                return t;
        }
        return null;
    }

    public void addEdgeWithNewNode(Edge e) throws Exception {
        MyTree from = getTreeWithNode(e.first);
        MyTree to = getTreeWithNode(e.second);
        if (from == null && to == null)
            throw new Exception("Forest doesn't contain required nodes");
        if (to == null) {
            to = new MyTree(e.second);
        }
        if (from == null) {
            from = new MyTree(e.first);
        }
        to.makeRoot(e.second);
        from.addTree(e, to);
        trees.remove(to);
    }

    public MyTree giveSingleMyTree() throws Exception {
        if (trees.size() == 1)
            return trees.iterator().next();
        else if (trees.size() <= 0)
            throw new Exception("no MyTree in forest");
        else
            throw new Exception("Multiple trees in Forest. Cant return single MyTree");
    }

    public boolean isSetConnected(HashSet<Node> set) {
        for (MyTree t : trees) {
            if (t.nodes.containsAll(set))
                return true;
        }
        return false;
    }

    public void printGraph(String path) throws IOException {
        writeDot(path);
        try (InputStream dot = new FileInputStream(("example/" + path + ".dot"))) {
            MutableGraph g = new Parser().read(dot);
            Graphviz.fromGraph(g).width(800).render(Format.SVG).toFile(new File("example/" + path + ".svg"));
        }
    }

    public void writeDot(String path) throws IOException {
        try (BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream("example/" + path + ".dot")))) {
            out.write("digraph {");
            out.newLine();
            for (MyTree t : trees) {
                t.writeDotRec(out);
            }
            out.write("}");
            out.close();
        }
    }

    public boolean everyMyTreeContainsTerminal(HashSet<Node> terminals) {
        for (MyTree t : trees) {
            HashSet<Node> tmp = t.nodes;
            tmp.retainAll(terminals);
            if (tmp.isEmpty())
                return false;
        }
        return true;
    }

    public double totalCost() {
        double total = 0;
        for (MyTree t : trees) {
            total += t.totalCost();
        }
        return total;
    }

    public int size() {
        return trees.size();
    }

    public HashSet<Edge> toEdgeSet() {
        HashSet<Edge> out = new HashSet<>();
        for (MyTree t : trees) {
            out.addAll(t.edges);
        }
        return out;
    }

    public void addEdge(Edge e) {
        if (e.first.equals(e.second))
            return;
        MyTree from = getTreeWithNode(e.first);
        MyTree to = getTreeWithNode(e.second);
        if (to == null) {
            to = new MyTree(e.second);
            trees.add(to);
        }
        if (from == null) {
            from = new MyTree(e.first);
            trees.add(from);
        }
        if (to.equals(from))
            return;
        to.makeRoot(e.second);
        from.addTree(e, to);
        trees.remove(to);
    }

    public void splitIntoFullComponents(HashSet<Node> terminals) {
        // while (!isFull(terminals))
        for (Edge e : toEdgeSet()) {
            if (terminals.contains(e.first)) {
                removeEdge(e);
                // break;
            }
        }
    }

    public void removeEdge(Edge e) {
        if (e.first.equals(e.second))
            return;
        MyTree from = getTreeWithEdge(e);
        if (from == null)
            return;
        trees.remove(from);
        MyTree to = from.removeEdge(e);
        trees.add(from);
        trees.add(to);
    }

    private MyTree getTreeWithEdge(Edge e) {
        for (MyTree t : trees) {
            if (t.edges.contains(e))
                return t;
        }
        return null;
    }

    public boolean isFull(HashSet<Node> terminals) {
        for (MyTree t : trees) {
            if (!t.isFull(terminals))
                return false;
        }
        return true;
    }

    public int degree(Node n) {
        return getTreeWithNode(n).getEdgesOfNode(n).size();
    }

    public HashSet<Node> toNodeSet() {
        HashSet<Node> out = new HashSet<>();
        for (MyTree t : trees) {
            out.addAll(t.nodes);
        }
        return out;
    }
}