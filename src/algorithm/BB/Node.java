/*
 * Copyright (c) 8/31/18 11:30 AM
 * Author: Yi-Hang Zhu
 */

package algorithm.BB;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBVar;

import java.util.ArrayList;
import java.util.Optional;

public class Node implements Comparable<Node> {
    private ArrayList<GRBVar> vars;
    private ArrayList<Double> values;
    private double bound = 0;
    private double obj = 0;
    private int depth; // 0, 1, 2 ,3

    public Node(Node node, GRBVar var, double value){
        vars = new ArrayList<>(node.getVars().size()+1);
        vars.addAll(node.getVars());
        vars.add(var);
        values = new ArrayList<>(vars.size());
        values.addAll(node.getValues());
        values.add(value);
        this.depth = node.getDepth()+1;
        this.bound = node.getObj();
    }

    public Node(){
        depth = 1;
        vars = new ArrayList<>();
        values = new ArrayList<>();
    }

    public Node(Node node){
        vars = new ArrayList<>(node.getVars());
        values = new ArrayList<>(node.getValues());
        depth = node.getDepth();
        obj = node.getObj();
    }

    public void getModel() throws GRBException {
        for (int i = 0; i < vars.size(); i++) {
            vars.get(i).set(GRB.DoubleAttr.LB,values.get(i));
            vars.get(i).set(GRB.DoubleAttr.UB,values.get(i));
        }
    }

    public void recoverModel()throws GRBException{
        for (GRBVar var : vars) {
            var.set(GRB.DoubleAttr.LB, 0);
            var.set(GRB.DoubleAttr.UB, 1);
        }
    }

    public ArrayList<GRBVar> getVars() {
        return vars;
    }

    public void setVars(ArrayList<GRBVar> vars) {
        this.vars = vars;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public void setValues(ArrayList<Double> values) {
        this.values = values;
    }

    public double getBound() {
        return bound;
    }

    public void setBound(double bound) {
        this.bound = bound;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setObj(double obj) {
        this.obj = obj;
    }

    public double getObj() {
        return obj;
    }

    @Override
    public int compareTo(Node o) {
        if (bound > o.getBound()){
            return -1;
        }
        return 1;
    }

}
