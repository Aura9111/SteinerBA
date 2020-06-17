package steiner;

import java.util.HashSet;

public class Forest {
    
    private HashSet<Tree> trees;

    public Forest(){
        trees=new HashSet<>();
    }

    public Forest(Tree t){
        trees=new HashSet<>();
        trees.add(t);
    }

    public boolean contains(Node n){
        for (Tree tree: trees){
            if (tree.containsNode(n)) return true;
        }
        return false;
    }

    public void addTree(Tree t){
        trees.add(t);
    }

    public Tree getTreeWithNode(Node n){
        for (Tree tree: trees){
            if (tree.containsNode(n)) return tree;
        }
        return null;
    }

    public void addEdge(TreeEdge e) throws Exception {
        Tree from=getTreeWithNode(e.from.node);
        Tree to=getTreeWithNode(e.to.node);
        if (from==null||to==null) throw new Exception("Forest doesn't contain required nodes");
        trees.remove(to);
        from.addChild(e.from.node, to, e.cost);
    }

    public void removeEdge(TreeEdge e) throws Exception {
        for (Tree t: trees){
            if (t.containsNode(e.from.node)){
                Tree to=t.removeEdge(e.from.node, e.to.node);
                if (to==null) throw new Exception("Edge not found");
                trees.add(to);
                return;
            }
        }
    }

    public Tree giveSingleTree() throws Exception {
        if (trees.size()==1) return trees.iterator().next();
        else if (trees.size()<=0) throw new Exception("no tree in forest");
        else throw new Exception("Multiple Trees in Forest. Cant return single tree");
    }

    public boolean isSetConnected(HashSet<Node> set){
        for(Tree t: trees){
            if (t.containsAllNodes(set)) return true;
        }
        return false;
    }

    public boolean wouldEdgeConnectSet(HashSet<Node> set, TreeEdge e) throws Exception {
        addEdge(e);
        boolean out=isSetConnected(set);
        removeEdge(e);
        return out;
    }
}