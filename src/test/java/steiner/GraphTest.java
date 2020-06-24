package steiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;

public class GraphTest {

    public static void main(String[] args) throws Exception {
        for (Graph g: GraphFactory.allGraphs()) {
            Tree t=App.bermanRamaiyer(g, g.getAllTerminalNodes(), g.getAllTerminalNodes().size());
            t.printGraph(g.path);
        }
    }

    public static void makeMyGraphMethods(String name) throws IOException {
        String path="D:\\I080\\i080-"+name;
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path + ".txt")));
        Scanner scanner = new Scanner(new File(path + ".stp"));
        out.write("public static Graph g"+name+"(){");
        out.newLine();
        out.write("Graph g = new Graph(\"" + path.substring(path.length() - 3) + "\");");
        out.newLine();
        // create Nodes
        int nodes = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.startsWith("Nodes")) {
                String[] split = line.split(" ");
                nodes = Integer.parseInt(split[split.length - 1]);
            }
            if (line.matches("SECTION Terminals")) {
                line = scanner.nextLine();
                String[] split = line.split(" ");
                int x = Integer.parseInt(split[split.length - 1]);
                for (int i = 1; i <= x; i++) {
                    out.write("g.addNode(\"\"+" + i + ", true);");
                    out.newLine();
                }
                for (int i = x + 1; i <= nodes; i++) {
                    out.write("g.addNode(\"\"+" + i + ", false);");
                    out.newLine();
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
                out.write("g.addEdge(\"\"+" + split[1] + ", \"\"+" + split[2] + ", " + split[3] + ");");
                out.newLine();
            }
        }
        scanner.close();
        out.write("return g;");
        out.newLine();
        out.write("}");
        out.close();
    }

}