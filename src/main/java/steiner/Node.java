package steiner;

import java.util.HashSet;
import java.util.Iterator;

public class Node {
    private String name;
    private HashSet<Edge> edges;
    private boolean terminal;

    public Node(String name, boolean isTerminal) {
        this.name = name;
        this.terminal = isTerminal;
        this.edges = new HashSet<Edge>();
    }

    public boolean addEdge(Edge e) {
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

    public HashSet<Edge> getEdges(){
        return edges;
    }

    public int getDegree(){
        return edges.size();
    }

    public String printEdges() {
        Iterator<Edge> it = edges.iterator();
        String s = "";
        while (it.hasNext()) {
            s += it.next() + "\n";
        }
        return s;
    }

    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    public boolean isTerminal() {
        return terminal;
    }

    public boolean isNeighborTo(Node n) {
        for (Edge e : edges) {
            if (e.contains(n))
                return true;
        }
        return false;
    }

    public boolean isInSameComponent(Node find) {
        if (this.equals(find))
            return true;
        boolean output = false;
        for (Edge e : edges) {
            Node n = e.opposite(this);
            output = output || n.isInSameComponent(this, find);
        }
        return output;
    }

    private boolean isInSameComponent(Node start, Node find) {
        if (this.equals(start))
            return false;
        if (this.equals(find))
            return true;
        boolean output = false;
        for (Edge e : edges) {
            Node n = e.opposite(this);
            output = output || n.isInSameComponent(start, find);
        }
        return output;
    }

    public HashSet<Node> getNodesInComponent(HashSet<Node> set) {
        if (set.contains(this))
            return new HashSet<Node>();
        set.add(this);
        for (Edge e : edges) {
            e.opposite(this).getNodesInComponent(set);
        }
        return set;
    }

    public HashSet<Node> getNeighbors() {
        HashSet<Node> set = new HashSet<Node>();
        for (Edge e : edges) {
            set.add(e.opposite(this));
        }
        return set;
    }
}