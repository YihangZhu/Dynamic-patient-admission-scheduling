/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp;

//import com.sun.istack.internal.NotNull;

import dpas.Params;
import dpas.instance.Instance;
import dpas.instance.Room;
import dpas.schedule.Problem;
import dpas.schedule.SPatient;
import dpas.schedule.Solution;
import gurobi.*;
import util.Gurobi;
import util.Util;

import java.util.ArrayList;

/**
 * Created by yihang on 4/14/17.
 * abstract class for dpas mip model.
 */
public abstract class PASMIP {
    boolean relax;
    private GRBEnv env;
    Problem problem;
    protected int currentDay;
    ArrayList<SPatient> patientsList;
    Instance instance;
    Room[] roomsList;
    protected GRBModel model;
    private GRBVar[][] gc, roomRiskOver;
    GRBVar[][] male, female;
    private GRBVar[][] overOR;
    GRBVar[] overORDay;
    GRBLinExpr totalRoomOccupancy, totalOROccupancy, totalORDayOccupancy;
    GRBLinExpr currentDayRoomOccupancy, currentDayOROccupancy;
    GRBLinExpr rmCost, deCost, trCost;
    GRBLinExpr rgCost, riCost, oorCost;
    GRBLinExpr irCost, iorCost;
    GRBLinExpr eRI, eORI;
    int futureDays;

    PASMIP(GRBEnv env, Problem problem, boolean relax) {
        initialize(env, problem, relax);
    }

    private void initialize(GRBEnv env, Problem problem, boolean relax) {
        this.env = env;
        this.problem = problem;
        patientsList = problem.getPatientSet();
        instance = problem.getInstance();
        roomsList = instance.getRooms();
        this.relax = relax;
    }

    static PASMIP trMIPBuilder(Problem problem, GRBEnv env) {
        return new MipTr(env, problem, true); // only used to generate the relaxation value
    }

    static PASMIP noTrMIPBuilder(Problem problem, GRBEnv env) {
        return new MipNoTr(problem, env, false);
    }

    public Solution solve() throws GRBException {
        try {
            Gurobi.optimize(model);
            if (Params.display) {
                showResult();
            }
            if (!relax) {
                Solution solution = getSolution();
                solution.setRoomCost((int) rgCost.getValue());
                solution.setTrCost((int) trCost.getValue());
                solution.setDeCost((int) deCost.getValue());
                solution.setRiCost((int) riCost.getValue());
                solution.setOROORTO((int) oorCost.getValue());
                solution.setIRCost((int) irCost.getValue());
                solution.setORIdleTime((int) iorCost.getValue());
                solution.updateObjectiveValue(false);
                return solution;
            } else {
                return null;
            }
        } finally {
            model.dispose();
        }
    }

    private void showResult() throws GRBException {
        System.out.println("\tMIPObjVal:\t" + model.get(GRB.DoubleAttr.ObjVal)
                + "\truntime:\t" + model.get(GRB.DoubleAttr.Runtime)
                + "\tpatientsNumber:\t" + problem.getPatientSet().size()
                + "\troom Occupancy:\t" + currentDayRoomOccupancy.getValue() / instance
                .getTotalRoomCapacity()
                + "\tOR Occupancy:\t" + currentDayOROccupancy.getValue() / (1 + instance
                .getORDayCapacity(currentDay))
        );
        System.out.println(
                "roomCost:\t" + rmCost.getValue() +
                        "\tgenderPolicy:\t" + rgCost.getValue() +
                        "\ttransferCost:\t" + trCost.getValue() +
                        "\tdelayCost:\t" + deCost.getValue() +
                        "\toverCrowdRisk:\t" + riCost.getValue() +
                        "\tovertime:\t" + oorCost.getValue() +
                        "\tidleRoom:\t" + irCost.getValue() +
                        "\tidleOR:\t" + iorCost.getValue() +
                        "\teRoomIdle:\t" + eRI.getValue() +
                        "\teORIdle:\t" + eORI.getValue()
        );
    }

    void model() throws GRBException {
        model = new GRBModel(env);
        model.set(GRB.IntParam.NumericFocus, 1);
        rmCost = new GRBLinExpr();
        deCost = new GRBLinExpr();

        trCost = new GRBLinExpr();
        rgCost = new GRBLinExpr();
        irCost = new GRBLinExpr();
        riCost = new GRBLinExpr();
        oorCost = new GRBLinExpr();
        iorCost = new GRBLinExpr();
        eRI = new GRBLinExpr();
        eORI = new GRBLinExpr();
        totalRoomOccupancy = new GRBLinExpr();
        totalOROccupancy = new GRBLinExpr();
        totalORDayOccupancy = new GRBLinExpr();
        currentDay = problem.getCurrent();
        currentDayRoomOccupancy = new GRBLinExpr();
        currentDayOROccupancy = new GRBLinExpr();
        futureDays = instance.getNumDays();
        setRoomVars();
        setORVars();

    }

    private void setRoomVars() throws GRBException {
        gc = new GRBVar[roomsList.length][];
        roomRiskOver = new GRBVar[roomsList.length][futureDays];
        male = new GRBVar[roomsList.length][];
        female = new GRBVar[roomsList.length][];
        for (int r = 0; r < gc.length; r++) {
            if (instance.roomForSG(r)) {
                gc[r] = new GRBVar[futureDays];
                male[r] = new GRBVar[futureDays];
                female[r] = new GRBVar[futureDays];
                for (int d = currentDay; d < gc[r].length; d++) {
                        gc[r][d] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB.BINARY,
                                String.format("g(%d,%d)", r, d));
                        rgCost.addTerm(Params.GENDER_POLICY, gc[r][d]);
                        male[r][d] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB.BINARY,
                                String.format("m(%d,%d)", r, d));
                        male[r][d].set(GRB.IntAttr.BranchPriority, 10);
                        female[r][d] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB.BINARY,
                                String.format("f(%d,%d)", r, d));
                        female[r][d].set(GRB.IntAttr.BranchPriority, 10);
                }
            }
            for (int d = currentDay; d < roomRiskOver[r].length; d++) {
                roomRiskOver[r][d] = model.addVar(0, GRB.INFINITY, 0, relax ? GRB.CONTINUOUS :
                        GRB.INTEGER, String.format("o(%d,%d)", r, d));
                riCost.addTerm(Params.OVERCROWD_RISK, roomRiskOver[r][d]);
            }
        }
    }

    private void setORVars() throws GRBException {
        overOR = new GRBVar[futureDays][instance.getSpecNum()];
        overORDay = new GRBVar[futureDays];

        for (int d = currentDay; d < overOR.length; d++) {
            for (int s = 0; s < overOR[d].length; s++) {
                overOR[d][s] = model.addVar(0, instance.getSpecAOT(d,
                        s), 0, GRB.CONTINUOUS, String.format("oO(%d,%d)",
                        d, s));
                oorCost.addTerm(Params.OVERTIME, overOR[d][s]);
            }
            overORDay[d] = model.addVar(0, Params
                    .ADMITTED_TOTAL_OVERTIME, 0, GRB.CONTINUOUS, String
                    .format("oT(%d)", d));
            oorCost.addTerm(Params.OVERTIME, overORDay[d]);
        }
    }

    void setRoomConstrs(int r, int d, int capacity,
                        GRBLinExpr roomOccupancy,
                        GRBLinExpr roomMaleOccupancy,
                        GRBLinExpr roomFemaleOccupancy,
                        GRBLinExpr roomPTOccupancy
    ) throws GRBException {
        model.addConstr(roomOccupancy, GRB.LESS_EQUAL, capacity, String.format("rcConstr(%d,%d)",
                r, d));
        roomPTOccupancy.addTerm(-1, roomRiskOver[r][d]);
        model.addConstr(roomPTOccupancy, GRB.LESS_EQUAL, capacity, String.format("roConstr(%d,%d)" +
                "", r, d));
        if (gc[r] != null) {
            GRBLinExpr expr = new GRBLinExpr();
            switch (Params.genderPolicyConstr) {
                case "4": // Ceschia 2011
                    for (int p = 0; p < patientsList.size(); p++) {
                        addRGConstr(r, d, p);
                    }
                    GRBLinExpr rg = new GRBLinExpr();
                    rg.addTerm(1, male[r][d]);
                    rg.addTerm(1, female[r][d]);
                    rg.addConstant(-1);
                    model.addConstr(gc[r][d], GRB.GREATER_EQUAL, rg, String.format("rg(%d,%d)",
                            r, d));
                    break;
                case "5":// yi-hang 2018
                    expr.clear();
                    expr.addTerm(capacity, male[r][d]);
                    model.addConstr(roomMaleOccupancy, GRB.LESS_EQUAL, expr, String.format
                            ("rgConstr1(%d,%d)", r, d));
                    expr.clear();
                    expr.addTerm(capacity, female[r][d]);
                    model.addConstr(roomFemaleOccupancy, GRB.LESS_EQUAL, expr, String.format
                            ("rgConstr2(%d,%d)", r, d));
                    expr.clear();
                    expr.addTerm(1, male[r][d]);
                    expr.addTerm(1, female[r][d]);
                    expr.addConstant(-1);
                    model.addConstr(expr, GRB.LESS_EQUAL, gc[r][d], String.format("rgConstr3(%d," +
                            "%d)", r, d));
                    break;
//                case "6": //hard
//                    expr.clear();
//                    expr.addTerm(capacity, male[r][d]);
//                    model.addConstr(roomMaleOccupancy, GRB.LESS_EQUAL, expr, String.format
//                            ("rgConstr1(%d,%d)", r, d));
//                    expr.clear();
//                    expr.addTerm(capacity, female[r][d]);
//                    model.addConstr(roomFemaleOccupancy, GRB.LESS_EQUAL, expr, String.format
//                            ("rgConstr2(%d,%d)", r, d));
//                    expr.clear();
//                    expr.addTerm(1, male[r][d]);
//                    expr.addTerm(1, female[r][d]);
//                    expr.addConstant(-1);
//                    model.addConstr(expr, GRB.LESS_EQUAL, 0, String.format("rgConstr3(%d,%d)", r,
//                            d));
//                    break;
                default:
                    System.out.println("Please select a gender policy constraint: among {1 2 3 4 " +
                            "5 6}.");
            }
        }
    }

    void setORSpecConstrs(int d, int s, GRBLinExpr orSpecOccupancy) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr(orSpecOccupancy);
        expr.addConstant(-instance.getORSpecCapacity(d, s));
        model.addConstr(expr, GRB.LESS_EQUAL, overOR[d][s], String.format("orsoConstr(%d,%d)", d,
                s));
    }

    void setORDayConstrs(int d, GRBLinExpr orDayOccupancy) throws GRBException {
        GRBLinExpr expr = new GRBLinExpr(orDayOccupancy);
        expr.addConstant(-instance.getORDayCapacity(d));
        model.addConstr(expr, GRB.LESS_EQUAL, overORDay[d], String.format("ordoConstr(%d)", d));
    }

    private void setIdleCosts() throws GRBException {
        irCost.addConstant(Params.IDLE_ROOM_CAPACITY * instance.maximumRoomUsage());
        irCost.multAdd(-Params.IDLE_ROOM_CAPACITY, totalRoomOccupancy);

        iorCost.addConstant(Params.IDLE_OPERATING_ROOM * instance.maximumORUsage());
        iorCost.multAdd(-Params.IDLE_OPERATING_ROOM, totalOROccupancy);
        for (int d = currentDay; d < instance.getPlanningHorizon(); d++) {
            for (GRBVar var : overOR[d]) {
                iorCost.addTerm(Params.IDLE_OPERATING_ROOM, var);
            }
        }
    }

    void setOBJ() throws GRBException {
        model.update();
        if (Params.STATIC) {
            setIdleCosts();
        }
        GRBLinExpr obj = new GRBLinExpr();
        obj.add(rmCost);
        obj.add(deCost);
        obj.add(trCost);
        obj.add(rgCost);
        obj.add(riCost);
        obj.add(oorCost);
//        obj.add(urCost);
//        obj.add(uorCost);
        obj.add(irCost);
        obj.add(iorCost);
        obj.add(eRI);
        obj.add(eORI);

        model.setObjective(obj, GRB.MINIMIZE);
    }

    void importUB() throws GRBException {
        int UB = Util.readSolution(instance, Params.importSolutionPath)
                .getObjectiveValue();
        model.set(GRB.DoubleParam.Cutoff, UB * 1.001);
    }

    public abstract void mipModel() throws GRBException;

    public abstract Solution getSolution() throws GRBException;

    public abstract void addRGConstr(int r, int d, int p) throws GRBException;

    @SuppressWarnings("unused")
    public abstract void importSol() throws GRBException;
}
