package steiner;

import java.util.ArrayList;

public class MyTree {
    private ArrayList<TreeEdge> children;
    private TreeNode root;
    
    public MyTree(TreeNode root){
        this.root=root;
        this.children=new ArrayList<TreeEdge>();
    }

    public TreeNode getRoot(){
        return root;
    }

    public ArrayList<TreeEdge> getChildren(){
        return children;
    }

    public void addChild(TreeEdge child){
        children.add(child);
    }

}