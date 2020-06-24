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

    public void addEdge(TreeEdge e) throws Exception {
        Tree from = getTreeWithNode(e.from.node);
        Tree to = getTreeWithNode(e.to.node);
        if (from == null || to == null)
            throw new Exception("Forest doesn't contain required nodes");
        Tree rebase = to;
        if (!to.node.equals(e.to.node)) {
            rebase = to.makeRoot(e.to.node);
        }
        trees.remove(to);
        from.addChild(e.from.node, rebase, e.cost);
    }

    public void addEdgeSmt(TreeEdge e) throws Exception {
        Tree from = getTreeWithNode(e.from.node);
        Tree to = getTreeWithNode(e.to.node);
        if (from == null && to == null)
            throw new Exception("Forest doesn't contain required nodes");
        Tree rebase = to;
        if (to == null) {
            rebase = new Tree(e.to.node);
        } else if (from == null) {
            from = new Tree(e.from.node);
            if (!to.node.equals(e.to.node)) {
                rebase = to.makeRoot(e.to.node);
            }
            trees.remove(to);
            trees.add(from);
        } else {
            if (from.equals(to))
                return; // Edge already exists
            if (!to.node.equals(e.to.node)) {
                rebase = to.makeRoot(e.to.node);
            }
            trees.remove(to);
        }
        from.addChild(e.from.node, rebase, e.cost);
    }

    public void removeEdge(TreeEdge e) throws Exception {
        for (Tree t : trees) {
            if (t.containsNode(e.from.node)) {
                Tree to = t.removeEdge(e.from.node, e.to.node);
                if (to == null) {
                    to = t.removeEdge(e.to.node, e.from.node);
                    if (to == null)
                        throw new Exception("Edge not found");
                }
                trees.add(to);
                return;
            }
        }
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

    public boolean wouldEdgeConnectSet(HashSet<Node> set, TreeEdge e) throws Exception {
        HashSet<Tree> copy=new HashSet<>();
        for (Tree t: trees){
            copy.add(t.copy());
        }
        addEdgeSmt(e);
        boolean out = isSetConnected(set);
        trees=copy;
        return out;
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
}