/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import algorithm.columnGeneration.Column;
import algorithm.columnGeneration.Master;
import algorithm.columnGeneration.MasterAbs;
import gurobi.*;
import dpas.PAS;
import dpas.Params;
import dpas.algoritm.mp.MipTr;
import dpas.instance.Instance;
import dpas.schedule.Problem;
import dpas.schedule.SPatient;
import dpas.schedule.ScheduleResult;
import dpas.schedule.Solution;
import util.Util;

import java.util.Arrays;
import java.util.LinkedList;

public class MasterPatient extends MasterAbs<ColumnPatient> {
    private Problem problem;
    private int patientNum;
    private Constraints constraints;
    private GRBLinExpr idleRoom = new GRBLinExpr();
    private GRBLinExpr idleOR = new GRBLinExpr();
    private GRBLinExpr rgCost = new GRBLinExpr();
    private GRBLinExpr riCost = new GRBLinExpr();
    private GRBLinExpr oorCost = new GRBLinExpr();
    private MipTr checkMIP;
    private GRBVar[][][] zr;
    private GRBVar[][][] zv;
    private GRBVar[][] zo;
    public MasterPatient(GRBEnv env, Problem problem, Solution solution) throws GRBException {
        this.problem = problem;
        patientNum = problem.getPatientSet().size();
        // create pricing solvers
        initializePricing(env);
        createModel(env);
//        ColumnXML.readXMLFile("columns.xml",problem.getPatientSet(),this,true);
        importColumn(solution.getScheduleResult());
        checkMIP = new MipTr(env, problem, true);
    }

    public MasterPatient(GRBEnv env, Problem problem) throws GRBException {
        this.problem = problem;
        patientNum = problem.getPatientSet().size();
        initializePricing(env);
        createModel(env);
    }

    private void importColumn(ScheduleResult solution) throws GRBException {
        for (int p = 0; p < problem.getPatientSet().size(); p++) {
            SPatient patient = solution.getSPatients(problem.getPatientSet().get(p).getNumber());
            int[] rooms = patient.getRooms().stream().mapToInt(i -> i).toArray();
            int delay = patient.getAdmissionDelay();
            ColumnPatient column = new ColumnPatient(p, delay, rooms);
            pricingSolvers[p].computeObj(column);
            addColumn(column);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializePricing(GRBEnv env) throws GRBException {
        pricingSolvers = new PricingHSolver[patientNum];
        columnSet = new LinkedList[patientNum];
        for (int p = 0; p < pricingSolvers.length; p++) {
            pricingSolvers[p] = new PricingHSolver(this, env, problem, p);
            columnSet[p] = new LinkedList<>();
        }
    }

    @Override
    @SuppressWarnings("all")
    public void createModel(GRBEnv env) throws GRBException {
        master = new GRBModel(env);
//        master.set(GRB.IntParam.Presolve,2);
        patientNum = problem.getPatientSet().size();
        int currentDay = problem.getCurrent();
        Instance instance = problem.getInstance();

        idleRoom.addConstant(-Params.IDLE_ROOM_CAPACITY * instance.redundantRoomCap());
        idleOR.addConstant(-Params.IDLE_OPERATING_ROOM * instance.redundantORCap());

        //add artificial variable will slightly increase computational time, but when
        //number of patients is large is will speed up convergence.
        GRBVar[] artificial = new GRBVar[patientNum];
        for (int p = 0; p < artificial.length; p++) {
            artificial[p] = master.addVar(0, GRB.INFINITY, 10000, GRB.CONTINUOUS, String.format
                    ("varArtificial(%d)", p));
        }
        GRBLinExpr cache = new GRBLinExpr();
        constraints = new Constraints();
        boolean newFormulation = false;
        constraints.setNewFormulation(newFormulation);
        constraints.initConstrs(patientNum, instance.getRoomNum(),
                instance.getNumDays(), instance.getSpecNum(),
                instance.getPlanningHorizon());

        for (int p = 0; p < patientNum; p++) {
            cache.clear();
//            cache.addTerm(1,artificial[p]);
            constraints.getConstrsConvexity()[p] = master.addConstr(cache, GRB.EQUAL, 1,
                    String.format("constrConvexity(%d)", p));
        }
        zr = new GRBVar[patientNum][instance.getRoomNum()][];
        zv = new GRBVar[patientNum][][];
        zo = new GRBVar[patientNum][];
        if (newFormulation) {
            for (int p = 0; p < patientNum; p++) {
                for (int r = 0; r < instance.getRoomNum(); r++) {
                    if (problem.getPatientSet().get(p).roomAvailable(r)) {
                        zr[p][r] = new GRBVar[instance.getNumDays()];
                        for (int d = problem.getPatientSet().get(p).getEarliestAD();
                             d < problem.getPatientSet().get(p).getMaxDD(); d++) {
                            zr[p][r][d] = master.addVar(0,GRB.INFINITY , 0, GRB.CONTINUOUS,
                                    String.format("zr(%d,%d,%d)", p, r, d));
                            constraints.getConstrAux1()[p][r][d] =
                                    master.addConstr(0, GRB.EQUAL, zr[p][r][d],
                                            String.format("constrZR(%d,%d,%d)", p, r, d));
                        }
                    }
                }
            }

//            for (int r = 0; r < instance.getRoomNum(); r++) {
//                for (int d = 0; d < instance.getNumDays(); d++) {
//                    zt[r][d] = master.addVar(0,GRB.INFINITY,0,GRB.CONTINUOUS,
//                            String.format("zt(%d,%d)",r,d));
//                    constraints.getConstrAux4()[r][d] = master.addConstr(0,GRB.EQUAL,zt[r][d],
//                            String.format("constrZT(%d,%d)",r,d));
//                }
//            }
            for (int p = 0; p < patientNum; p++) {
                if (problem.getPatientSet().get(p).getVariablity() > 0) {
                    zv[p] = new GRBVar[instance.getRoomNum()][];
                    for (int r = 0; r < instance.getRoomNum(); r++) {
                        if (problem.getPatientSet().get(p).roomAvailable(r)) {
                            zv[p][r] = new GRBVar[instance.getNumDays()];
                            int temp = Math.min(instance.getNumDays(),problem.getPatientSet().get(p).getMaxDD()+1);
                            for (int d = problem.getPatientSet().get(p).getEarliestDD(); d < temp; d++) {
                                zv[p][r][d] = master.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS,
                                        String.format("zv(%d,%d,%d)", p, r, d));
                                constraints.getConstrAux2()[p][r][d] =
                                        master.addConstr(0, GRB.EQUAL, zv[p][r][d],
                                                String.format("constrZV(%d,%d,%d)", p, r, d));
                            }
                        }
                    }
                }
            }

            for (int p = 0; p < patientNum; p++) {
                if (problem.getPatientSet().get(p).needSurgery()) {
                    zo[p] = new GRBVar[instance.getNumDays()];
                    for (int d = 0; d < problem.getPatientSet().get(p).getMaxDelay()+1; d++) {
                        int sd = d + problem.getPatientSet().get(p).getEarliestSD();
                        if (sd < instance.getNumDays()) {
                            zo[p][sd] = master.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS,
                                    String.format("zo(%d,%d)", p, sd));
                            constraints.getConstrAux3()[p][sd] = master.addConstr(0, GRB.EQUAL, zo[p][sd],
                                    String.format("constrZO(%d,%d)", p, sd));
                        }
                    }
                }
            }
        }

        GRBVar[][] ro = new GRBVar[instance.getRoomNum()][instance.getNumDays()];
        GRBVar[][] gc = new GRBVar[instance.getRoomNum()][instance.getNumDays()];
        GRBVar[][] male = new GRBVar[instance.getRoomNum()][instance.getNumDays()];
        GRBVar[][] female = new GRBVar[instance.getRoomNum()][instance.getNumDays()];
        GRBVar[][] ir = new GRBVar[instance.getRoomNum()][instance.getPlanningHorizon()];
        for (int r = 0; r < instance.getRoomNum(); r++) {
            int capacity = instance.getRooms()[r].getCapacity();
            //********************************** room capacity constraints******************************
            for (int d = currentDay; d < instance.getNumDays(); d++) {
                cache.clear();
                if (newFormulation) {
                    for (int p = 0; p < patientNum; p++) {
                        if (zr[p][r] != null && zr[p][r][d]!=null) {
                            cache.addTerm(1, zr[p][r][d]);
                        }
                    }
//                    cache.addTerm(1,zt[r][d]);
                }
                constraints.getConstrsRC()[r][d] = master.addConstr(cache, GRB.LESS_EQUAL,
                        capacity, String.format("constrRC(%d,%d)", r, d));
            }
            //************************room idle capacity***************************
            for (int d = currentDay; d < instance.getPlanningHorizon(); d++) {
                ir[r][d] = master.addVar(0, GRB.INFINITY, Params.IDLE_ROOM_CAPACITY, GRB
                        .CONTINUOUS, String.format("varRI(%d,%d)", r, d));
                idleRoom.addTerm(Params.IDLE_ROOM_CAPACITY, ir[r][d]);
                cache.clear();
                cache.addTerm(-1, ir[r][d]);
                if (newFormulation) {
                    for (int p = 0; p < patientNum; p++) {
                        if (zr[p][r] != null && zr[p][r][d] != null) {
                            cache.addTerm(-1, zr[p][r][d]);
                        }
                    }
//                    cache.addTerm(-1,zt[r][d]);
                }
                constraints.getConstrsRI()[r][d] = master.addConstr(cache, GRB.EQUAL, -capacity,
                        String.format("constrRI(%d,%d)", r, d));
            }
            //*************************room over crowd constraints******************************
            for (int d = currentDay; d < instance.getNumDays(); d++) {
                ro[r][d] = master.addVar(0, GRB.INFINITY, Params.OVERCROWD_RISK, GRB.CONTINUOUS,
                        String.format("varRO(%d,%d)", r, d));
                riCost.addTerm(Params.OVERCROWD_RISK, ro[r][d]);
                cache.clear();
                cache.addTerm(-1, ro[r][d]);
                if (newFormulation) {
                    for (int p = 0; p < patientNum; p++) {
                        if (zr[p][r] != null && zr[p][r][d]!=null) {
                            cache.addTerm(1, zr[p][r][d]);
                        }
                        if (zv[p] != null && zv[p][r] != null && zv[p][r][d]!=null) {
                            cache.addTerm(1, zv[p][r][d]);
                        }
                    }
//                    cache.addTerm(1,zt[r][d]);
                }
                constraints.getConstrsRO()[r][d] = master.addConstr(cache, GRB.LESS_EQUAL,
                        capacity, String.format("constrRO(%d,%d)", r, d));
            }
            //****************************room gender policy constraints****************************
            if (instance.roomForSG(r)) {
                constraints.getConstrsRGMale()[r] =
                        new GRBConstr[instance.getNumDays()][patientNum];
                constraints.getConstrsRGFemale()[r] = new GRBConstr[instance.getNumDays()
                        ][patientNum];
                for (int d = currentDay; d < instance.getNumDays(); d++) {
                    gc[r][d] = master.addVar(0, 1, Params.GENDER_POLICY, GRB.CONTINUOUS,
                            String.format("varG(%d,%d)", r, d));
                    rgCost.addTerm(Params.GENDER_POLICY, gc[r][d]);
                    male[r][d] = master.addVar(0, 1, 0, GRB.CONTINUOUS, String
                            .format("varM(%d," + "%d)", r, d));
                    female[r][d] = master.addVar(0, 1, 0, GRB.CONTINUOUS, String
                            .format("varF(%d," +
                            "%d)", r, d));
                    for (int p = 0; p < patientNum; p++) {
                        if (problem.getPatientSet().get(p).roomAvailable(r)) {
                            cache.clear();
                            if (newFormulation) {
                                if (zr[p][r][d]!=null) {
                                    cache.addTerm(1, zr[p][r][d]);
                                }
                            }
                            if (problem.getPatientSet().get(p).isMale()) {
                                constraints.getConstrsRGMale()[r][d][p] =
                                        master.addConstr(cache, GRB.LESS_EQUAL, male[r][d],
                                                String.format("constrRGM(%d,%d,%d)", r, d, p));
                            } else {
                                constraints.getConstrsRGFemale()[r][d][p] =
                                        master.addConstr(cache, GRB.LESS_EQUAL, female[r][d],
                                                String.format("constrRGF(%d,%d,%d)", r, d, p));
                            }
                        }
                    }
                    cache.clear();
                    cache.addTerm(1, male[r][d]);
                    cache.addTerm(1, female[r][d]);
                    cache.addConstant(-1);
                    master.addConstr(cache, GRB.LESS_EQUAL, gc[r][d],
                            String.format("constrRG(%d,%d)", r, d));
                }
            }
        }

        GRBVar[][] orso = new GRBVar[instance.getNumDays()][instance.getSpecNum()];
        GRBVar[] ordo = new GRBVar[instance.getNumDays()];
        GRBVar[][] ori = new GRBVar[instance.getPlanningHorizon()][instance.getSpecNum()];

        for (int d = currentDay; d < instance.getPlanningHorizon(); d++) {
            for (int s = 0; s < instance.getSpecNum(); s++) {
                ori[d][s] = master.addVar(0, GRB.INFINITY, Params.IDLE_OPERATING_ROOM, GRB
                                .CONTINUOUS,
                        String.format("varORI(%d,%d)", d, s));
                idleOR.addTerm(Params.IDLE_OPERATING_ROOM, ori[d][s]);
                cache.clear();
                cache.addTerm(-1, ori[d][s]);
                if (newFormulation) {
                    for (int p = 0; p < patientNum; p++) {
                        if (problem.getPatientSet().get(p).getSpec() == s
                                && zo[p] != null && zo[p][d]!=null
                                && problem.getPatientSet().get(p).isElective()) {
                            cache.addTerm(-problem.getPatientSet().get(p).getSurDur(), zo[p][d]);
                        }
                    }
                }
                constraints.getConstrsORI()[d][s] =
                        master.addConstr(cache, GRB.LESS_EQUAL, -instance.getORSpecCapacity(d, s),
                                String.format("constrORI(%d,%d)", d, s));
            }
        }

        for (int d = currentDay; d < instance.getNumDays(); d++) {
            //*******************************operating room specialism constraints***************
            for (int s = 0; s < instance.getSpecNum(); s++) {
                cache.clear();
                orso[d][s] =
                        master.addVar(0, instance.getSpecAOT(d, s), Params.OVERTIME, GRB.CONTINUOUS,
                                String.format("varORSO(%d,%d)", d, s));
                oorCost.addTerm(Params.OVERTIME, orso[d][s]);
                cache.addTerm(-1, orso[d][s]);
                if (newFormulation) {
                    for (int p = 0; p < patientNum; p++) {
                        if (problem.getPatientSet().get(p).getSpec() == s
                                && zo[p] != null && zo[p][d]!=null
                                && problem.getPatientSet().get(p).isElective()) {
                            cache.addTerm(problem.getPatientSet().get(p).getSurDur(), zo[p][d]);
                        }
                    }
                }
                constraints.getConstrsORSO()[d][s] =
                        master.addConstr(cache, GRB.LESS_EQUAL, instance.getORSpecCapacity(d, s),
                                String.format("constrORSO(%d,%d)", d, s));
            }
            //*********************************** operating room day constraints**********************
            cache.clear();
            ordo[d] = master.addVar(0, Params.ADMITTED_TOTAL_OVERTIME, Params.OVERTIME, GRB
                            .CONTINUOUS,
                    String.format("varORDO(%d)", d));
            oorCost.addTerm(Params.OVERTIME, ordo[d]);
            cache.addTerm(-1, ordo[d]);
            if (newFormulation) {
                for (int p = 0; p < patientNum; p++) {
                    if (zo[p] != null && zo[p][d]!=null) {
                        cache.addTerm(problem.getPatientSet().get(p).getSurDur(), zo[p][d]);
                    }
                }
            }
            constraints.getConstrsORDO()[d] =
                    master.addConstr(cache, GRB.LESS_EQUAL, instance.getORDayCapacity(d),
                            String.format("constrORDO(%d)", d));
        }
        master.update();
        constraints.initDuals();
    }

    @Override
    public double solve() throws GRBException {
        try {
            double runTime = Params.stateTime;
            double obj;
            do {
                master.update();
//                Gurobi.optimize(master);
                master.optimize();
                if (master.get(GRB.IntAttr.Status) == GRB.INFEASIBLE){
                    return 3.1415926;
                }
                obj = master.get(GRB.DoubleAttr.ObjVal) -
                        problem.getInstance().redundantORCap() * Params.IDLE_OPERATING_ROOM -
                        problem.getInstance().redundantRoomCap() * Params.IDLE_ROOM_CAPACITY;
                if (display) {
                    System.out.println("LR:\t" + obj + "\truntime per iteration:\t" +
                            ((double) (System.currentTimeMillis()) - runTime) / 1000
                    +"\truntime of master:\t" + master.get(GRB.DoubleAttr.Runtime)
                    +"\truntime of NPricing:\t" + EPricingTime/1000
                    );
                    EPricingTime = 0;
                    HPricingTime = 0;
                }
                runTime = (double) (System.currentTimeMillis());

            } while (solvePricing());
            if (display) {
                int colNum = Arrays.stream(columnSet).mapToInt(LinkedList::size).sum();
                System.out.println(String.format("Master problem is solved! %d columns are added.",colNum));
            }
//            ColumnXML.recordData("columns.xml", columnSet);
            runTime = (double) (System.currentTimeMillis() - Params.stateTime) / 1000;
            System.out.println(
                    Params.instanceName + "\tLinear relaxation:\t" + obj
                            + "\truntime:\t" + runTime
            );
//            checkSolution();
            return obj;
        } finally {
//            dispose();
        }
    }

    @Override
    public void updateDual() throws GRBException {
        constraints.updateDualValue();
    }

    Constraints getConstraints() {
        return constraints;
    }

    @Override
    public boolean addColumn(ColumnPatient column) throws GRBException {
        if (isNewColumn(column)) {
            GRBConstr[] constrs = constraints.getConstrs(problem.getPatientSet().get(column.getID
                    ()), column);
            double[] coeffs = constraints.getCoeffs();
            column.setVar(master.addVar(0, GRB.INFINITY, column.getCost(), GRB.CONTINUOUS,
                    constrs, coeffs,
                    String.format("lambda[%d,%d]", column.getID(), columnSet[column.getID()].size
                            ())));
            columnSet[column.getID()].add(column);
            return true;
        }
        return false;
    }

    @Override
    public int getAddColumnNum() {
        return 10;
    }

    public GRBVar[][][] getZr() {
        return zr;
    }

    public GRBVar[][][] getZv() {
        return zv;
    }

    public GRBVar[][] getZo() {
        return zo;
    }

    public Solution getSolution()throws GRBException{
        int[] delay = new int[patientNum];
        int[][] rooms = new int[patientNum][];
        int count = 0;
        for (int p = 0; p < delay.length; p++) {
            for (ColumnPatient columnPatient:columnSet[p]){
                if (Util.isEqual(columnPatient.getVar().get(GRB.DoubleAttr.X) , 1)){
                    delay[p] = columnPatient.getDelay();
                    rooms[p] = columnPatient.getRooms();
                    count ++;
                }
            }
            if (count != 1){
                throw new IllegalStateException("Patient "+problem.getPatientSet().get(p)+
                        "does not have feasible solution: count "+count);
            }
        }
        return new Solution(delay,rooms);
    }


    public GRBModel getModel(){
        return master;
    }

    @Override
    public void checkSolution() throws GRBException {
        double ptCost = 0;
        for (int p = 0; p < patientNum; p++) {
            for (Column column : columnSet[p]) {
                ptCost += column.getVar().get(GRB.DoubleAttr.X) *
                        column.getVar().get(GRB.DoubleAttr.Obj);
            }
        }
        double objVal = master.get(GRB.DoubleAttr.ObjVal) -
                problem.getInstance().redundantORCap() * Params.IDLE_OPERATING_ROOM -
                problem.getInstance().redundantRoomCap() * Params.IDLE_ROOM_CAPACITY;

        checkMIP.mipModel();
        if (checkMIP.withInitialSol(objVal, columnSet)){
            System.out.println("Linear relaxation:\t " + objVal);
            System.out.println("PatternCost:\t" + ptCost
                + "\tGenderPolicy:\t" + rgCost.getValue()
                + "\tOverCrowdRisk:\t" + riCost.getValue()
                + "\tIdleRoom:\t" + idleRoom.getValue()
                + "\tIdleOR:\t" + idleOR.getValue()
                + "\tOvertime:\t" + oorCost.getValue()
            );
        }
    }

    public static void main(String[] args) {
        try {
            Params.transfer = true;
            Params.STATIC = true;
            Params.genderPolicyConstr = "4"; //only for validate the solution with MipTr.

            PAS pas = new PAS(args);
            Problem problem = pas.getProblem();
            GRBEnv env = new GRBEnv();
            env.set(GRB.IntParam.OutputFlag, 0);
            env.set(GRB.IntParam.Threads, 1);
            Master master = new MasterPatient(env, problem,
                    Util.readSolution(problem.getInstance(), Params.importSolutionPath));
            master.solve();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

}
