package steiner.BermanRamaiyer;

import java.util.HashSet;
import java.util.Iterator;

public class Node {
    public String name;
    private HashSet<Edge> edges;
    private boolean terminal;
    public int id;

    public Node(String name, boolean isTerminal) {
        this.name = name;
        this.terminal = isTerminal;
        this.edges = new HashSet<Edge>();
        this.id=Integer.parseInt(name)-1;
    }

    public Node(int id, boolean isTerminal) {
        this.name = ""+(1+id);
        this.terminal = isTerminal;
        this.edges = new HashSet<Edge>();
        this.id=id;
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

    public HashSet<Edge> getEdges() {
        return edges;
    }

    public int getDegree() {
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
        HashSet<Node> set = getNodesInComponent(new HashSet<Node>());
        return set.contains(find);
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

    @Override
    public boolean equals(Object o) {
        if (!this.getClass().equals(o.getClass()))
            return false;
        return this.id==((Node) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public boolean stillInComponentWithTerminal(Edge e) {
        this.edges.remove(e);
        HashSet<Node> set = getNodesInComponent(new HashSet<Node>());
        for (Node n : set) {
            if (n.terminal) {
                edges.add(e);
                return true;
            }
        }
        edges.add(e);
        return false;
    }

	public void removeNeighbor(Node other) {
        if (getNeighbors().contains(other)){
            for (Edge e:edges){
                if (e.opposite(this).equals(other)){
                    edges.remove(e);
                }
            }
        }
	}

	public Node copy() {
        Node out= new Node(id, terminal);
        for(Edge e: edges){
            out.edges.add(new Edge(e.getName(), e.first, e.second, e.cost));
            }
        return out;
	}
}