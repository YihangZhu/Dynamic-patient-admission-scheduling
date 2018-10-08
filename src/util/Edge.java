/*
 * Copyright (c) 3/16/18 10:13 AM
 * Author: Yi-Hang Zhu
 */

package util;

public class Edge {

    Edge(){

    }
    Edge(Edge edge){
        src = edge.getSrc();
        dest = edge.getDest();
    }
    private int src, dest;
    private double weight;

    public void setSrcDestWeight(int src,int dest, int weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getSrc() {
        return src;
    }

    public int getDest() {
        return dest;
    }
}