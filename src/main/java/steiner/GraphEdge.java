package steiner;

public class GraphEdge {

    private String name;
    private GraphNode first;
    private GraphNode second;
    private double weight;

    public GraphEdge(String name, GraphNode n1, GraphNode n2, double weight) {
        this.name = name;
        this.first = n1;
        this.second = n2;
        this.weight = weight;
    }

    public String toString() {
        return name;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (o.getClass() != this.getClass())
            return false;
        GraphEdge other = (GraphEdge) o;
        return ((this.first == other.first && this.second == other.second)
                || (this.first == other.second && this.second == other.first));
    }

    public boolean contains(GraphNode n) {
        return n == this.first || n == this.second;
    }

    public String getName() {
        return name;
    }

    public GraphNode opposite(GraphNode GraphNode) {
        return first.equals(GraphNode) ? second : first;
    }
}