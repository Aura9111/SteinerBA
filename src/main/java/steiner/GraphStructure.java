package steiner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

public interface GraphStructure {

    public boolean addEdge(String nodeName1, String nodeName2, double weight);

    public boolean containsEdge(String nodeName1, String nodeName2);
    
    public boolean containsNode(String nodeName);

    public Optional<Node> getNode(String nodeName);

    public Optional<Edge> getEdge(String nodeName1, String nodeName2);
    
    public Optional<Component> getComponent(String nodeName);

    public HashSet<Node> getNeighborNodes(String nodeName);

    public HashSet<Node> getNonNeighborNodes(String nodeName);

    public HashSet<Node> getAllTerminalNodes();

    public HashSet<Node> getAllSteinerNodes();

    public HashSet<Component> removeNode(String nodeName);

    public Optional<Component> removeEdge(String nodeName1, String nodeName2);

    public boolean isInSameComponent(String nodeName1, String nodeName2);

    public int numberOfComponents();

    public boolean hasCircles();
    
    public void printGraph() throws IOException;
}