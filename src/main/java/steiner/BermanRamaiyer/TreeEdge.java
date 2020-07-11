package steiner.BermanRamaiyer;

public class TreeEdge {
    public Tree from;
    public Tree to;
    public double cost;

    public TreeEdge(Tree from, Tree to){
        this.from=from;
        this.to=to;
        this.cost=to.cost;
    }

    public TreeEdge(Tree from, Tree to, double cost){
        this.from=from;
        this.to=to;
        this.cost=cost;
    }

    public TreeEdge(Tree t, Node nFrom, Node nTo, double cost) {
        this.from=t.findNode(nFrom);
        this.to = t.findNode(nTo);
        this.cost=cost;
	}

	public String toString(){
        return from.node.getName() +"->"+ to.node.getName();
    }
}