package steiner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class GraphNode {
    private String name;
    private ArrayList<GraphEdge> edges;
    private boolean terminal;

    public GraphNode(String name, boolean isTerminal) {
        this.name = name;
        this.terminal = isTerminal;
        this.edges = new ArrayList<>();
    }

    public boolean addEdge(GraphEdge e) {
        if (!e.contains(this)) {
            return false;
        }
        edges.add(e);
        return true;
    }

    public String toString() {
        return (terminal ? "T_" : "S_") + this.name;
    }

    public String getName() {
        return name;
    }

    public String printEdges() {
        Iterator<GraphEdge> it = edges.iterator();
        String s = "";
        while (it.hasNext()) {
            s += it.next() + "\n";
        }
        return s;
    }

    public void removeEdge(GraphEdge e) {
        edges.remove(e);
    }

    public boolean isTerminal() {
        return terminal;
    }

    public boolean isNeighborTo(GraphNode n) {
        for (GraphEdge e : edges) {
            if (e.contains(n))
                return true;
        }
        return false;
    }

    public boolean isInSameComponent(GraphNode find) {
        if (this.equals(find))
            return true;
        boolean output = false;
        for (GraphEdge e : edges) {
            GraphNode n = e.opposite(this);
            output = output || n.isInSameComponent(this, find);
        }
        return output;
    }

    private boolean isInSameComponent(GraphNode start, GraphNode find) {
        if (this.equals(start))
            return false;
        if (this.equals(find))
            return true;
        boolean output = false;
        for (GraphEdge e : edges) {
            GraphNode n = e.opposite(this);
            output = output || n.isInSameComponent(start, find);
        }
        return output;
    }

    public HashSet<GraphNode> getGraphNodesInComponent(HashSet<GraphNode> set) {
        if (set.contains(this))
            return new HashSet<GraphNode>();
        set.add(this);
        for (GraphEdge e : edges) {
            e.opposite(this).getGraphNodesInComponent(set);
        }
        return set;
    }

    public HashSet<GraphNode> getNeighbors() {
        HashSet<GraphNode> set = new HashSet<GraphNode>();
        for (GraphEdge e : edges) {
            set.add(e.opposite(this));
        }
        return set;
    }
}