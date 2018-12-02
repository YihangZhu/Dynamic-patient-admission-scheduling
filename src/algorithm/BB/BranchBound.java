/*
 * Copyright (c) 8/31/18 11:30 AM
 * Author: Yi-Hang Zhu
 */

package algorithm.BB;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBVar;
import dpas.PAS;
import dpas.Params;
import dpas.algoritm.mp.cg.MasterPatient;
import dpas.schedule.Problem;
import dpas.schedule.Solution;
import util.Util;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public class BranchBound {
    private MasterPatient master;
    private double upperBound = Double.MAX_VALUE;
    private double lowerBound = Double.MIN_VALUE;
    private double MIPGap = 100; // percentage
    private double solFeasibleTol = 1e-5;
//    private double stopMIPGap = 1e-6;
    private boolean display = true;
    private ArrayList<GRBVar> branchVars = new ArrayList<>();
    private ArrayList<GRBVar> restVars = new ArrayList<>();
    private PriorityBlockingQueue<Node> queue = new PriorityBlockingQueue<>();
    private PriorityQueue<Node> solPool = new PriorityQueue<>();

    public void createModel(GRBEnv env,PAS pas) throws GRBException {
        Problem problem = pas.getProblem();
        Solution solution = Util.readSolution(problem.getInstance(), Params.importSolutionPath);
        master = new MasterPatient(env, problem, solution);
        upperBound = solution.getObjectiveValue();
        organizeVars();
    }

    public void solve(PAS pas) throws Exception {
        Node node = new Node();
        queue.add(node);
        while (queue.size()>0){
            processNode(queue.take());
        }
        processNode(solPool.poll());
        pas.storeData(master.getSolution());
    }

    private void processNode(Node node){
        try {
            if (node.getObj() < upperBound) {
                node.getModel();
                double LR = master.solve();
                node.recoverModel();
                String str = String.format("Depth: %d ", node.getDepth());

                if (master.getModel().get(GRB.IntAttr.Status) == GRB.INFEASIBLE) {
                    str = "Infeasible\t" + str;
                } else {
                    node.setObj(LR);
                    if (LR >= upperBound) {
                        str = "Pruned\t" + str;
                    } else {
                        Optional<GRBVar> bv = checkVars(branchVars);
                        if (!bv.isPresent()){
                            bv = checkVars(restVars);
                        }
                        if (bv.isPresent()){
                            queue.add(new Node(node,bv.get(),0));
                            queue.add(new Node(node,bv.get(),1));
                        }else {
                            setUpperBound(LR);
                            solPool.add(new Node(node));
                            str = "*\t" + str;
                        }
                    }
                    str = str + String.format("OBJ: %f, ", LR);
                }
                str = str + String.format("UB: %f, LB: %f, Gap: %f", upperBound, lowerBound, MIPGap) + "%";
                if (display) {
                    System.out.println(str);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Optional<GRBVar> checkVars(ArrayList<GRBVar> vars) throws GRBException{
        double value;
        double gap = 1;
        GRBVar bv = null;
        for (GRBVar var : vars) {
            value = var.get(GRB.DoubleAttr.X);
            if (value + solFeasibleTol < 1 && value - solFeasibleTol > 0) {
                if (value % 1 < gap) {
                    gap = value % 1;
                    bv = var;
                }
            }
        }
        return Optional.of(bv);
    }

    public void setUpperBound(double upperBound) {
        this.upperBound = upperBound;
        updateMIPGap();
    }

    public void setLowerBound(double lowerBound) {
        this.lowerBound = lowerBound;
        updateMIPGap();
    }

    public void updateMIPGap(){
        this.MIPGap = 100*(upperBound-lowerBound)/upperBound;
    }

    public void organizeVars(){
        branchVars.addAll(Arrays.stream(master.getZr()).filter(Objects::nonNull)
                .flatMap(Arrays::stream).filter(Objects::nonNull)
                .flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList()));
        restVars.addAll(Arrays.stream(master.getZo()).filter(Objects::nonNull)
                .flatMap(Arrays::stream).filter(Objects::nonNull)
                .collect(Collectors.toList()));
        restVars.addAll(Arrays.stream(master.getZv()).filter(Objects::nonNull)
                .flatMap(Arrays::stream).filter(Objects::nonNull)
                .flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public static void main(String[] args){
        try {
            if (args.length >0){
                Params.instanceName = args[0];
            }
            Params.transfer = true;
            Params.STATIC = true;
            Params.genderPolicyConstr = "4"; //only for validate the solution with MipTr.
            PAS pas = new PAS(null);
            GRBEnv env = new GRBEnv();
            env.set(GRB.IntParam.OutputFlag,0);
            BranchBound branchAndBound = new BranchBound();
            branchAndBound.createModel(env,pas);
            branchAndBound.solve(pas);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
