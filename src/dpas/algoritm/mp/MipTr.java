/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp;

import gurobi.*;
import dpas.Params;
import dpas.algoritm.mp.cg.ColumnPatient;
import dpas.schedule.Problem;
import dpas.schedule.SPatient;
import dpas.schedule.ScheduleResult;
import dpas.schedule.Solution;
import util.DrawModel;
import util.Gurobi;
import util.Util;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * mip model with room transfer
 */

public class MipTr extends PASMIP {
    private GRBVar[][] de;
    private GRBVar[][][] roomAssign;
    private GRBVar[][][] roomPAssign;
    private int patientNum;

    public MipTr(GRBEnv env, Problem problem, boolean relax) {
        super(env, problem, relax);
    }

    @Override
    public void mipModel() throws GRBException {
        super.model();
        patientNum = patientsList.size();
        // variables definition
        de = new GRBVar[patientNum][];
        roomAssign = new GRBVar[patientNum][roomsList.length][];
        GRBVar[][][] transfer = new GRBVar[patientNum][roomsList.length][];
        roomPAssign = new GRBVar[patientNum][][];
        for (int p = 0; p < roomAssign.length; p++) {
            SPatient patient = patientsList.get(p);
            de[p] = new GRBVar[patient.getMaxDelay() + 1];
            for (int i = 0; i < de[p].length; i++) {
                de[p][i] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB.BINARY, String
                        .format("x(%d,%d)", p, i));
                deCost.addTerm(Params.DELAY * patient.getPriority() * i, de[p][i]);
            }
            for (int r = 0; r < roomAssign[p].length; r++) {
                if (patient.roomAvailable(r)) {
                    roomAssign[p][r] = new GRBVar[patient.getMaxDD()];
                    transfer[p][r] = new GRBVar[patient.getMaxDD()];
                    for (int d = patient.getEarliestAD(); d < roomAssign[p][r].length; d++) {
                        roomAssign[p][r][d] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB
                                .BINARY, String.format("y(%d,%d,%d)", p, r, d));
                        rmCost.addTerm(patient.getRoomCost(r), roomAssign[p][r][d]);
                        transfer[p][r][d] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB
                                .BINARY, String.format("t(%d,%d,%d)", p, r, d));
                        trCost.addTerm(Params.TRANSFER, transfer[p][r][d]);
                    }
                }
            }
            if (patient.getVariablity() > 0) {
                roomPAssign[p] = new GRBVar[roomsList.length][];
                for (int r = 0; r < roomPAssign[p].length; r++) {
                    if (patient.roomAvailable(r)) {
                        roomPAssign[p][r] = new GRBVar[patient.getMaxDD() + 1];
                        for (int d = patient.getEarliestDD(); d < roomPAssign[p][r].length; d++) {
                            roomPAssign[p][r][d] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS :
                                    GRB.BINARY, String.format("y'(%d,%d,%d)", p, r, d));
                        }
                    }
                }
            }
        }
        // ****************************************************constraints ***********************
        for (int p = 0; p < de.length; p++) {
            SPatient patient = patientsList.get(p);
            GRBLinExpr oneDe = new GRBLinExpr();
            for (int d = 0; d < de[p].length; d++) {
                oneDe.addTerm(1, de[p][d]);
            }
            model.addConstr(oneDe, GRB.EQUAL, 1, String.format("pi0(%d)", p));

            GRBLinExpr s = new GRBLinExpr();
            for (int r = 0; r < roomAssign[p].length; r++) {
                if (roomAssign[p][r] != null) {
                    s.addTerm(1, roomAssign[p][r][patient.getEarliestAD()]);
                }
            }
            model.addConstr(s, GRB.EQUAL, de[p][0], String.format("pi1(%d)", p));
            for (int i = 1; i < de[p].length; i++) {
                GRBLinExpr s1 = new GRBLinExpr();
                for (int r = 0; r < roomAssign[p].length; r++) {
                    if (roomAssign[p][r] != null) {
                        s1.addTerm(1, roomAssign[p][r][patient.getEarliestAD() + i]);
                        s1.addTerm(-1, roomAssign[p][r][patient.getEarliestAD() + i - 1]);
                    }
                }
                model.addConstr(s1, GRB.LESS_EQUAL, de[p][i], String.format("pi2(%d,%d)", p, i));
            }
            for (int d = patient.getMaxAD() + 1; d < patient.getMaxDD(); d++) {
                GRBLinExpr s1 = new GRBLinExpr();
                for (int r = 0; r < roomAssign[p].length; r++) {
                    if (roomAssign[p][r] != null) {
                        s1.addTerm(1, roomAssign[p][r][d]);
                        s1.addTerm(-1, roomAssign[p][r][d - 1]);
                    }
                }
                model.addConstr(s1, GRB.LESS_EQUAL, 0, String.format("pi3(%d,%d)", p, d));
            }
            GRBLinExpr los = new GRBLinExpr();
            for (int r = 0; r < roomAssign[p].length; r++) {
                for (int d = patient.getEarliestAD(); roomAssign[p][r] != null && d <
                        roomAssign[p][r].length; d++) {
                    los.addTerm(1, roomAssign[p][r][d]);
                }
            }
            model.addConstr(los, GRB.EQUAL, patient.getRestLOS(), String.format("pi4(%d)", p));
            for (int i = 0; roomPAssign[p] != null && i < de[p].length; i++) {
                GRBLinExpr ps = new GRBLinExpr();
                for (int r = 0; r < roomPAssign[p].length; r++) {
                    if (roomPAssign[p][r] != null) {
                        ps.addTerm(1, roomPAssign[p][r][i + patient.getEarliestDD()]);
                        model.addConstr(roomPAssign[p][r][i + patient.getEarliestDD()],
                                GRB.LESS_EQUAL,
                                roomAssign[p][r][i + patient.getEarliestDD() - 1],
                                String.format("pi5.1(%d,%d,%d)", p, r, i));
                    }
                }
                model.addConstr(ps, GRB.EQUAL, de[p][i], String.format("pi5(%d,%d)", p, i));
            }
            for (int r = 0; r < roomAssign[p].length; r++) {
                for (int d = patient.getEarliestAD() + 1; roomAssign[p][r] != null && d <
                        roomAssign[p][r].length; d++) {
                    GRBLinExpr tr = new GRBLinExpr();
                    tr.addTerm(1, roomAssign[p][r][d]);
                    tr.addTerm(-1, roomAssign[p][r][d - 1]);
                    if (d <= patient.getMaxAD()) {
                        tr.addTerm(-1, de[p][d - patient.getEarliestAD()]);
                    }
                    model.addConstr(tr, GRB.LESS_EQUAL, transfer[p][r][d], String.format("pi6(%d," +
                            "%d,%d)", p, r, d));
                }
            }
            if (!patient.isRegistered()) {
                GRBLinExpr tr = new GRBLinExpr();
                tr.addConstant(1);
                tr.addTerm(-1, roomAssign[p][patient.getPreRoomIndex()][patient.getEarliestAD()]);
                model.addConstr(tr, GRB.LESS_EQUAL, transfer[p][patient.getPreRoomIndex()
                        ][patient.getEarliestAD()], String.format("pi7(%d)", p));
            }
        }

        for (int r = 0; r < roomsList.length; r++) {
            int capacity = instance.getRoomCapacity(r);
            for (int d = currentDay; d < instance.getNumDays(); d++) {
                GRBLinExpr roomOccupancy = new GRBLinExpr();
                GRBLinExpr roomPTOccupancy = new GRBLinExpr();
                GRBLinExpr roomMaleOccupancy = new GRBLinExpr();
                GRBLinExpr roomFemaleOccupancy = new GRBLinExpr();
                for (int p = 0; p < patientsList.size(); p++) {
                    SPatient patient = patientsList.get(p);
                    if (roomAssign[p][r] != null) {
                        if (d >= patient.getEarliestAD() && d < roomAssign[p][r].length) {
                            roomOccupancy.addTerm(1, roomAssign[p][r][d]);
                            roomPTOccupancy.addTerm(1, roomAssign[p][r][d]);
                            if (patient.isMale()) {
                                roomMaleOccupancy.addTerm(1, roomAssign[p][r][d]);
                            } else {
                                roomFemaleOccupancy.addTerm(1, roomAssign[p][r][d]);
                            }
                        }
                        if (patient.getVariablity() > 0
                                && d >= patient.getEarliestDD()
                                && d < roomPAssign[p][r].length) {
                            roomPTOccupancy.addTerm(1, roomPAssign[p][r][d]);
                        }
                    }
                }
                if (d < instance.getPlanningHorizon()) {
                    totalRoomOccupancy.add(roomOccupancy);
                }
                setRoomConstrs(r, d, capacity,
                        roomOccupancy, roomMaleOccupancy, roomFemaleOccupancy, roomPTOccupancy);
            }
        }

        for (int d = currentDay; d < instance.getNumDays(); d++) {
            for (int s = 0; s < instance.getSpecNum(); s++) {
                GRBLinExpr orSpecOccupancy = new GRBLinExpr();
                for (int p = 0; p < patientsList.size(); p++) {
                    SPatient patient = patientsList.get(p);
                    if (patient.needSurgery() && patient.isElective() && patient.getSpec() == s) {
                        int temp = d - patient.getEarliestSD();
                        if (temp < de[p].length && temp >= 0) {
                            orSpecOccupancy.addTerm(patient.getSurDur(), de[p][temp]);
                        }
                    }
                }
                if (d < instance.getPlanningHorizon()) {
                    totalOROccupancy.add(orSpecOccupancy);
                }
                setORSpecConstrs(d, s, orSpecOccupancy);
            }
            GRBLinExpr orDayOccupancy = new GRBLinExpr();
            for (int p = 0; p < patientsList.size(); p++) {
                SPatient patient = patientsList.get(p);
                if (patient.needSurgery()) {
                    int temp = d - patient.getEarliestSD();
                    if (temp < de[p].length && temp >= 0) {
                        orDayOccupancy.addTerm(patient.getSurDur(), de[p][temp]);
                    }
                }
            }
            setORDayConstrs(d, orDayOccupancy);
        }

        setOBJ();
        model.update();
//        model.write("modelTr.lp");
    }

    @Override
    public void addRGConstr(int r, int d, int p) throws GRBException {
        if (roomAssign[p][r] != null && d < roomAssign[p][r].length && roomAssign[p][r][d] !=
                null) {
            if (patientsList.get(p).isMale()) {
                model.addConstr(male[r][d], GRB.GREATER_EQUAL, roomAssign[p][r][d], String.format
                        ("male(%d,%d,%d)", r, d, p));
            } else {
                model.addConstr(female[r][d], GRB.GREATER_EQUAL, roomAssign[p][r][d], String
                        .format("female(%d,%d,%d)", r, d, p));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean withInitialSol(double objVal, LinkedList<?>[] set) throws GRBException {
        LinkedList<ColumnPatient>[] columnSet = (LinkedList<ColumnPatient>[]) set;
        double[][][] x = new double[patientsList.size()][roomsList.length][instance.getNumDays()];
        double[][][] y = new double[patientsList.size()][roomsList.length][instance.getNumDays()];
        double[][] z = new double[patientsList.size()][instance.getNumDays()];
        double pCost = 0;
        for (int p = 0; p < columnSet.length; p++) {
            double totVal = 0;
            SPatient patient = patientsList.get(p);
//            if (p==28){
//                columnSet[p].forEach(columnPatient ->
//                {
//                    try {
//                        System.out.println(
//                                String.format("Patient:%d, var: %f, delay:%d, room:",
//                                        columnPatient.getID(),
//                                        columnPatient.getVar().get(GRB.DoubleAttr.X),
//                                        columnPatient.getDelay()));
//                        Arrays.stream(columnPatient.getRooms()).forEach(r->System.out.print(r+" "));
//                    } catch (GRBException e) {
//                        e.printStackTrace();
//                    }
//                });
//
//            }
            for (ColumnPatient column : columnSet[p]) {
                double val = column.getVar().get(GRB.DoubleAttr.X);
                if (val > Util.EPS) {
                    totVal += val;
                    pCost += val * column.getCost();
                    int[] rooms = column.getRooms();
                    int ad = column.getDelay() + patient.getEarliestAD();
                    z[p][column.getDelay()] += val;
                    int i = 0;
                    for (; i < patient.getRestLOS(); i++) {
                        int d = ad + i;
                        int r = rooms[i];
                        x[p][r][d] += val;
                    }
                    if (patient.getVariablity() > 0) {
                        int dd = ad + patient.getRestLOS();
                        int r = rooms[i - 1];
                        if (dd < instance.getNumDays()) {
                            y[p][r][dd] += val;
                        }
                    }
                }
            }
            if (totVal + Util.EPS < 1) {
                throw new GRBException("Convexity constraint problem:\t" + totVal);
            }

            for (int r = 0; r < x[p].length; r++) {
                for (int d = 0; d < x[p][r].length; d++) {
                    if (x[p][r][d] > Util.EPS) {
                        roomAssign[p][r][d].set(GRB.DoubleAttr.LB, x[p][r][d]);
                        roomAssign[p][r][d].set(GRB.DoubleAttr.UB, x[p][r][d]);
                    }
                    if (y[p][r][d] > Util.EPS) {
                        roomPAssign[p][r][d].set(GRB.DoubleAttr.LB, y[p][r][d]);
                        roomPAssign[p][r][d].set(GRB.DoubleAttr.UB, y[p][r][d]);
                    }
                }
            }
            for (int d = 0; d < patient.getMaxDelay() + 1; d++) {
                de[p][d].set(GRB.DoubleAttr.LB, z[p][d]);
                de[p][d].set(GRB.DoubleAttr.UB, z[p][d]);
            }
        }

        GRBLinExpr obj = new GRBLinExpr();
        obj.addConstant(pCost); // doesn't check Tr, De, and PRC.
        obj.add(rgCost);
        obj.add(irCost);
        obj.add(riCost);
        obj.add(oorCost);
        obj.add(iorCost);
        model.setObjective(obj, GRB.MINIMIZE);
        Gurobi.optimize(model);
        if (Util.isUnequal(objVal, model.get(GRB.DoubleAttr.ObjVal), true)) {
            System.out.println("\tMIPObjVal:\t" + model.get(GRB.DoubleAttr.ObjVal) +
                    "\tgenderPolicy:\t" + rgCost.getValue() +
                    "\toverCrowdRisk:\t" + riCost.getValue() +
                    "\tidleRoom:\t" + irCost.getValue() +
                    "\tidleOR:\t" + iorCost.getValue() +
                    "\tovertime:\t" + oorCost.getValue()
            );
            return true;
        }
        System.exit(0);
        return false;
    }

//    void drawPatientDecompose()throws GRBException{
//        DrawModel drawModel = new DrawModel(model);
//        drawModel.drawOrigModel();
//        int p = 0;
//        for (;p < patientNum; p++) {
//            drawModel.pricingVars.put(p,new ArrayList<>());
//            drawModel.sortVar(de[p],p);
//            for (GRBVar[] vars : roomAssign[p]){
//                drawModel.sortVar(vars,p);
//            }
//            if (roomPAssign[p]!=null) {
//                for (GRBVar[] vars : roomPAssign[p]) {
//                    drawModel.sortVar(vars, p);
//                }
//            }
//            for (GRBVar[] vars : transfer[p]) {
//                drawModel.sortVar(vars,p);
//            }
//            drawModel.sortConstrs(p);
//        }
//        drawModel.printVisualization(p,patientNum);
//    }
//
//    void drawRoomDecompose()throws GRBException{
//        DrawModel drawModel = new DrawModel(model);
//        drawModel.drawOrigModel();
//        int r = 0;
//        for (;r < roomsList.length; r++) {
//            drawModel.pricingVars.put(r,new ArrayList<>());
//            drawModel.sortVar(gc[r], r);
//            drawModel.sortVar(roomRiskOver[r],r);
//            drawModel.sortVar(male[r],r);
//            drawModel.sortVar(female[r],r);
//            for (GRBVar[][] vars: roomAssign) {
//                drawModel.sortVar(vars[r],r);
//            }
//            for (GRBVar[][] vars : roomPAssign) {
//                if (vars!=null) {
//                    drawModel.sortVar(vars[r], r);
//                }
//            }
//            for (GRBVar[][] vars : transfer) {
//                drawModel.sortVar(vars[r],r);
//            }
//            drawModel.sortConstrs(r);
//        }
//        drawModel.printVisualization(r, roomsList.length);
//    }

    public void importSol() throws GRBException {
        ScheduleResult scheduleResult = Util.readSolution(instance, Params.importSolutionPath)
                .getScheduleResult();
        for (int p = 0; p < patientsList.size(); p++) {
            SPatient patient = scheduleResult.getSPatients(patientsList.get(p).getNumber());
            int ad = patient.getAdmissionDay();
            for (int i = 0; i < patient.getTotalLOS(); i++) {
                int d = ad + i;
                int r = patient.getRooms(i);
                roomAssign[p][r][d].set(GRB.DoubleAttr.Start, 1);
            }
            if (patient.getVariablity() > 0) {
                int d = ad + patient.getTotalLOS();
                int r = patient.getRooms(patient.getTotalLOS() - 1);
                roomPAssign[p][r][d].set(GRB.DoubleAttr.Start, 1);
            }
        }
    }

    @Override
    public Solution getSolution() throws GRBException {
        int[] de = getDelays();
        int[][] rooms = getRooms();

//        new CheckSol(problem,de,rooms,model.get(GRB.DoubleAttr.ObjVal));
        return new Solution(de, rooms);
    }

    private int[] getDelays() throws GRBException {
        int[] admissionDelay = new int[patientNum];
        for (int p = 0; p < de.length; p++) {
            int count = 0;
            for (int i = 0; i < de[p].length; i++) {
                double diff = de[p][i].get(GRB.DoubleAttr.X) - Util.EPS;
                if (diff > 0) {
                    admissionDelay[p] = i;
                    count++;
                }
            }
            if (count != 1) {
                Util.printArray(Gurobi.getValue(de),0);
                throw new GRBException(String.format("Patient %d has %d delay values", p, count));
            }
        }
        return admissionDelay;
    }

    private int[][] getRooms() throws GRBException {
        int[][] rooms = new int[patientNum][];
        for (int p = 0; p < patientNum; p++) {
            SPatient patient = patientsList.get(p);
            int count = 0;
            rooms[p] = new int[patient.getRestLOS()];
            for (int d = patient.getEarliestAD(); d < patient.getMaxDD(); d++) {
                for (int r = 0; r < roomAssign[p].length; r++) {
                    if (roomAssign[p][r] != null) {
                        double diff = roomAssign[p][r][d].get(GRB.DoubleAttr.X) - Util.EPS;
                        if (diff > 0) {
                            rooms[p][count] = r;
                            if (!patient.roomAvailable(r)) {
                                throw new GRBException("The room is not suitable for patient!");
                            }
                            count++;
                        }
                    }
                }
            }
            if (patient.getVariablity() > 0) {
                for (int r = 0; r < roomPAssign[p].length; r++) {
                    if (roomPAssign[p][r] != null) {
                        for (GRBVar var : roomPAssign[p][r]) {
                            if (var != null) {
                                double diff = var.get(GRB.DoubleAttr.X) - Util.EPS;
                                if (diff > 0) {
                                    if (rooms[p][count - 1] != r) {
                                        throw new GRBException("The room is not correct!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (count != rooms[p].length) {
                throw new GRBException(String.format("Patient %d get %d rooms", p, count));
            }
        }
        return rooms;
    }
}
