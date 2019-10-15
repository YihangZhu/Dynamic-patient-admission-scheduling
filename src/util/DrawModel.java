/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import gurobi.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by yihang on 4/25/17.
 * draw the graph for model or other information
 *
 DrawModel drawModel = new DrawModel(model);
 drawModel.drawOrigModel();
 int t = 0;
 for (;t < y.length; t++) {
 drawModel.pricingVars.put(t,new ArrayList<>());
 for (GRBVar[] vars: x[t]) {
 drawModel.sortVar(vars,t);
 }
 drawModel.sortVar(y[t],t);
 drawModel.sortConstrs(t);
 }
 drawModel.printVisualization(t, y.length);
 */

@SuppressWarnings("all")
public final class DrawModel {
    private GRBModel model;
    private int varNum, constrNum;
    private ArrayList<GRBVar> totVars;
    private HashMap<GRBConstr,Integer> constrMap = new HashMap<>();//map each constraint to a unique number
    private HashMap<GRBVar,Integer> varsMap = new HashMap<>(); //map each variable to a unique number
    public HashMap<Integer,ArrayList<GRBVar>> pricingVars = new HashMap<>();//map variables to the index of each pattern
    private HashMap<Integer,ArrayList<GRBConstr>> pricingConstrs = new HashMap<>();//map constraints to the index of variables
    private HashMap<Integer,ArrayList<GRBConstr>> pricingCoupleConstrs = new HashMap<>(); // map constraints to the index of varibles

    /**
     * Dantzig wolfe decomposition graph.
     * x axis is pattern, Y axix is constraints.
     * e.g. if constraint a appear in both pattern 1 and pattern 2, there would be a horizonal line from point (1,a) to (2,a)
     * before make this graph, we should know the relations between variables to different patterns
     */

    public DrawModel(GRBModel model) {
        this.model = model;
        totVars = new ArrayList<>(Arrays.asList(model.getVars()));
        constrNum = 0; varNum = 0;
    }

    /**
     * give the graph for the original model
     */
    public void drawOrigModel() throws GRBException {
        HashMap<GRBConstr,Integer> constrMap = new HashMap<>();
        GRBVar[] vars = model.getVars();
        int constrNum = 0;
        ArrayList<int[]> graph = new ArrayList<>();

        for (int i = 0; i < vars.length; i++) {
            GRBVar var = vars[i];
            GRBColumn col = model.getCol(var);
            for (int c = 0; c < col.size(); c++) {
                GRBConstr constr = col.getConstr(c);
                if (!constrMap.containsKey(constr)) {
                    constrMap.put(constr, constrNum++);
                }
                graph.add(new int[]{i,constrMap.get(constr)});
            }
        }
        for (int[] variable : graph) {
            System.out.println(String.format("%d,%d",variable[0],variable[1]));
        }
        System.out.println("split");
    }


    public int sortVar(GRBVar[] vars, int i){
        if (vars!=null) {
            for (GRBVar var : vars) {
                varNum = sortVar(var, i);
            }
        }
        return varNum;
    }

    public int sortVar(GRBVar var, int i){
        if (var!=null) {
            totVars.remove(var);
            pricingVars.get(i).add(var);
            if (!varsMap.containsKey(var))
                varsMap.put(var,varNum++);
        }
        return varNum;
    }

    /**
     *
     * @param pattern p
     * get variables associated with pattern p
     * assign constraints to the list of each varibles the constraint contains.
     */
    public int sortConstrs(int p) throws GRBException{
        for (int i = 0; i < pricingVars.get(p).size(); i++) {
            GRBVar var = pricingVars.get(p).get(i);
            pricingConstrs.put(varsMap.get(var),new ArrayList<>());
            pricingCoupleConstrs.put(varsMap.get(var),new ArrayList<>());
            GRBColumn col = model.getCol(var); // get the constraints associated with the variable
            for (int j = 0; j < col.size(); j++) {
                GRBConstr constr  = col.getConstr(j);
                GRBLinExpr expr = model.getRow(constr); //get the constraint
                boolean couplingConstr = false; //true if the constraint is associated with different pattern otherwise false
                for (int k = 0; k < expr.size(); k++) {
                    GRBVar var1 = expr.getVar(k);
                    if (!pricingVars.get(p).contains(var1)){
                        couplingConstr = true;
                        break;
                    }
                }
                if (couplingConstr){
                    pricingCoupleConstrs.get(varsMap.get(var)).add(constr);
                }else {
                    pricingConstrs.get(varsMap.get(var)).add(constr);
                    if (!constrMap.containsKey(constr))
                        constrMap.put(constr,constrNum++);
                }
            }
        }
        return constrNum;
    }

    private void sortCoupledConstrs(){
        for (ArrayList<GRBConstr> grbConstrs : pricingCoupleConstrs.values()) {
            for (GRBConstr grbConstr : grbConstrs) {
                if (!constrMap.containsKey(grbConstr)){
                    constrMap.put(grbConstr,constrNum++);
                }
            }
        }
    }
    //assign all the rest variables to the last block
    //and sort the associated constraints
    private void sortRestVarsConstrs(int p) throws GRBException{
        pricingVars.put(p,new ArrayList<>());
        for (GRBVar var : totVars) {
            if (var!=null) {
                varsMap.put(var,varNum++);
                pricingVars.get(p).add(var);
                pricingConstrs.put(varsMap.get(var),new ArrayList<>());
                GRBColumn col = model.getCol(var);
                for (int c = 0; c < col.size(); c++) {
                    GRBConstr constr = col.getConstr(c);
                    pricingConstrs.get(varsMap.get(var)).add(constr);
                    if (!constrMap.containsKey(constr))
                        constrMap.put(constr,constrNum++);
                }
            }
        }
    }

    /**
     * @param blockNum number of patterns
     * visualize the whole model
     */
    public void printVisualization(int i,int blockNum)throws GRBException{
        sortCoupledConstrs();
        sortRestVarsConstrs(i);
        for (int p = 0; p<blockNum+1;p++) {
//            System.out.println(String.format("Block %d",integer+1));
            for (GRBVar var : pricingVars.get(p)) {
                for (GRBConstr constr : pricingConstrs.get(varsMap.get(var))) {
                    System.out.println(String.format("%d,%d",varsMap.get(var),constrMap.get(constr)));
//                    System.out.println(constr.get(GRB.StringAttr.ConstrName));
                }
            }
            if (p<blockNum) {
                System.out.println("k");
            }
        }
        for (int p = 0; p < blockNum; p++) {
            for (GRBVar var : pricingVars.get(p)) {
                for (GRBConstr constr : pricingCoupleConstrs.get(varsMap.get(var))) {
//                    System.out.println(constr.get(GRB.StringAttr.ConstrName));
                    System.out.println(String.format("%d,%d",varsMap.get(var),constrMap.get(constr)));
                }
            }
        }
        System.out.println("k");
        System.out.println("split");
    }
}
