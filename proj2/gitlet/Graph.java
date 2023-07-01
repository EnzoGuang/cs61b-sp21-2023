package gitlet;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public class Graph {
    private final int V;
    private int E;
    public TreeSet<String> allVertices = new TreeSet<>();
    public TreeMap<String, ArrayList<String>> adj;

    public Graph(int V) {
        this.V = V;
        this.E = 0;
        this.adj = new TreeMap<>();
    }

    /**@param v is an ArrayList of all vertices to initialize the Graph. */
    public Graph(ArrayList<String> v) {
        this(v.size());
        for (int i = 0; i < this.numOfVertices(); i++) {
            this.adj.put(v.get(i), new ArrayList<>());
        }
    }

    /* Adds the undirected edge v-w to this graph. */
    public void addEdge(String v, String w) {
        this.E++;
        allVertices.add(v);
        allVertices.add(w);
        ArrayList<String> adjOfV = this.adj.get(v);
        adjOfV.add(w);
        ArrayList<String> adjOfW = this.adj.get(w);
        adjOfW.add(v);
    }

    /** Return the vertices adjacent to vertex v. */
    public Iterable<String> adj(String v) {
        return this.adj.get(v);
    }

    public void printGraph() {
        for (String temp: getAllVertices()) {
            System.out.println(temp + " - " + adj(temp));
        }
    }

    public Iterable<String> getAllVertices() {
        ArrayList<String> vertices = new ArrayList<>();
        for (String temp: this.allVertices) {
            vertices.add(temp);
        }
        return vertices;
    }

    /* number of vertices. */
    public int numOfVertices() {
        return this.V;
    }

    /* number of edges. */
    public int numOfEdges() {
        return this.E;
    }
}
