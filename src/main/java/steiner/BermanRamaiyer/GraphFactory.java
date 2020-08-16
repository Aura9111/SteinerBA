package steiner.BermanRamaiyer;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;

public class GraphFactory {

    public static HashSet<Graph> allGraphs() throws IOException {

        HashSet<Graph> out = new HashSet<>();

        File folder = new File("D:\\I080\\");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                String path = file.getName();
                if (path.charAt(path.length() - 6) == '1' || path.charAt(path.length() - 6) == '2'
                        || path.charAt(path.length() - 6) == '4') {
                    Graph g = makeMyGraphMethods(file);
                    out.add(g);
                }
            }
        }
        return out;
    }

    public static Graph makeMyGraphMethods(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        // create Nodes
        int nodes = 0;
        Graph g = new Graph(file.getName().substring(file.getName().indexOf("-") + 1, file.getName().indexOf(".")));
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
                    g.addNode("" + i, true);
                }
                for (int i = x + 1; i <= nodes; i++) {
                    g.addNode("" + i, false);
                }
            }
        }
        scanner.close();
        // create Edges
        scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("E ")) {
                String[] split = line.split(" ");
                double cost = Double.parseDouble(split[3]);
                g.addEdge(split[1], split[2], cost);
            }
        }
        scanner.close();
        return g;
    }
}