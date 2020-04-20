package steiner;

import java.util.HashSet;

public class SetPair {

    public HashSet<TreeEdge> addSet;
    public HashSet<TreeEdge> removeSet;

    public SetPair() {
        this.addSet = new HashSet<>();
        this.removeSet = new HashSet<>();
    }

    public SetPair(HashSet<TreeEdge> addSet, HashSet<TreeEdge> removeSet) {
        this.addSet = addSet;
        this.removeSet = removeSet;
    }

    public void add(SetPair other) {
        this.addSet.addAll(other.addSet);
        this.removeSet.addAll(other.removeSet);
    }

    public String toString(){
        return "Remove Set: "+ removeSet + "\nAdd Set: " +addSet;
    }
}
