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
    public HashMap<String, GraphNode> GraphNodes;
    public HashMap<String, GraphEdge> edges;
    public int edgeCount;
    public int GraphNodeCount;

    public MyGraph(String name) {
        this.path = "example/" + name;
        this.GraphNodes = new HashMap<>();
        this.edges = new HashMap<>();
        this.edgeCount = 0;
        this.GraphNodeCount = 0;
    }

    public MyGraph(HashMap<String, GraphNode> GraphNodes, HashMap<String, GraphEdge> edges, String name) {
        this.path = "example/" + name;
        this.GraphNodes = GraphNodes;
        this.edges = edges;
        this.GraphNodeCount = GraphNodes.size();
        this.edgeCount = edges.size();
    }

    public void removeGraphNode(String GraphNode) {

    }

    public void addNode(String GraphNode, boolean isTerminal) {
        GraphNode n = new GraphNode(GraphNode, isTerminal);
        GraphNodes.put(GraphNode, n);
        GraphNodeCount++;
    }

    public boolean addEdge(String GraphNode1, String GraphNode2, double weight) {
        if (GraphNodes.containsKey(GraphNode1) && GraphNodes.containsKey(GraphNode2)
                && !edges.containsKey(GraphNode1 + "--" + GraphNode2)
                && !edges.containsKey(GraphNode2 + "--" + GraphNode1) && !GraphNode1.equals(GraphNode2)) {
            GraphNode n1 = GraphNodes.get(GraphNode1);
            GraphNode n2 = GraphNodes.get(GraphNode2);
            String edgeName = GraphNode1 + "--" + GraphNode2;
            GraphEdge e = new GraphEdge(edgeName, n1, n2, weight);
            n1.addEdge(e);
            n2.addEdge(e);
            edges.put(edgeName, e);
            edgeCount++;
        }
        return false;
    }

    public Optional<GraphEdge> removeEdge(String GraphNode1, String GraphNode2) {
        if (edges.containsKey(GraphNode1 + "--" + GraphNode2)) {
            edgeCount--;
            return Optional.of(edges.remove(GraphNode1 + "--" + GraphNode2));
        }
        if (edges.containsKey(GraphNode2 + "--" + GraphNode1)) {
            edgeCount--;
            return Optional.of(edges.remove(GraphNode2 + "--" + GraphNode1));
        }
        return Optional.empty();
    }

    public Optional<GraphEdge> getEdge(String GraphNode1, String GraphNode2) {
        if (edges.containsKey(GraphNode1 + "--" + GraphNode2)) {
            return Optional.of(edges.get(GraphNode1 + "--" + GraphNode2));
        }
        if (edges.containsKey(GraphNode2 + "--" + GraphNode1)) {
            return Optional.of(edges.get(GraphNode2 + "--" + GraphNode1));
        }
        return Optional.empty();
    }

    public String toString() {
        return "Hi I'm a Graph with " + edgeCount + " Edges and " + GraphNodeCount + " GraphNodes\n";
    }

    public void writeDot() throws IOException {
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".dot")))) {
            out.write("graph {");
            out.newLine();
            for (GraphNode n : GraphNodes.values()) {
                out.write(n.getName() + (n.isTerminal() ? "[color=red]" : "[color=blue]"));
            }

            for (GraphEdge e : edges.values()) {
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
            Graphviz.fromGraph(g).width(1920).render(Format.SVG).toFile(new File(path + ".svg"));
        }
    }

    public boolean inSameComponent(String GraphNode1, String GraphNode2) {
        if (!GraphNodes.containsKey(GraphNode1) || !GraphNodes.containsKey(GraphNode2))
            return false;
        return GraphNodes.get(GraphNode1).isInSameComponent(GraphNodes.get(GraphNode2));
    }

    public int numberOfComponents() {
        int i = 0;
        HashSet<GraphNode> tmp = new HashSet<GraphNode>(GraphNodes.values());
        while (!tmp.isEmpty()) {
            Iterator<GraphNode> it = tmp.iterator();
            tmp.removeAll(it.next().getGraphNodesInComponent(new HashSet<GraphNode>()));
            i++;
        }
        return i;
    }

    public HashSet<GraphNode> getNodesinComponent(String GraphNode) {
        return GraphNodes.get(GraphNode).getGraphNodesInComponent(new HashSet<GraphNode>());
    }

    public HashSet<GraphNode> getNodesNotinComponent(String GraphNode) {
        HashSet<GraphNode> tmp = new HashSet<GraphNode>(GraphNodes.values());
        tmp.removeAll(GraphNodes.get(GraphNode).getGraphNodesInComponent(new HashSet<GraphNode>()));
        return tmp;
    }

    public HashSet<GraphNode> getNodesNotConnected(String GraphNode) {
        HashSet<GraphNode> tmp = new HashSet<GraphNode>(GraphNodes.values());
        tmp.removeAll(GraphNodes.get(GraphNode).getNeighbors());
        return tmp;
    }
}
