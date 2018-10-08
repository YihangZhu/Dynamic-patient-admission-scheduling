/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm.columnGeneration;

import gurobi.GRBEnv;
import gurobi.GRBException;

import java.util.LinkedList;

public interface Master<C extends Column> {

    /**
     * Initialize pricing solvers and columnSet for each pattern.
     */
    void initializePricing(GRBEnv env)throws GRBException;

    /**
     * create model for the master problem.
     */
    void createModel(GRBEnv env) throws GRBException;

    /**
     * solve the master problem
     */
    double solve() throws GRBException;

    /**
     * solving pricing problem iterative
     * @return if new column found reture true, otherwise false.
     */
    boolean solvePricing() throws GRBException;

    /**
     * update dual values for constraints
     */
    void updateDual() throws GRBException;
    /**
     * add columns to the master problem
     */
    boolean addColumn(C column) throws GRBException;

    /**
     * check linear relaxation solution
     */
    void checkSolution() throws GRBException;

    /**
     * check if the same column already in the basis
     * @return true is yes.
     */
    boolean isNewColumn(C column);
    /**
     * dispose all the models
     */
    void dispose() throws GRBException;

    void importColumn(LinkedList<C> columns)throws GRBException;

    /**
     * maximum number of columns add to master each time.
     */
    int getAddColumnNum();

    boolean printColumn();

    /**
     * record runtime for Heuristic pricing solver
     * @param time runtime of the current iteration
     */
    void countHPricingTime(double time);

    /**
     *
     * @param time runtime of the current iteration
     */
    void countEPricingTime(double time);
}
