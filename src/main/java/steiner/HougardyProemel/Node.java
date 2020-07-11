package steiner.HougardyProemel;

public class Node {
    public boolean terminal;
    public int id;

    public Node(int id, boolean isTerminal) {
        this.terminal = isTerminal;
        this.id = id;
    }

    public String toString() {
        return (terminal ? "T_" : "S_") + this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (!this.getClass().equals(o.getClass()))
            return false;
        return this.id == (((Node) o).id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    public Node copy() {
        return new Node(id, terminal);
    }
}