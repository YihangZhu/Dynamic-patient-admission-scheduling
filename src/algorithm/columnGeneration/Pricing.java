/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm.columnGeneration;

import gurobi.GRBException;

public interface Pricing <C extends Column>{
    /**
     *
     * @return return true if new columns are found.
     */
    boolean solve()throws GRBException;

    /**
     * calculate reduced cost manually.
     */
    double computeObj(C column)throws GRBException;

    /**
     * dispose pricing model
     */
    void dispose()throws GRBException;
}
