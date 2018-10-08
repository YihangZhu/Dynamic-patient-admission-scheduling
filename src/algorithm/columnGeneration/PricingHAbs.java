/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm.columnGeneration;

import gurobi.GRBException;
import util.Util;

import java.util.LinkedList;

public abstract class PricingHAbs <M extends Master<C>, C extends Column, E extends PricingEAbs<M,C> >
        implements Pricing<C> {
    protected E es = null;
    protected C bestColumn = null;
    protected int maxIteration;
    private LinkedList<C> columns = new LinkedList<>();

    /**
     * A very basic heuristic procedure. rewritten if develop more complicated heuristic
     */
    public boolean solve() throws GRBException{
        columns.clear();
        boolean colFound = false;
        bestColumn = initSolution();
        double obj = computeObj(bestColumn);
        storeColumns(obj);
        int iteration = 0;
        while (iteration < maxIteration) {
            C columnNew = searchColumn();
            if (columnNew == null){
                break;
            }
            double objTemp = computeObj(columnNew);
            if (objTemp <= obj){
                obj = objTemp;
                bestColumn = columnNew;
                storeColumns(obj);
            }
            iteration++;
        }
        int temp = Math.min(es.getMaster().getAddColumnNum(),columns.size());
        for (int i = 0; i < temp; i++) {
            C column = columns.pop();
            if (column.getReducedCost() <- Util.EPS){
                if (es.getMaster().addColumn(column)) {
                    if (es.master.printColumn()) {
                        System.out.println(column);
                    }
                    colFound = true;
                }
            }else {
                break;
            }
        }
        return colFound;
    }
    private void storeColumns(double obj){
        bestColumn.setReducedCost(obj);
        if (obj < -Util.EPS ){
            columns.push(bestColumn);
        }
    }
    /**
     * search better columns based on the current best column
     */
    public abstract C searchColumn();

    public abstract C initSolution() throws GRBException;

    public double computeObj(C column) throws GRBException {
        return es.computeObj(column);
    }

    public void dispose() {
        es.dispose();
    }
}
