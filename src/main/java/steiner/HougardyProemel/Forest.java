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

public class Forest {

    private HashSet<Tree> trees;

    public Forest() {
        trees = new HashSet<>();
    }

    public Forest(Tree t) {
        trees = new HashSet<>();
        trees.add(t.copy());
    }

    public boolean contains(Node n) {
        for (Tree tree : trees) {
            if (tree.containsNode(n))
                return true;
        }
        return false;
    }

    public void addTree(Tree t) {
        trees.add(t);
    }

    public Tree getTreeWithNode(Node n) {
        for (Tree tree : trees) {
            if (tree.containsNode(n))
                return tree;
        }
        return null;
    }

    public void addEdgeWithNewNode(Edge e) throws Exception {
        Tree from = getTreeWithNode(e.first);
        Tree to = getTreeWithNode(e.second);
        if (from == null && to == null)
            throw new Exception("Forest doesn't contain required nodes");
        Tree rebase = to;
        if (to == null) {
            rebase = new Tree(e.second);
        } else if (from == null) {
            from = new Tree(e.first);
            if (!to.node.equals(e.second)) {
                rebase = to.makeRoot(e.second);
            }
            trees.remove(to);
            trees.add(from);
        } else {
            if (from.equals(to))
                return; // Edge already exists
            if (!to.node.equals(e.second)) {
                rebase = to.makeRoot(e.second);
            }
            trees.remove(to);
        }
        from.addChild(e.first, rebase, e.cost);
    }

    public Tree giveSingleTree() throws Exception {
        if (trees.size() == 1)
            return trees.iterator().next();
        else if (trees.size() <= 0)
            throw new Exception("no tree in forest");
        else
            throw new Exception("Multiple Trees in Forest. Cant return single tree");
    }

    public boolean isSetConnected(HashSet<Node> set) {
        for (Tree t : trees) {
            if (t.containsAllNodes(set))
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
            for (Tree t : trees) {
                t.writeDotRec(out);
            }
            out.write("}");
            out.close();
        }
    }

    public boolean everyTreeContainsTerminal(HashSet<Node> terminals) {
        for (Tree t : trees) {
            HashSet<Node> tmp = t.getNodes();
            tmp.retainAll(terminals);
            if (tmp.isEmpty())
                return false;
        }
        return true;
    }

    public double totalCost() {
        double total = 0;
        for (Tree t : trees) {
            total += t.totalCost();
        }
        return total;
    }

    /*
     * public Tree union() throws Exception { Iterator<Tree> it= trees.iterator();
     * if (trees.size()==1) return it.next(); Tree t=it.next(); while(it.hasNext()){
     * t.incorporateOtherTree(it.next()); } return giveSingleTree(); }
     */

    public int size() {
        return trees.size();
    }

    public HashSet<Tree> getTrees() {
        return trees;
    }
}