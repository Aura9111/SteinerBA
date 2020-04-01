package steiner;

public class Edge {

    private String name;
    private Node first;
    private Node second;
    private double weight;

    public Edge(String name, Node n1, Node n2, double weight) {
        this.name=name;
        this.first = n1;
        this.second = n2;
        this.weight = weight;
    }

    public String toString() {
        return name;
    }

    public double getWeight(){
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o.getClass() != this.getClass())
            return false;
        Edge other = (Edge) o;
        return ((this.first == other.first && this.second == other.second)
                || (this.first == other.second && this.second == other.first));
    }

    public boolean contains(Node n) {
        return n == this.first || n == this.second;
    }
}