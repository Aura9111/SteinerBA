package steiner;

import java.util.HashSet;

public class myEvaluationResult {

    public HashSet<Node> terminals;
    public HashSet<TreeEdge> addSet;
    public HashSet<TreeEdge> removeSet;

    public myEvaluationResult(HashSet<Node> terminals, HashSet<TreeEdge> addSet, HashSet<TreeEdge> removeSet) {
        this.terminals = terminals;
        this.addSet = addSet;
        this.removeSet = removeSet;
    }

    public myEvaluationResult(HashSet<Node> terminals, SetPair setPair) {
        this.terminals = terminals;
        this.addSet = setPair.addSet;
        this.removeSet = setPair.removeSet;
	}

	public String toString() {
        return "Terminals: " + terminals + "\nRemove Set: " + removeSet + "\nAdd Set: " + addSet;
    }
}