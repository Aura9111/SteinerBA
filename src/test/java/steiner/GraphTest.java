package steiner;

import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;

import guru.nidi.graphviz.*;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;

public class GraphTest {

    public static void main(String[] args) throws IOException {
        MutableGraph g = mutGraph("example1").setDirected(true).use((gr, ctx) -> {
            mutNode("b");
            nodeAttrs().add(Color.RED);
            mutNode("a").addLink(mutNode("b"));
        });
        Graphviz.fromGraph(g).width(200).render(Format.PNG).toFile(new File("example/ex1i.png"));
    }
    /*
     * @Test public void testConnectNodes() { Graph g = new Graph();
     * g.addNode(true); g.addNode(false); assertTrue(g.connectNodes(0, 1, 3));
     * assertFalse(g.connectNodes(0, 1, 3)); assertFalse(g.connectNodes(2, 1, 3));
     * assertFalse(g.connectNodes(0, 2, 3));
     * 
     * }
     * 
     * @Test public void test50NodesGraphs() { Graph g = new Graph(); Random RNGesus
     * = new Random(); for (int i = 0; i < 50; i++) { g.addNode(RNGesus.nextInt(2)
     * == 0); assertEquals(g.nodeCount, g.nodes.size()); } for (int i = 0; i <
     * RNGesus.nextInt(500); i++) { int n1 = RNGesus.nextInt(50); int n2 =
     * RNGesus.nextInt(50); assertEquals(!g.getEdge(n1, n2).isPresent(),
     * g.connectNodes(n1, n2, RNGesus.nextInt(20))); assertEquals(g.edgeCount,
     * g.edges.size()); } System.out.println(g.printGraph()); }
     */
}