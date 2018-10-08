/*
 * Copyright (c) 3/15/18 11:38 AM
 * Author: Yi-Hang Zhu
 */

package algorithm;

import util.Graph;

public class ShortestPath {

    private static int V;

    private static double[] dist;
    private static int[] pred;

    /**
     *
     * @param graph weight between different vertexes
     * @param src source point
     * @param dest destiny is only need one path.
     */
    public static void dijkstra(int graph[][], int src, int dest) {
        V = graph.length;
        initialize();
        // sptSet[i] will true if vertex i is included in shortest path tree or shortest distance from src to i is finalized
        Boolean sptSet[] = new Boolean[V];
        for (int i = 0; i < V; i++) {
            sptSet[i] = false;
        }

        // Distance of source vertex from itself is 0
        dist[src] = 0;

        // Searching start
        for (int count = 0; count < V-1; count++)
        {
            int u = minDistance(dist, sptSet);
            sptSet[u] = true;
            if (u == dest){
                break;
            }

            // Update dist value of the adjacent vertices of the picked vertex.
            for (int v = 0; v < V; v++) // waste lots of time here.
                if (!sptSet[v] && graph[u][v]!=0 && dist[u]+graph[u][v] < dist[v]){
                    dist[v] = dist[u] + graph[u][v];
                    pred[v] = u;
                }

        }
    }

    public static void BellmanFord(Graph graph, int src) {

        // Initialize distances from src to all other vertices as INFINITE
        V = graph.getV();
        int E = graph.getE();
        initialize();
        dist[src] = 0;

        // Relax all edges |V| - 1 times. A simple shortest path from src to any other vertex can
        // have at-most |V| - 1 edges
        for (int i=1; i<V; ++i) {
            for (int j=0; j<E; ++j) {
                int u = graph.getEdge(j).getSrc();
                int v = graph.getEdge(j).getDest();
                double weight = graph.getEdge(j).getWeight();
                if (dist[u]!=Integer.MAX_VALUE && dist[u]+weight<dist[v]){
                    dist[v]=dist[u]+weight;
                    pred[v]=u;
                }
            }
        }

        // check for negative-weight cycles.
        for (int j=0; j<E; ++j) {
            int u = graph.getEdge(j).getSrc();
            int v = graph.getEdge(j).getDest();
            double weight = graph.getEdge(j).getWeight();
            if (dist[u] != Integer.MAX_VALUE && dist[u]+weight < dist[v])
                System.out.println("Graph contains negative weight cycle");
        }
    }

    private static void initialize(){
        dist = new double[V];
        pred = new int[V];
        for (int i = 0; i < V; i++) {
            dist[i] = Integer.MAX_VALUE;
            pred[i] = -1;
        }
    }

    private static int minDistance(double dist[], Boolean sptSet[]) {
        // Initialize min value
        double min = Integer.MAX_VALUE;
        int min_index=-1;
        for (int v = 0; v < V; v++)
            if (!sptSet[v] && dist[v] <= min) {
                min = dist[v];
                min_index = v;
            }
        return min_index;
    }

    public static double[] getDist() {
        return dist;
    }

    public static int[] getPred() {
        return pred;
    }
}

