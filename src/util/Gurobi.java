/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import gurobi.*;
import java.io.PrintStream;

/**
 * Created by yihang on 4/14/17.
 * The methods for gurobi
 */
@SuppressWarnings("all")
public class Gurobi {
    public static boolean result = false;

    public static void printModel(GRBModel model, PrintStream ps) throws GRBException {
        result = false;
        if(model.get(GRB.IntAttr.ModelSense) == 1) {
            ps.println("Min:");
        } else {
            ps.println("Max:");
        }
        GRBLinExpr expr = (GRBLinExpr) model.getObjective();
        printExpression(ps, expr);

        ps.println();
        for (GRBConstr constr : model.getConstrs()) {
            printConstraint(model, ps, constr);

        }
        ps.println();
        for (GRBVar var : model.getVars()) {
            printVariable(ps, var);

        }
    }

    public static void printModelResult (GRBModel model, PrintStream ps) throws GRBException{
        System.out.println("The optimal result: "+model.get(GRB.DoubleAttr.ObjVal));
        result = true;
        if(model.get(GRB.IntAttr.ModelSense) == 1) {
            ps.println("Min:");
        } else {
            ps.println("Max:");
        }
        GRBLinExpr expr = (GRBLinExpr) model.getObjective();
        printExpression(ps, expr);

        ps.println();
        for (GRBConstr constr : model.getConstrs()) {
            printConstraint(model, ps, constr);

        }
        ps.println();
        for (GRBVar var : model.getVars()) {
            printVariable(ps, var);

        }
    }

    public static void printColumn(GRBModel model, PrintStream ps, GRBVar var) throws GRBException {
        printVariable(ps,var);
        GRBColumn column = model.getCol(var);
        for (int i = 0; i < column.size();i++){
            printConstraint(model,ps,column.getConstr(i));
        }
    }

    private static void printIIS(GRBModel model, PrintStream ps) throws GRBException {
        model.computeIIS();

        for (GRBConstr constr : model.getConstrs()) {
            if (constr.get(GRB.IntAttr.IISConstr) != 0) {
                printConstraint(model, ps, constr);
            }
        }
        for (GRBVar var : model.getVars()) {
            if (var.get(GRB.IntAttr.IISLB) == 1 || var.get(GRB.IntAttr.IISUB) == 1) {
                printVariable(ps, var);
            }
        }
    }

    public static void printConstraint(GRBModel model, PrintStream ps,
                                       GRBConstr constr) throws GRBException {
        GRBLinExpr row = model.getRow(constr);
        ps.print(constr.get(GRB.StringAttr.ConstrName) + " : ");
        if (result && model.get(GRB.IntAttr.IsMIP)==0) {
            ps.print(" dual value: " + constr.get(GRB.DoubleAttr.Pi) + " | ");
        }
        printExpression(ps, row);
        ps.println(constr.get(GRB.CharAttr.Sense) + "= "
                + constr.get(GRB.DoubleAttr.RHS));
    }

    public static void printExpression(PrintStream ps, GRBLinExpr row)
            throws GRBException {
        for (int i = 0; i < row.size(); i++) {
            GRBVar var = row.getVar(i);

            if (i < row.size() - 1) {
                ps.print(row.getCoeff(i) + "*"
                        + var.get(GRB.StringAttr.VarName) + " "
                        + (row.getCoeff(i + 1) < 0 ? "" : "+ "));
            } else {
                ps.print(row.getCoeff(i) + "*"
                        + var.get(GRB.StringAttr.VarName) + " ");
            }
        }
    }

    public static void printVariable(PrintStream ps, GRBVar var)
            throws GRBException {
        ps.print(var.get(GRB.DoubleAttr.LB) + " <= "
                + var.get(GRB.StringAttr.VarName) + " <= "
                + var.get(GRB.DoubleAttr.UB)+" cof: "+var.get(GRB.DoubleAttr.Obj)
        );
        if (result) {
            ps.println(" current value:" + var.get(GRB.DoubleAttr.X));
        }else {
            ps.println();
        }
    }

//    public static void printGRBVar(GRBVar[][] vars, boolean round) throws GRBException{
//        int i = 0;
//        for (GRBVar[] var : vars) {
//            System.out.print(i++ +":\t");
//            if (var!=null) {
//                printGRBVar(var,round);
//            }else {
//                System.out.println();
//            }
//        }
//    }
//
//    public static void printGRBVar(GRBVar[] var, boolean round)throws GRBException{
//        for (GRBVar aVar : var) {
//            if (aVar != null) {
//                if (round) {
//                    System.out.print(Math.round(aVar.get(GRB.DoubleAttr.Xn)) + "\t");
//                } else {
//                    System.out.print(aVar.get(GRB.DoubleAttr.Xn) + "\t");
//                }
//            }
//        }
//        System.out.println();
//    }

    public static double[] getValue(GRBVar[] vars){
        double [] temp = new double[vars.length];
        try {
            for (int i = 0; i < temp.length; i++) {
                temp[i] = vars[i].get(GRB.DoubleAttr.Xn);
            }
        }catch (GRBException e){
            e.printStackTrace();
        }
        return temp;
    }

    public static double[][] getValue(GRBVar[][] vars){
        double[][] temp = new double[vars.length][];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = getValue(vars[i]);
        }
        return temp;
    }

    public static double[] getValue(GRBLinExpr[] exprs){
        double[] temp = new double[exprs.length];
        try {
            for (int i = 0; i < temp.length; i++) {
                temp[i] = exprs[i].getValue();
            }
        }catch (GRBException e){
            e.printStackTrace();
        }
        return temp;
    }

    public static double[][] getValue(GRBLinExpr[][] exprs){
        double[][] temp = new double[exprs.length][];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = getValue(exprs[i]);
        }
        return temp;
    }

    public static void optimize(GRBModel model) throws GRBException{
        model.optimize();
        if (model.get(GRB.IntAttr.Status) == GRB.INFEASIBLE) {
            printIIS(model, System.out);
            throw new GRBException("The model is infeasible!");
        } else if (model.get(GRB.IntAttr.Status) == GRB.UNBOUNDED){
            throw new GRBException("The model is unbounded");
        }
    }

//    public static void printExpr(GRBLinExpr[] exprs,boolean round)throws GRBException {
//        for (GRBLinExpr expr : exprs) {
//            if (round){
//                System.out.print(Math.round(getValue(expr)) + "\t");
//            }else {
//                System.out.print(getValue(expr) + "\t");
//            }
//        }
//        System.out.println();
//    }

    public static double getValue(GRBLinExpr expr)throws GRBException{
        double value = 0;
        for (int i = 0; i < expr.size(); i++) {
            double val = expr.getVar(i).get(GRB.DoubleAttr.Xn);
            double coeff = expr.getCoeff(i);
            value += val * coeff;
        }
        value += expr.getConstant();

        return value;
    }
    
    public static double calculateObj(GRBModel model, boolean roundVar) throws GRBException{
        GRBLinExpr linExpr = (GRBLinExpr) model.getObjective();
        double obj = linExpr.getConstant();
        for (int i = 0; i < linExpr.size(); i++) {
            double value = linExpr.getVar(i).get(GRB.DoubleAttr.Xn);
            obj += linExpr.getVar(i).get(GRB.DoubleAttr.Obj) * (roundVar?Math.round(value):value);
        }
        return obj;
    }
}
