/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm.columnGeneration;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import dpas.Params;
import util.Gurobi;

import java.util.LinkedList;

public abstract class MasterAbs<C extends Column> implements Master<C>{
    protected GRBModel master;
    protected Pricing[] pricingSolvers = null;
    protected LinkedList<C>[] columnSet = null;
    protected boolean display = Params.display;
    protected double HPricingTime = 0;
    protected double EPricingTime = 0;

    @Override
    public double solve() throws GRBException {
        double runtime = 0;
        double runtimeTotal = System.currentTimeMillis();
        do {
            master.update();
            Gurobi.optimize(master);
            if (display) {
                System.out.println("LR:\t" + master.get(GRB.DoubleAttr.ObjVal) +
                        "\truntime per iteration:\t" + (System.currentTimeMillis() - runtime) / 1000);
                runtime = System.currentTimeMillis();
            }
        }while (solvePricing());
        checkSolution();
        System.out.println("Linear relaxation:\t"+master.get(GRB.DoubleAttr.ObjVal) +
                "\truntime:\t"+(System.currentTimeMillis()-runtimeTotal)/1000);
        return master.get(GRB.DoubleAttr.ObjVal);
    }

    @Override
    public boolean solvePricing() throws GRBException {
        updateDual();
        boolean newCol = false;
        for (Pricing pricingSolver : pricingSolvers) {
            if (pricingSolver.solve()) {
                newCol = true;
            }
        }
        return newCol;
    }

    @Override
    public void importColumn(LinkedList<C> columns)throws GRBException{}

    @Override
    public void checkSolution() throws GRBException{}

    @Override
    public boolean isNewColumn(C column) {
        return !columnSet[column.getID()].contains(column);
    }

    @Override
    public void countHPricingTime(double time) {
        HPricingTime += time;
    }

    @Override
    public void countEPricingTime(double time) {
        EPricingTime += time;
    }

    @Override
    public void dispose() throws GRBException {
        master.dispose();
        for (Pricing pricingSolver : pricingSolvers) {
            pricingSolver.dispose();
        }
    }

    @Override
    public boolean printColumn() {
        return Params.printColumn;
    }
}
