package steiner.BermanRamaiyer;

public class Edge {

    private String name;
    public Node first;
    public Node second;
    public double cost;

    public Edge(String name, Node n1, Node n2, double cost) {
        this.name = name;
        this.first = n1;
        this.second = n2;
        this.cost = cost;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o.getClass() != this.getClass())
            return false;
        Edge other = (Edge) o;
        return ((this.first.equals(other.first) && this.second.equals(other.second))
                || (this.first.equals(other.second) && this.second.equals(other.first)));
    }

    @Override
    public int hashCode() {
        return first.hashCode()+second.hashCode();
    }

    public boolean contains(Node n) {
        return n == this.first || n == this.second;
    }

    public String getName() {
        return name;
    }

    public Node opposite(Node node) {
        return first.equals(node) ? second : first;
    }
}