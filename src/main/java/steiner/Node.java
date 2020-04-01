package steiner;

import java.util.ArrayList;
import java.util.Iterator;

public class Node {
    private String name;
    private ArrayList<Edge> edges;
    private boolean isTerminal;

    public Node(String name, boolean isTerminal) {
        this.name = name;
        this.isTerminal = isTerminal;
        this.edges = new ArrayList<>();
    }

    public boolean addEdge(Edge e) {
        if (!e.contains(this)) {
            return false;
        }
        edges.add(e);
        return true;
    }

    public String toString() {
        return (isTerminal ? "T_" : "S_") + this.name;
    }

    public String getName(){
        return name;
    }

    public String printEdges() {
        Iterator<Edge> it = edges.iterator();
        String s = "";
        while (it.hasNext()) {
            s += it.next()+"\n";
        }
        return s;
    }

	public void removeEdge(Edge e) {
        edges.remove(e);
	}
}