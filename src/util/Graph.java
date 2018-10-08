/*
 * Copyright (c) 3/16/18 9:21 AM
 * Author: Yi-Hang Zhu
 */

package util;

public class Graph {
    private int V, E;
    private Edge edge[];

    /**
     * Copy graph
     */
    public Graph(Graph graph){
        V = graph.getV();
        E = graph.getE();
        edge = new Edge[E];
        for (int i = 0; i < E; i++) {
            edge[i] = new Edge(graph.getEdge(i));
        }
    }

    /**
     * Create graph in terms of edges.
     * @param vertexNum number of vertexes
     * @param edgeNum number of edges.
     */
    public Graph(int vertexNum, int edgeNum) {
        V = vertexNum;
        E = edgeNum;
        edge = new Edge[E];
        for (int i=0; i<E; ++i)
            edge[i] = new Edge();
    }

    public Edge getEdge(int id) {
        return edge[id];
    }

    public int getV() {
        return V;
    }

    public int getE() {
        return E;
    }
}
