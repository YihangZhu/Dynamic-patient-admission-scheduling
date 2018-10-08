/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm.columnGeneration;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;
import util.Util;

public class Column implements Comparable<Column> {
    private int ID;
    private GRBVar var;
    private double cost; // gamma, cost for the column
    private double reducedCost;
    private int unusedIterations = 0;

    protected Column(int ID){
        this.ID = ID;
    }

    @SuppressWarnings("unused")
    boolean dispose(GRBModel master,int criteria) throws GRBException {
        if (unusedIterations> criteria){
            master.remove(var);
            return true;
        }else {
            if (var.get(GRB.DoubleAttr.X) < Util.EPS){
                unusedIterations ++;
            }
            return false;
        }
    }

    public void setVar(GRBVar var) {
        this.var = var;
    }

    public int getID() {
        return ID;
    }

    public GRBVar getVar() {
        return var;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getCost() {
        return cost;
    }

    protected double getReducedCost() {
        return reducedCost;
    }

    public void setReducedCost(double reducedCost) {
        this.reducedCost = reducedCost;
    }

    @Override
    public int compareTo(Column o) {
        return Double.compare(reducedCost, o.getReducedCost());
    }
}
