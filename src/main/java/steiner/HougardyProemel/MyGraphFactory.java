package steiner.HougardyProemel;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

public class MyGraphFactory {

    public static void main(String[] args) throws Exception {
        for (MyGraph g : getAllGraphs()) {
            MyTree t = g.MstApproximation(g.getTerminalNodes());
            t.printGraph(g.name);
            System.out.println(t.totalCost() + "/" + g.optimal);
        }
    }

    public static HashSet<MyGraph> getAllGraphs() throws IOException {
        HashSet<MyGraph> out = new HashSet<>();
        for(int i=0;i<=3;i++){
            for(int j=1;j<=5;j++){
                out.add(makeMyGraphMethods(i+"1"+j));
                out.add(makeMyGraphMethods(i+"2"+j));
                out.add(makeMyGraphMethods(i+"4"+j));
            }
        }
        return out;
    }

    public static MyGraph makeMyGraphMethods(String name) throws IOException {
        String path = "D:\\I080\\i080-" + name;
        Scanner scanner = new Scanner(new File(path + ".stp"));
        // create Nodes
        int nodes = 0;
        MyGraph g = null;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.startsWith("Nodes")) {
                String[] split = line.split(" ");
                nodes = Integer.parseInt(split[split.length - 1]);
                g = new MyGraph(name, nodes);
            }
            if (line.matches("SECTION Terminals")) {
                line = scanner.nextLine();
                String[] split = line.split(" ");
                int x = Integer.parseInt(split[split.length - 1]);
                for (int i = 0; i < x; i++) {
                    g.addNode(new Node(i, true));
                }
                for (int i = x; i < nodes; i++) {
                    g.addNode(new Node(i, false));
                }
            }
        }
        scanner.close();
        // create Edges
        scanner = new Scanner(new File(path + ".stp"));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("E ")) {
                String[] split = line.split(" ");
                int nodeID1 = Integer.parseInt(split[1]) - 1;
                int nodeID2 = Integer.parseInt(split[2]) - 1;
                double cost = Double.parseDouble(split[3]);
                Edge e = new Edge(g.nodes[nodeID1], g.nodes[nodeID2], cost);
                g.addEdgeNoUpdate(e);
            }
        }
        g.computeShortestPaths();
        scanner.close();
        return g;
    }

}