package steiner;

import java.util.HashSet;

public class SetPair {

    public HashSet<Edge> addSet;
    public HashSet<Edge> removeSet;

    public SetPair() {
        this.addSet = new HashSet<>();
        this.removeSet = new HashSet<>();
    }

    public SetPair(HashSet<Edge> addSet, HashSet<Edge> removeSet) {
        this.addSet = addSet;
        this.removeSet = removeSet;
    }

    public void add(SetPair other) {
        this.addSet.addAll(other.addSet);
        this.removeSet.addAll(other.removeSet);
    }

}
