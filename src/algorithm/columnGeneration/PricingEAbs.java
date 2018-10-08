/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm.columnGeneration;

import gurobi.*;
import util.Gurobi;
import util.Util;

public abstract class PricingEAbs<M extends Master<C>, C extends Column> implements Pricing<C> {
    protected int ID;
    protected M master;
    protected GRBModel slave = null;
    protected GRBLinExpr partialObj;
    protected GRBLinExpr obj;
    protected C newColumn = null;

    public PricingEAbs(int ID, M master) {
        this.ID = ID;
        this.master = master;
        partialObj = new GRBLinExpr();
        obj = new GRBLinExpr();
    }

    @Override
    public boolean solve() throws GRBException {
        boolean columnFound = false;
        updateObj();
        Gurobi.optimize(slave);
        int solNum = slave.get(GRB.IntAttr.SolCount);
        for (int s = 0; s < solNum; s++) {
            slave.set(GRB.IntParam.SolutionNumber,s);
            double objVal =  getColumn();
            newColumn.setReducedCost(objVal);
            if (s == 0) {
                if (Util.isUnequal(objVal, Gurobi.calculateObj(slave,true),true)) {
                    System.out.println(slave.get(GRB.DoubleAttr.ObjCon));
                    Gurobi.printModelResult(slave, System.out);
                    System.exit(-1);
                }
            }
            if ( objVal < -Util.EPS ) {
                if (master.addColumn(newColumn)) {
                    if (master.printColumn()) {
                        System.out.println(newColumn.toString());
                    }
                    columnFound = true;
                }
            }
        }
        return columnFound;
    }

    @Override
    public void dispose() {
        slave.dispose();
    }

    public int getID() {
        return ID;
    }

    public M getMaster() {
        return master;
    }

    public abstract void createModel(GRBEnv env) throws GRBException;

    public abstract void updateObj() throws GRBException;

    /**
     * @return reduce cost of the column.
     */
    public abstract double getColumn() throws GRBException ;

}
