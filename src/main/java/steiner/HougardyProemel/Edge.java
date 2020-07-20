package steiner.HougardyProemel;

public class Edge {

    private String name;
    public Node first;
    public Node second;
    public double cost;

    public Edge(Node n1, Node n2, double cost) {
        this.name = n1.id + "-" + n2.id;
        this.first = n1;
        this.second = n2;
        this.cost = cost;
    }

    public void reverse() {
        Node tmp = first;
        first = second;
        second = tmp;
        name = first.id + "-" + second.id;
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
        return ((this.first == other.first && this.second == other.second)
                || (this.first == other.second && this.second == other.first));
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
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

    public Edge copy() {
        return new Edge(first, second, cost);
    }
}