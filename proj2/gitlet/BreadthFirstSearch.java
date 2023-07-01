package gitlet;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.TreeMap;

public class BreadthFirstSearch {
    public static TreeMap<String, Boolean> marked;
    public static TreeMap<String, String> edgeTo;
    public static TreeMap<String, Integer> distTo;

    public BreadthFirstSearch(Graph g, String s) {
        marked = new TreeMap<>();
        edgeTo = new TreeMap<>();
        distTo = new TreeMap<>();
        for (String vertices: g.allVertices) {
            marked.put(vertices, false);
            edgeTo.put(vertices, null);
            distTo.put(vertices, 0);
        }
        bfs(g, s);
    }

//    public void bfs(Graph g, int s) {
//        Queue<Integer> queue = new ArrayDeque<>();
//        queue.add(s);
//        marked[s] = true;
//        while (!queue.isEmpty()) {
//            int v = queue.remove();
//            for (int n: g.adj(v)) {
//                if (!marked[n]) {
//                    marked[n] = true;
//                    edgeTo[n] = v;
//                    distTo[n] = distTo[v] + 1;
//                    queue.add(n);
//                }
//            }
//        }
//    }

    public void bfs(Graph g, String s) {
        Queue<String> queue = new ArrayDeque<>();
        queue.add(s);
        marked.put(s, true);
        while(!queue.isEmpty()) {
            String v = queue.remove();
            for (String n: g.adj(v)) {
                if (!marked.get(n)) {
                    marked.put(n, true);
                    edgeTo.put(n, v);
                    distTo.put(n, distTo.get(v) + 1);
                    queue.add(n);
                }
            }
        }
    }

    public TreeMap<String, Integer> getDistTo() {
        return distTo;
    }
}
