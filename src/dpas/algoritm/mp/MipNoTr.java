/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp;

import gurobi.*;
import dpas.Params;
import dpas.schedule.*;
import util.Gurobi;
import util.Util;

/**
 * Created by yihang on 3/16/17.
 */
public class MipNoTr extends PASMIP {
    private GRBVar[][][] derm;
    private GRBLinExpr[][][] roomAssign;

    MipNoTr(Problem problem, GRBEnv env, boolean relax) {
        super(env, problem, relax);
    }

    @Override
    public void mipModel() throws GRBException {
        super.model();

        derm = new GRBVar[patientsList.size()][][];
        GRBLinExpr[][] delay = new GRBLinExpr[patientsList.size()][];
        roomAssign = new GRBLinExpr[patientsList.size()][roomsList.length][];
        GRBLinExpr[][][] roomPAssign = new GRBLinExpr[patientsList.size()][][];
        for (int p = 0; p < roomAssign.length; p++) {
            SPatient patient = patientsList.get(p);
            for (int r = 0; r < roomAssign[p].length; r++) {
                if (patient.roomAvailable(r)) {
                    roomAssign[p][r] = new GRBLinExpr[patient.getMaxDD()];
                    for (int d = patient.getEarliestAD(); d < roomAssign[p][r].length; d++) {
                        roomAssign[p][r][d] = new GRBLinExpr();
                    }
                }
            }
            if (patient.getVariablity() > 0) {
                roomPAssign[p] = new GRBLinExpr[roomsList.length][];
                for (int r = 0; r < roomPAssign[p].length; r++) {
                    if (patient.roomAvailable(r)) {
                        roomPAssign[p][r] = new GRBLinExpr[patient.getMaxDD() + 1];
                        for (int d = patient.getEarliestDD(); d < roomPAssign[p][r].length; d++) {
                            roomPAssign[p][r][d] = new GRBLinExpr();
                        }
                    }
                }
            }
            delay[p] = new GRBLinExpr[patient.getMaxDelay() + 1];
            for (int d = 0; d < delay[p].length; d++) {
                delay[p][d] = new GRBLinExpr();
            }

            derm[p] = new GRBVar[patient.getMaxDelay() + 1][roomsList.length];
            for (int d = 0; d < derm[p].length; d++) {
                for (int r = 0; r < derm[p][d].length; r++) {
                    if (patient.roomAvailable(r)) {
                        derm[p][d][r] = model.addVar(0, 1, 0, relax ? GRB.CONTINUOUS : GRB
                                .BINARY, String.format("y[%d,%d,%d]", p, d, r));
                        delay[p][d].addTerm(1, derm[p][d][r]);
                        rmCost.addTerm(patient.getRoomCosts(r), derm[p][d][r]);
                        deCost.addTerm(Params.DELAY * patient.getPriority() * d, derm[p][d][r]);
                        for (int i = 0; i < patient.getRestLOS(); i++) {
                            int day = i + d + patient.getEarliestAD();
                            roomAssign[p][r][day].addTerm(1, derm[p][d][r]);
                        }
                        if (patient.getVariablity() > 0) {
                            int dd = d + patient.getEarliestAD() + patient.getRestLOS();
                            roomPAssign[p][r][dd].addTerm(1, derm[p][d][r]);
                        }
                    }
                }
            }
            if (!Params.suddenTr) {
                if (!patient.isRegistered()) {
                    derm[p][0][patient.getPreRoomIndex()].set(GRB.DoubleAttr.LB, 1);
                    derm[p][0][patient.getPreRoomIndex()].set(GRB.DoubleAttr.UB, 1);
                }
            }

            if (!patient.isRegistered()) {
                trCost.addConstant(Params.TRANSFER);
                if (derm[p][0][patient.getPreRoomIndex()] == null) {
                    throw new NullPointerException("The variable is null!");
                }
                trCost.addTerm(-Params.TRANSFER, derm[p][0][patient.getPreRoomIndex()]);
            }
        }

        // ****************************************************constraints: ****************
        for (int p = 0; p < derm.length; p++) {
            GRBLinExpr one = new GRBLinExpr();
            for (int d = 0; d < derm[p].length; d++) {
                for (int r = 0; r < roomsList.length; r++) {
                    if (derm[p][d][r] != null) {
                        one.addTerm(1, derm[p][d][r]);
                    }
                }
            }
            model.addConstr(one, GRB.EQUAL, 1, String.format("pi0[%d]", p));
        }

        GRBLinExpr[][] roomOccupancy = new GRBLinExpr[roomsList.length][futureDays];
        for (int r = 0; r < roomsList.length; r++) {
            int capacity = instance.getRoomCapacity(r);
            for (int d = currentDay; d < futureDays; d++) {
                roomOccupancy[r][d] = new GRBLinExpr();
                GRBLinExpr roomPTOccupancy = new GRBLinExpr();
                GRBLinExpr roomMaleOccupancy = new GRBLinExpr();
                GRBLinExpr roomFemaleOccupancy = new GRBLinExpr();
                for (int p = 0; p < patientsList.size(); p++) {
                    SPatient patient = patientsList.get(p);
                    if (roomAssign[p][r] != null) {
                        if (d < roomAssign[p][r].length && roomAssign[p][r][d] != null) {
                            roomOccupancy[r][d].add(roomAssign[p][r][d]);
                            roomPTOccupancy.add(roomAssign[p][r][d]);
                            if (patient.isMale()) {
                                roomMaleOccupancy.add(roomAssign[p][r][d]);
                            } else {
                                roomFemaleOccupancy.add(roomAssign[p][r][d]);
                            }
                        }
                        if (patient.getVariablity() > 0
                                && d < roomPAssign[p][r].length
                                && roomPAssign[p][r][d] != null) {
                            roomPTOccupancy.add(roomPAssign[p][r][d]);
                        }
                    }
                }
                if (d < instance.getPlanningHorizon()) {
                    totalRoomOccupancy.add(roomOccupancy[r][d]);
                }
                if (d == currentDay) {
                    currentDayRoomOccupancy.add(roomOccupancy[r][d]);
                }
                setRoomConstrs(r, d, capacity,
                        roomOccupancy[r][d], roomMaleOccupancy, roomFemaleOccupancy,
                        roomPTOccupancy);
            }
        }

        GRBLinExpr[] orDayOccupancy = new GRBLinExpr[futureDays];
        for (int d = currentDay; d < futureDays; d++) {
            for (int s = 0; s < instance.getSpecNum(); s++) {
                GRBLinExpr orSpecOccupancy = new GRBLinExpr();
                for (int p = 0; p < patientsList.size(); p++) {
                    SPatient patient = patientsList.get(p);
                    if (patient.needSurgery() && patient.isElective() && patient.getSpec() == s) {
                        int temp = d - patient.getEarliestSD();
                        if (temp < delay[p].length && temp >= 0) {
                            orSpecOccupancy.multAdd(patient.getSurDur(), delay[p][temp]);
                        }
                    }
                }
                if (d < instance.getPlanningHorizon()) {
                    totalOROccupancy.add(orSpecOccupancy);
                }
                setORSpecConstrs(d, s, orSpecOccupancy);
            }
            orDayOccupancy[d] = new GRBLinExpr();
            for (int p = 0; p < patientsList.size(); p++) {
                SPatient patient = patientsList.get(p);
                if (patient.needSurgery()) {
                    int temp = d - patient.getEarliestSD();
                    if (temp < delay[p].length && temp >= 0) {
                        orDayOccupancy[d].multAdd(patient.getSurDur(), delay[p][temp]);
                    }
                }
            }
            if (d < instance.getPlanningHorizon()) {
                totalORDayOccupancy.add(orDayOccupancy[d]);
            }
            if (d == currentDay) {
                currentDayOROccupancy.add(orDayOccupancy[d]);
            }

            setORDayConstrs(d, orDayOccupancy[d]);
        }

        if (!Params.STATIC) {
            if (Params.dynStrategy) {
                resourceUtilConstr(roomOccupancy, orDayOccupancy);
            }
            setDynamicIdleCosts();
        }

        model.update();
        setOBJ();
        model.update();
        //        model.write("model.lp");
    }

    @Override
    public void addRGConstr(int r, int d, int p) throws GRBException {
        if (roomAssign[p][r] != null && d < roomAssign[p][r].length && roomAssign[p][r][d] !=
                null) {
            if (patientsList.get(p).isMale()) {
                model.addConstr(male[r][d], GRB.GREATER_EQUAL, roomAssign[p][r][d], String.format
                        ("male(%d,%d)", r, d));
            } else {
                model.addConstr(female[r][d], GRB.GREATER_EQUAL, roomAssign[p][r][d], String
                        .format("female(%d,%d)", r, d));
            }
        }
    }

    private void resourceUtilConstr(GRBLinExpr[][] roomOccupancy, GRBLinExpr[] orDayOccupancy)
            throws GRBException {
        GRBVar[][] idleR = new GRBVar[roomsList.length][instance.getNumDays()];
        GRBVar[] idleOR = new GRBVar[instance.getNumDays()];
        GRBLinExpr expr = new GRBLinExpr();

        for (int d = currentDay; d < idleOR.length; d++) {
            // penaltyDecay is set such that the penalty decay to zero at the end of the planning horizon.
            double penaltyDecay = Math.pow(Params.penaltyDecay, d);

            if (penaltyDecay - Util.EPS>0) {
                double eCostR = penaltyDecay * problem.getRoomDemand() / (Params.a * instance
                        .getRoomNum());
                double eCostOR = penaltyDecay * problem.getOrDemand() / (Params.b * instance
                        .getORNum());
                // short term room idle penalty
                for (int r = 0; r < idleR.length; r++) {
                    idleR[r][d] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, String
                            .format("idleR" + "(%d,%d)", r, d));
                    eRI.addTerm(eCostR, idleR[r][d]);

                    expr.clear();
                    expr.add(roomOccupancy[r][d]);
                    expr.addTerm(1, idleR[r][d]);
                    model.addConstr(expr, GRB.GREATER_EQUAL, instance.getRoomCapacity(r), String
                            .format("idleRConstr(%d,%d)", r, d));
                }
                // short term OR idle penalty
                idleOR[d] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, String
                        .format("idleOR(%d)", d));
                eORI.addTerm(eCostOR, idleOR[d]);
                expr.clear();
                expr.add(orDayOccupancy[d]);
                expr.addTerm(1, idleOR[d]);
                model.addConstr(expr, GRB.GREATER_EQUAL, instance.getORDayCapacity(d), String
                        .format("idleORConstr(%d)", d));
            }
        }
    }

    private void setDynamicIdleCosts() throws GRBException {
        irCost.addConstant(Params.IDLE_ROOM_CAPACITY * instance.getTotalRoomCapacity(currentDay));
        irCost.multAdd(-Params.IDLE_ROOM_CAPACITY, totalRoomOccupancy);

        iorCost.addConstant(Params.IDLE_OPERATING_ROOM * instance.getTotalORCapacity(currentDay));
        iorCost.multAdd(-Params.IDLE_OPERATING_ROOM, totalORDayOccupancy);
        for (int d = currentDay; d < instance.getPlanningHorizon(); d++) {
            iorCost.addTerm(Params.IDLE_OPERATING_ROOM, overORDay[d]);
        }
    }

    public Solution getSolution() throws GRBException {
        int[][] results = new int[2][patientsList.size()];
        for (int p = 0; p < patientsList.size(); p++) {
            int count = 0;
            for (int d = 0; d < derm[p].length; d++) {
                for (int r = 0; r < roomsList.length; r++) {
                    if (derm[p][d][r] != null) {
                        double diff = derm[p][d][r].get(GRB.DoubleAttr.X) - Util.EPS;
                        if (diff > 0) {
                            results[0][p] = d;
                            results[1][p] = r;
                            count++;
                        }
                    }
                }
            }
            if (Util.isUnequal(count, 1, false)) {
                Gurobi.printConstraint(model, System.out, model.getConstrByName("pi0[5]"));
                Util.printArray(Gurobi.getValue(derm[p]),0); 
                throw new GRBException("result is not correct!");
            }
        }

        return new Solution(results[0], results[1]);
    }

    public void importSol() throws GRBException {
        ScheduleResult scheduleResult = Util.readSolution(instance, Params.importSolutionPath)
                .getScheduleResult();
        for (int p = 0; p < patientsList.size(); p++) {
            SPatient patient = scheduleResult.getSPatients(patientsList.get(p).getNumber());
            int de = patient.getAdmissionDay() - patient.getEarliestAD();
            int roomID = patient.getRooms(0);
            derm[p][de][roomID].set(GRB.DoubleAttr.Start, 1);
        }
        model.update();
    }
}
