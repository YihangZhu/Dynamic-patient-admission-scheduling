/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import algorithm.columnGeneration.PricingEAbs;
import gurobi.*;
import dpas.Params;
import dpas.instance.Instance;
import dpas.schedule.Problem;
import dpas.schedule.SPatient;
import util.Gurobi;
import util.Util;

public class PricingESolver extends PricingEAbs<MasterPatient, ColumnPatient> {
    private Instance instance;
    private SPatient patient;
    private GRBLinExpr rmCost = new GRBLinExpr();
    private GRBLinExpr trCost = new GRBLinExpr();
    private GRBLinExpr deCost = new GRBLinExpr();
    private GRBVar[] de;
    private GRBVar[][] roomAssign;
    private GRBVar[][] roomPAssign = null;

    PricingESolver(MasterPatient master, GRBEnv env, Problem problem, int ID) throws GRBException {
        super(ID, master);
        instance = problem.getInstance();
        patient = problem.getPatientSet().get(ID);
        createModel(env);
    }

    public void createModel(GRBEnv env) throws GRBException {
        slave = new GRBModel(env);
        slave.set(GRB.IntParam.OutputFlag, 0);
//        slave.set(GRB.DoubleParam.IntFeasTol,1e-8);
        slave.set(GRB.DoubleParam.MIPGap,1E-8);
        int roomNum = instance.getRoomNum();
        roomAssign = new GRBVar[roomNum][];
        GRBVar[][] transfer = new GRBVar[roomNum][];
        de = new GRBVar[patient.getMaxDelay() + 1];
        for (int i = 0; i < de.length; i++) {
            de[i] = slave.addVar(0, 1, 0, GRB.BINARY, String.format("x(%d)", i));
            deCost.addTerm(Params.DELAY * i, de[i]);
        }
        for (int r = 0; r < roomAssign.length; r++) {
            if (patient.roomAvailable(r)) {
                roomAssign[r] = new GRBVar[patient.getMaxDD()];
                transfer[r] = new GRBVar[patient.getMaxDD()];
                for (int d = patient.getEarliestAD(); d < roomAssign[r].length; d++) {
                    roomAssign[r][d] = slave.addVar(0, 1, 0, GRB.BINARY, String.format("y(%d,%d)" +
                            "", r, d));
                    rmCost.addTerm(patient.getRoomCost(r), roomAssign[r][d]);
                    transfer[r][d] = slave.addVar(0, 1, 0, GRB.BINARY, String.format("t(%d,%d)",
                            r, d));
                    trCost.addTerm(Params.TRANSFER, transfer[r][d]);
                }
            }
        }
        if (patient.getVariablity() > 0) {
            roomPAssign = new GRBVar[roomNum][];
            for (int r = 0; r < roomPAssign.length; r++) {
                if (patient.roomAvailable(r)) {
                    roomPAssign[r] = new GRBVar[patient.getMaxDD() + 1];
                    for (int d = patient.getEarliestDD(); d < roomPAssign[r].length; d++) {
                        roomPAssign[r][d] = slave.addVar(0, 1, 0, GRB.BINARY, String.format("y'" +
                                "(%d,%d)", r, d));
                    }
                }
            }
        }

        slave.update();
        //*****************************constraints********************************

        GRBLinExpr oneDe = new GRBLinExpr();
        for (GRBVar aDe : de) {
            oneDe.addTerm(1, aDe);
        }
        slave.addConstr(oneDe, GRB.EQUAL, 1, "oneDelay");

        GRBLinExpr s = new GRBLinExpr();
        for (GRBVar[] aRoomAssign : roomAssign) {
            if (aRoomAssign != null) {
                s.addTerm(1, aRoomAssign[patient.getEarliestAD()]);
            }
        }

        slave.addConstr(s, GRB.EQUAL, de[0], "consecutive1");
        for (int i = 1; i < de.length; i++) {
            GRBLinExpr s1 = new GRBLinExpr();
            for (GRBVar[] aRoomAssign : roomAssign) {
                if (aRoomAssign != null) {
                    s1.addTerm(1, aRoomAssign[patient.getEarliestAD() + i]);
                    s1.addTerm(-1, aRoomAssign[patient.getEarliestAD() + i - 1]);
                }
            }
            slave.addConstr(s1, GRB.LESS_EQUAL, de[i], String.format("consecutive2(%d)", i));
        }

        for (int d = patient.getMaxAD() + 1; d < patient.getMaxDD(); d++) {
            GRBLinExpr s1 = new GRBLinExpr();
            for (GRBVar[] aRoomAssign : roomAssign) {
                if (aRoomAssign != null) {
                    s1.addTerm(1, aRoomAssign[d]);
                    s1.addTerm(-1, aRoomAssign[d - 1]);
                }
            }
            slave.addConstr(s1, GRB.LESS_EQUAL, 0, String.format("consecutive3(%d)", d));
        }
        GRBLinExpr los = new GRBLinExpr();
        for (GRBVar[] aRoomAssign : roomAssign) {
            if (aRoomAssign != null) {
                for (GRBVar grbVar : aRoomAssign) {
                    if (grbVar != null) {
                        los.addTerm(1, grbVar);
                    }
                }
            }
        }
        slave.addConstr(los, GRB.EQUAL, patient.getRestLOS(), "los");
        for (int i = 0; roomPAssign != null && i < de.length; i++) {
            GRBLinExpr ps = new GRBLinExpr();
            for (int r = 0; r < roomPAssign.length; r++) {
                if (roomPAssign[r] != null) {
                    int d = i + patient.getEarliestDD();
                    ps.addTerm(1, roomPAssign[r][d]);
                    slave.addConstr(roomPAssign[r][d],
                            GRB.LESS_EQUAL, roomAssign[r][d - 1], String.format("ri(%d,%d)", r, i));
                }
            }
            slave.addConstr(ps, GRB.EQUAL, de[i], String.format("stayRisk(%d)", i));
        }
        for (int r = 0; r < roomAssign.length; r++) {
            for (int d = patient.getEarliestAD() + 1; roomAssign[r] != null && d < roomAssign[r]
                    .length; d++) {
                GRBLinExpr tr = new GRBLinExpr();
                tr.addTerm(1, roomAssign[r][d]);
                tr.addTerm(-1, roomAssign[r][d - 1]);
                if (d <= patient.getMaxAD()) {
                    tr.addTerm(-1, de[d - patient.getEarliestAD()]);
                }
                slave.addConstr(tr, GRB.LESS_EQUAL, transfer[r][d], String.format("transfer(%d," +
                        "%d)", r, d));
            }
        }
        if (!patient.isRegistered()) {
            GRBLinExpr tr = new GRBLinExpr();
            tr.addConstant(1);
            tr.addTerm(-1, roomAssign[patient.getPreRoomIndex()][patient.getEarliestAD()]);
            slave.addConstr(tr, GRB.LESS_EQUAL, transfer[patient.getPreRoomIndex()][patient
                    .getEarliestAD()], "transfer2");
        }
        slave.update();
    }

    public void updateObj() throws GRBException {
        obj.clear();
        obj.add(rmCost);
        obj.add(deCost);
        obj.add(trCost);
        master.getConstraints().updateObj(this, obj);
        slave.setObjective(obj, GRB.MINIMIZE);
        slave.update();
    }

    public double getColumn() throws GRBException {
        int delay = getDelay();
        int[] rooms = getRooms(delay + patient.getEarliestAD(), patient);
        newColumn = new ColumnPatient(ID, delay, rooms);
        return computeObj(newColumn);
    }

    private int getDelay() throws GRBException {
        int delay = -1;
        int count = 0;
        for (int d = 0; d < de.length; d++) {
            if (de[d].get(GRB.DoubleAttr.Xn) + 1e-2 >= 1) {
                delay = d;
                count++;
            }
        }
        if (count != 1) {
            Util.printArray(Gurobi.getValue(de),0 );
            throw new GRBException("The delay is not right");
        }
        return delay;
    }

    private int[] getRooms(int ad, SPatient patient) throws GRBException {
        int[] rooms = new int[patient.getRestLOS()];
        for (int d = 0; d < patient.getRestLOS(); d++) {
            int day = d + ad;
            int count = 0;
            for (int r = 0; r < roomAssign.length; r++) {
                if (roomAssign[r] != null && roomAssign[r][day].get(GRB.DoubleAttr.Xn) + 1e-2
                        >= 1) {
                    rooms[d] = r;
                    count++;
                }
            }
            if (count != 1) {
                Util.printArray(Gurobi.getValue(roomAssign),0 );
                Util.printArray(Gurobi.getValue(de), 0);
                throw new GRBException("The room is not right!"); //
            }
        }
        if (patient.getVariablity() > 0) {
            int count = 0;
            int dd = ad + patient.getRestLOS();
            for (int r = 0; r < roomAssign.length; r++) {
                if (roomAssign[r] != null && roomPAssign[r][dd].get(GRB.DoubleAttr.Xn) + 1e-2
                        >= 1) {
                    if (rooms[patient.getRestLOS() - 1] != r) {
                        throw new GRBException("The potential room is not right!");
                    }
                    count++;
                }
            }
            if (count != 1) {
                Util.printArray(Gurobi.getValue(roomPAssign), 0);
                throw new GRBException("The potential room is not right!");
            }
        }
        return rooms;
    }

    @Override
    public double computeObj(ColumnPatient column) {
        double roomCost = 0, trCost = 0, deCost = 0;

        for (int d = 0; d < patient.getRestLOS(); d++) {
            roomCost += patient.getRoomCost(column.getRooms()[d]);
        }
        if (patient.isRegistered()) {
            deCost += column.getDelay() * Params.DELAY;
        } else {
            trCost += patient.transfer(column.getRooms()[0]) * Params.TRANSFER;
        }
        for (int d = 1; d < patient.getRestLOS(); d++) {
            if (column.getRooms()[d - 1] != column.getRooms()[d]) {
                trCost += Params.TRANSFER;
            }
        }

        column.setCost(trCost + roomCost + deCost);

        return master.getConstraints().validateReducedCost(patient, column) +
                column.getCost();
    }


    public Instance getInstance() {
        return instance;
    }

    public SPatient getPatient() {
        return patient;
    }

    GRBVar[] getDe() {
        return de;
    }

    GRBVar[][] getRoomAssign() {
        return roomAssign;
    }

    GRBVar[][] getRoomPAssign() {
        return roomPAssign;
    }
}
