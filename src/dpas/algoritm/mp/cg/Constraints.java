/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import gurobi.GRB;
import gurobi.GRBConstr;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import dpas.schedule.SPatient;

import java.util.LinkedList;

public class Constraints {
    private GRBConstr[] constrsConvexity; // p0 convexity constraints
    private double[] dualConvexity;

    private GRBConstr[][][] constrAux1;
    private GRBConstr[][][] constrAux2;
    private GRBConstr[][] constrAux3;

    private double[][][] dualAux1;
    private double[][][] dualAux2;
    private double[][] dualAux3;

    private GRBConstr[][] constrsRC; // room capacity constraints
    private double[][] dualRC;

    private GRBConstr[][] constrsRO; // room overcrowd risk constraints.
    private double[][] dualRO;

    private GRBConstr[][][] constrsRGMale; // room gender policy constraints for male patients
    private GRBConstr[][][] constrsRGFemale; // room gender policy constraints for female patients
    private double[][][] dualRGMale;
    private double[][][] dualRGFemale;

    private GRBConstr[][] constrsRI; // pi 7 room idle capacity during the original planning horizon
    private double[][] dualRI;

    private GRBConstr[] constrsORDO; // operating room day overtime
    private double[] dualORDO;

    private GRBConstr[][] constrsORSO; // operating room specialism overtime
    private double[][] dualORSO;

    private GRBConstr[][] constrsORI; // operating room specialism idle capacity during the
    // original planning horizon
    private double[][] dualORIdle;

    private LinkedList<GRBConstr> constrs = new LinkedList<>();
    private LinkedList<Integer> coeffs = new LinkedList<>();
    private boolean newFormulation = false;

    void setNewFormulation(boolean newFormulation) {
        this.newFormulation = newFormulation;
    }

    void initConstrs(int patientNum, int roomNum, int dayNum, int specNum, int originalHorizon) {
        constrsConvexity = new GRBConstr[patientNum];

        constrAux1 = new GRBConstr[patientNum][roomNum][dayNum];
        constrAux2 = new GRBConstr[patientNum][roomNum][dayNum];
        constrAux3 = new GRBConstr[patientNum][dayNum];

        constrsRC = new GRBConstr[roomNum][dayNum];
        constrsRO = new GRBConstr[roomNum][dayNum];
        constrsRGMale = new GRBConstr[roomNum][][];
        constrsRGFemale = new GRBConstr[roomNum][][];

        constrsORDO = new GRBConstr[dayNum];
        constrsORSO = new GRBConstr[dayNum][specNum];

        constrsORI = new GRBConstr[originalHorizon][specNum];
        constrsRI = new GRBConstr[roomNum][originalHorizon];
    }

    void initDuals() {
        dualConvexity = new double[constrsConvexity.length];
        if (newFormulation) {
            dualAux1 = new double[constrAux1.length][constrAux1[0].length][constrAux1[0][0].length];
            dualAux2 = new double[constrAux2.length][constrAux2[0].length][constrAux2[0][0].length];
            dualAux3 = new double[constrAux3.length][constrAux3[0].length];
        }else {
            dualRC = new double[constrsRC.length][constrsRC[0].length];
            dualRO = new double[constrsRO.length][constrsRO[0].length];
            dualRI = new double[constrsRI.length][constrsRI[0].length];

            dualRGMale = new double[constrsRGMale.length][][];
            dualRGFemale = new double[constrsRGFemale.length][][];
            for (int i = 0; i < dualRGMale.length; i++) {
                if (constrsRGMale[i] != null) {
                    dualRGMale[i] = new double[constrsRGMale[i].length][constrsRGMale[i][0].length];
                    dualRGFemale[i] = new double[constrsRGFemale[i].length][constrsRGFemale[i][0]
                            .length];
                }
            }
            dualORDO = new double[constrsORDO.length];
            dualORSO = new double[constrsORSO.length][constrsORSO[0].length];
            dualORIdle = new double[constrsORI.length][constrsORI[0].length];
        }
    }

    void updateDualValue() throws GRBException {
        for (int i = 0; i < dualConvexity.length; i++) {
            dualConvexity[i] = constrsConvexity[i].get(GRB.DoubleAttr.Pi);
        }
        if (newFormulation) {
            for (int p = 0; p < constrAux1.length; p++) {
                for (int r = 0; r < constrAux1[p].length; r++) {
                    for (int d = 0; d < constrAux1[p][r].length; d++) {
                        if (constrAux1[p][r][d] != null) {
                            dualAux1[p][r][d] = constrAux1[p][r][d].get(GRB.DoubleAttr.Pi);
                        }
                        if (constrAux2[p][r][d] != null) {
                            dualAux2[p][r][d] = constrAux2[p][r][d].get(GRB.DoubleAttr.Pi);
                        }
                    }
                }
            }
            for (int p = 0; p < constrAux3.length; p++) {
                for (int d = 0; d < constrAux3[p].length; d++) {
                    if (constrAux3[p][d] != null) {
                        dualAux3[p][d] = constrAux3[p][d].get(GRB.DoubleAttr.Pi);
                    }
                }
            }
        }else {
            for (int r = 0; r < dualRC.length; r++) {
                for (int d = 0; d < dualRC[r].length; d++) {
                    dualRC[r][d] = constrsRC[r][d].get(GRB.DoubleAttr.Pi);
                    dualRO[r][d] = constrsRO[r][d].get(GRB.DoubleAttr.Pi);
                }
                for (int d = 0; d < dualRI[r].length; d++) {
                    dualRI[r][d] = constrsRI[r][d].get(GRB.DoubleAttr.Pi);
                }
                if (constrsRGMale[r] != null) {
                    for (int d = 0; d < dualRGMale[r].length; d++) {
                        for (int p = 0; p < dualRGMale[r][d].length; p++) {
                            if (constrsRGMale[r][d][p] != null) {
                                dualRGMale[r][d][p] = constrsRGMale[r][d][p].get(GRB.DoubleAttr.Pi);
                            } else if (constrsRGFemale[r][d][p] != null) {
                                dualRGFemale[r][d][p] = constrsRGFemale[r][d][p].get(GRB.DoubleAttr.Pi);
                            }
                        }
                    }
                }
            }

            for (int d = 0; d < dualORDO.length; d++) {
                dualORDO[d] = constrsORDO[d].get(GRB.DoubleAttr.Pi);
                for (int s = 0; s < dualORSO[d].length; s++) {
                    dualORSO[d][s] = constrsORSO[d][s].get(GRB.DoubleAttr.Pi);
                }
            }
            for (int d = 0; d < dualORIdle.length; d++) {
                for (int s = 0; s < dualORIdle[d].length; s++) {
                    dualORIdle[d][s] = constrsORI[d][s].get(GRB.DoubleAttr.Pi);
                }
            }
        }
    }

    void updateObj(PricingESolver es, GRBLinExpr obj) {
        obj.addConstant(-dualConvexity[es.getID()]);

        if (newFormulation) {
            for (int r = 0; r < es.getRoomAssign().length; r++) {
                for (int d = 0; es.getRoomAssign()[r] != null
                        && d < es.getRoomAssign()[r].length; d++) {
                    if (es.getRoomAssign()[r][d] != null) {
                        obj.addTerm(-dualAux1[es.getID()][r][d], es.getRoomAssign()[r][d]);
                    }
                }
            }

            if (es.getPatient().getVariablity() > 0) {
                for (int r = 0; r < es.getRoomPAssign().length; r++) {
                    for (int d = 0; es.getRoomPAssign()[r] != null && d < es.getRoomPAssign()[r].length
                            && d < constrAux2[es.getID()][r].length; d++) {
                        if (es.getRoomPAssign()[r][d] != null) {
                            obj.addTerm(-dualAux2[es.getID()][r][d], es.getRoomPAssign()[r][d]);
                        }
                    }
                }
            }

            if (es.getPatient().needSurgery()) {
                for (int d = 0; d < es.getDe().length; d++) {
                    int sd = d + es.getPatient().getEarliestSD();
                    obj.addTerm(-dualAux3[es.getID()][sd],
                            es.getDe()[d]);
                }
            }
        }else {
            updateObjRC(es, obj);
        }
    }

    void updateNetwork(PricingNSolver ns){
        int numRoom = ns.getPatient().getFeasibleRooms().size();
        if (newFormulation) {
            for (int d = 0; d < ns.getGraphs().length; d++) {
                int admissionDay = ns.getPatient().getEarliestAD() + d;
                int e = 0;
                //first layer
                for (int i = 0; i < numRoom; i++, e++) {
                    int roomIndex = ns.getPatient().getFeasibleRooms().get(i);
                    double weight = ns.getGraphsTemp()[d].getEdge(e).getWeight()
                            - dualConvexity[ns.getID()]
                            - dualAux1[ns.getID()][roomIndex][admissionDay];
                    if (ns.getPatient().needSurgery()) {
                        weight -= dualAux3[ns.getID()][ns.getPatient().getEarliestSD() + d];
                    }

                    int dest = i + 1;
                    if (ns.getGraphs()[d].getEdge(e).getDest() != dest) {
                        throw new IllegalMonitorStateException();
                    }
                    ns.getGraphs()[d].getEdge(e).setWeight(weight);
                }

                //los - 1 layer
                for (int dd = 1; dd < ns.getPatient().getRestLOS(); dd++) {
                    int currentDay = dd + admissionDay;
                    for (int i = 0; i < numRoom; i++) {
                        for (int j = 0; j < numRoom; j++, e++) {
                            int roomIndex = ns.getPatient().getFeasibleRooms().get(j);
                            double weight = ns.getGraphsTemp()[d].getEdge(e).getWeight()
                                    - dualAux1[ns.getID()][roomIndex][currentDay];
                            int src = i + (dd - 1) * numRoom + 1;
                            int dest = j + dd * numRoom + 1;
                            if (src != ns.getGraphs()[d].getEdge(e).getSrc()
                                    || dest != ns.getGraphs()[d].getEdge(e).getDest()) {
                                throw new IllegalMonitorStateException();
                            }
                            ns.getGraphs()[d].getEdge(e).setWeight(weight);
                        }
                    }
                }

                // last layer
                if (ns.getPatient().getVariablity() > 0) {
                    int currentDay = ns.getPatient().getRestLOS() + admissionDay;
                    if (currentDay < dualAux2[ns.getID()]
                            [ns.getPatient().getFeasibleRooms().get(0)].length) {
                        for (int i = 0; i < numRoom; i++, e++) {
                            int roomIndex = ns.getPatient().getFeasibleRooms().get(i);
                            double weight = -dualAux2[ns.getID()][roomIndex][currentDay];

                            int src = i + (ns.getPatient().getRestLOS() - 1) * numRoom + 1;
                            if (src != ns.getGraphs()[d].getEdge(e).getSrc()) {
                                throw new IllegalMonitorStateException();
                            }
                            ns.getGraphs()[d].getEdge(e).setWeight(weight);
                        }
                    }
                }
            }
        }else {
            for (int d = 0; d < ns.getGraphs().length; d++) {
                int admissionDay = ns.getPatient().getEarliestAD() + d;
                int e = 0;
                //first layer
                for (int i = 0; i < numRoom; i++, e++) {
                    double weight = ns.getGraphsTemp()[d].getEdge(e).getWeight()
                            - dualConvexity[ns.getID()];
                    int roomIndex = ns.getPatient().getFeasibleRooms().get(i);
                    weight += -dualRC[roomIndex][admissionDay] - dualRO[roomIndex][admissionDay];
                    if (dualRGMale[roomIndex] != null) {
                        if (ns.getPatient().isMale()) {
                            weight += -dualRGMale[roomIndex][admissionDay][ns.getID()];
                        } else {
                            weight += -dualRGFemale[roomIndex][admissionDay][ns.getID()];
                        }
                    }
                    if (admissionDay < dualRI[roomIndex].length) {
                        weight += dualRI[roomIndex][admissionDay];
                    }

                    if (ns.getPatient().needSurgery()) {
                        int sd = ns.getPatient().getEarliestSD() + d;
                        weight += -dualORDO[sd] * ns.getPatient().getSurDur();
                        if (ns.getPatient().isElective()) {
                            weight += -dualORSO[sd][ns.getPatient().getSpec()]
                                    * ns.getPatient().getSurDur();
                            if (sd < dualORIdle.length) {
                                weight += dualORIdle[sd][ns.getPatient().getSpec()] *
                                        ns.getPatient().getSurDur();
                            }
                        }
                    }

                    int dest = i + 1;
                    if (ns.getGraphs()[d].getEdge(e).getDest() != dest) {
                        throw new IllegalMonitorStateException();
                    }
                    ns.getGraphs()[d].getEdge(e).setWeight(weight);
                }

                //los - 1 layer
                for (int dd = 1; dd < ns.getPatient().getRestLOS(); dd++) {
                    int currentDay = dd + admissionDay;
                    for (int i = 0; i < numRoom; i++) {
                        for (int j = 0; j < numRoom; j++, e++) {
                            double weight = ns.getGraphsTemp()[d].getEdge(e).getWeight();
                            int roomIndex = ns.getPatient().getFeasibleRooms().get(j);
                            weight += -dualRC[roomIndex][currentDay] - dualRO[roomIndex][currentDay];
                            if (dualRGMale[roomIndex] != null) {
                                if (ns.getPatient().isMale()) {
                                    weight += -dualRGMale[roomIndex][currentDay][ns.getID()];
                                } else {
                                    weight += -dualRGFemale[roomIndex][currentDay][ns.getID()];
                                }
                            }
                            if (currentDay < dualRI[roomIndex].length) {
                                weight += dualRI[roomIndex][currentDay];
                            }

                            int src = i + (dd - 1) * numRoom + 1;
                            int dest = j + dd * numRoom + 1;
                            if (src != ns.getGraphs()[d].getEdge(e).getSrc()
                                    || dest != ns.getGraphs()[d].getEdge(e).getDest()) {
                                throw new IllegalMonitorStateException();
                            }
                            ns.getGraphs()[d].getEdge(e).setWeight(weight);
                        }
                    }
                }

                // last layer
                if (ns.getPatient().getVariablity() > 0) {
                    int currentDay = ns.getPatient().getRestLOS() + admissionDay;
                    if (currentDay < dualRO[0].length) {
                        for (int i = 0; i < numRoom; i++, e++) {
                            int roomIndex = ns.getPatient().getFeasibleRooms().get(i);
                            double weight = -dualRO[roomIndex][currentDay];

                            int src = i + (ns.getPatient().getRestLOS() - 1) * numRoom + 1;
                            if (src != ns.getGraphs()[d].getEdge(e).getSrc()) {
                                throw new IllegalMonitorStateException();
                            }
                            ns.getGraphs()[d].getEdge(e).setWeight(weight);
                        }
                    }
                }
            }
        }
    }

    double validateReducedCost(SPatient patient, ColumnPatient column) {
        double reducedCost = -dualConvexity[column.getID()];
        if (newFormulation) {
            for (int i = 0; i < patient.getRestLOS(); i++) {
                int d = patient.getEarliestAD() + column.getDelay() + i;
                reducedCost -= dualAux1[column.getID()][column.getRooms()[i]][d];
            }

            if (patient.getVariablity() > 0) {
                int los = patient.getRestLOS();
                int dd = column.getDelay() + patient.getEarliestAD() + los;
                if (dd < constrAux2[column.getID()][column.getRooms()[los - 1]].length) {
                    reducedCost -= dualAux2[column.getID()][column.getRooms()[los - 1]][dd];
                }
            }

            if (patient.needSurgery()) {
                int sd = column.getDelay() + patient.getEarliestSD();
                reducedCost -= dualAux3[column.getID()][sd];
            }
        }else {
            reducedCost += getReducedCostRC(patient, column);
        }
        return reducedCost ;
    }

    GRBConstr[] getConstrs(SPatient patient, ColumnPatient column) {
        constrs.clear(); coeffs.clear();
        constrs.add(constrsConvexity[column.getID()]);
        coeffs.add(1);
        if (newFormulation) {
            for (int i = 0; i < patient.getRestLOS(); i++) {
                int d = column.getDelay() + patient.getEarliestAD() + i;
                constrs.add(constrAux1[column.getID()][column.getRooms()[i]][d]);
                coeffs.add(1);
            }

            if (patient.getVariablity() > 0) {
                int dd = column.getDelay() + patient.getRestLOS() + patient.getEarliestAD();
                if (dd < constrAux2[column.getID()][column.getRooms()[patient.getRestLOS() - 1]]
                        .length) {
                    constrs.add(constrAux2[column.getID()]
                            [column.getRooms()[patient.getRestLOS() - 1]][dd]);
                    coeffs.add(1);
                }
            }

            if (patient.needSurgery()) {
                int sd = column.getDelay() + patient.getEarliestSD();
                constrs.add(constrAux3[column.getID()][sd]);
                coeffs.add(1);
            }
        }else {
            for (int i = 0; i < patient.getRestLOS(); i++) {
                int d = column.getDelay() + patient.getEarliestAD() + i;
                constrs.add(constrsRC[column.getRooms()[i]][d]);
                coeffs.add(1);

                if (d < constrsRI[0].length) {
                    constrs.add(constrsRI[column.getRooms()[i]][d]);
                    coeffs.add(-1);
                }
                constrs.add(constrsRO[column.getRooms()[i]][d]);
                coeffs.add(1);
            }

            if (patient.getVariablity() > 0) {
                int dd = column.getDelay() + patient.getRestLOS() + patient.getEarliestAD();
                if (dd < constrsRO[column.getRooms()[patient.getRestLOS() - 1]].length) {
                    constrs.add(constrsRO[column.getRooms()[patient.getRestLOS() - 1]][dd]);
                    coeffs.add(1);
                }
            }

            if (patient.isMale()) {
                addConstrRG(patient, column, constrsRGMale);
            } else {
                addConstrRG(patient, column, constrsRGFemale);
            }

            if (patient.needSurgery()) {
                int sd = column.getDelay() + patient.getEarliestSD();
                constrs.add(constrsORDO[sd]);
                coeffs.add(patient.getSurDur());

                if (patient.isElective()) {
                    int spec = patient.getSpec();
                    constrs.add(constrsORSO[sd][spec]);
                    coeffs.add(patient.getSurDur());

                    if (sd < constrsORI.length) {
                        constrs.add(constrsORI[sd][spec]);
                        coeffs.add(-patient.getSurDur());
                    }
                }
            }
        }
        return constrs.toArray(new GRBConstr[0]);
    }

    double[] getCoeffs() {
        return coeffs.stream().mapToDouble(i -> i).toArray();
    }

    private void updateObjRC(PricingESolver es, GRBLinExpr obj) {
        for (int r = 0; r < es.getRoomAssign().length; r++) {
            for (int d = 0; es.getRoomAssign()[r] != null && d < es.getRoomAssign()[r].length;
                 d++) {
                if (es.getRoomAssign()[r][d] != null) {
                    obj.addTerm(-dualRC[r][d], es.getRoomAssign()[r][d]);
                }
            }
        }
        updateObjRO(es, obj);
    }

    private void updateObjRO(PricingESolver es, GRBLinExpr obj) {
        for (int r = 0; r < es.getRoomAssign().length; r++) {
            for (int d = 0; es.getRoomAssign()[r] != null &&
                    d < es.getRoomAssign()[r].length && d < constrsRO[r].length; d++) {
                if (es.getRoomAssign()[r][d] != null) {
                    obj.addTerm(-dualRO[r][d], es.getRoomAssign()[r][d]);
                }
            }
        }
        if (es.getPatient().getVariablity() > 0) {
            for (int r = 0; r < es.getRoomPAssign().length; r++) {
                for (int d = 0; es.getRoomPAssign()[r] != null &&
                        d < es.getRoomPAssign()[r].length && d < constrsRO[r].length; d++) {
                    if (es.getRoomPAssign()[r][d] != null) {
                        obj.addTerm(-dualRO[r][d], es.getRoomPAssign()[r][d]);
                    }
                }
            }
        }
        if (es.getPatient().isMale()) {
            updateObjRG(es, obj, dualRGMale);
        } else {
            updateObjRG(es, obj, dualRGFemale);
        }
        updateObjRI(es, obj);
    }

    private void updateObjRG(PricingESolver es, GRBLinExpr obj, double[][][] dualValues) {
        for (int r = 0; r < es.getRoomAssign().length; r++) {
            for (int d = 0; constrsRGMale[r] != null && es.getRoomAssign()[r] != null
                    && d < es.getRoomAssign()[r].length && d < constrsRGMale[r].length; d++) {
                if (es.getRoomAssign()[r][d] != null) {
                    obj.addTerm(-dualValues[r][d][es.getID()], es.getRoomAssign()[r][d]);
                }
            }
        }
    }

    private void updateObjRI(PricingESolver es, GRBLinExpr obj) {
        for (int r = 0; r < es.getRoomAssign().length; r++) {
            for (int d = 0; es.getRoomAssign()[r] != null &&
                    d < es.getRoomAssign()[r].length && d < constrsRI[r].length; d++) {
                if (es.getRoomAssign()[r][d] != null) {
                    obj.addTerm(dualRI[r][d], es.getRoomAssign()[r][d]);
                }
            }
        }
        updateObjORDO(es, obj);
    }

    private void updateObjORDO(PricingESolver es, GRBLinExpr obj) {
        if (es.getPatient().needSurgery()) {
            for (int d = 0; d < es.getDe().length; d++) {
                int sd = d + es.getPatient().getEarliestSD();
                if (!es.getPatient().isRegistered() && d>0) {
                    throw new IllegalMonitorStateException("the admitted patient is delayed!");
                }
                obj.addTerm(-dualORDO[sd] * es.getPatient().getSurDur(),
                        es.getDe()[d]);
            }
        }
        updateObjORSO(es, obj);
    }

    private void updateObjORSO(PricingESolver es, GRBLinExpr obj) {
        if (es.getPatient().isElective() && es.getPatient().needSurgery()) {
            if (es.getPatient().isRegistered()) {
                for (int d = 0; d < es.getDe().length; d++) {
                    int sd = d + es.getPatient().getEarliestSD();
                    obj.addTerm(-dualORSO[sd][es.getPatient().getSpec()] *
                            es.getPatient().getSurDur(), es.getDe()[d]);
                }
            } else {
                obj.addConstant(-dualORSO[es.getPatient().getSD()][es.getPatient().getSpec()] *
                        es.getPatient().getSurDur());
            }
        }
        updateObjORI(es, obj);
    }

    private void updateObjORI(PricingESolver es, GRBLinExpr obj) {
        if (es.getPatient().isElective() && es.getPatient().needSurgery()) {
            for (int d = 0; d < es.getDe().length; d++) {
                int sd = d + es.getPatient().getEarliestSD();
                if (sd < dualORIdle.length) {
                    obj.addTerm(dualORIdle[sd][es.getPatient().getSpec()] *
                            es.getPatient().getSurDur(), es.getDe()[d]);
                }
            }
        }
    }

    private double getReducedCostRC(SPatient patient, ColumnPatient column) {
        double price = 0;
        for (int i = 0; i < patient.getRestLOS(); i++) {
            int d = patient.getEarliestAD() + column.getDelay() + i;
            price -= dualRC[column.getRooms()[i]][d];
        }
        return price + getReducedCostRO(patient, column);
    }

    private double getReducedCostRO(SPatient patient, ColumnPatient column) {
        double price = 0;
        int los = patient.getRestLOS();
        for (int i = 0; i < los; i++) {
            int d = i + column.getDelay() + patient.getEarliestAD();
            price -= dualRO[column.getRooms()[i]][d];
        }
        int dd = column.getDelay() + patient.getEarliestAD() + los;
        if (patient.getVariablity() > 0 && dd < dualRO[column.getRooms()[los - 1]].length) {
            price -= dualRO[column.getRooms()[los - 1]][dd];
        }
        return price + getReducedCostRG(patient, column);
    }

    private double getReducedCostRG(SPatient patient, ColumnPatient column) {
        double price = 0;
        for (int i = 0; i < patient.getRestLOS(); i++) {
            int d = i + column.getDelay() + patient.getEarliestAD();
            if (constrsRGMale[column.getRooms()[i]] != null) {
                if (patient.isMale()) {
                    price -= dualRGMale[column.getRooms()[i]][d][column.getID()];
                } else {
                    price -= dualRGFemale[column.getRooms()[i]][d][column.getID()];
                }
            }
        }
        return price + getReducedCostRI(patient, column);
    }

    private double getReducedCostRI(SPatient patient, ColumnPatient column) {
        double price = 0;
        for (int i = 0; i < patient.getRestLOS(); i++) {
            int d = patient.getEarliestAD() + column.getDelay() + i;
            if (d < constrsRI[0].length) {
                price += dualRI[column.getRooms()[i]][d];
            }
        }
        return price + getReducedCostORDO(patient, column);
    }

    private double getReducedCostORDO(SPatient patient, ColumnPatient column) {
        double price = 0;
        if (patient.needSurgery()) {
            int sd = column.getDelay() + patient.getEarliestSD();
            price -= dualORDO[sd] * patient.getSurDur();
        }
        return price + getReducedCostORSO(patient, column);
    }

    private double getReducedCostORSO(SPatient patient, ColumnPatient column) {
        double price = 0;
        if (patient.needSurgery() && patient.isElective()) {
            int sd = column.getDelay() + patient.getEarliestSD();
            price -= dualORSO[sd][patient.getSpec()] * patient.getSurDur();
        }
        return price + getReducedCostORI(patient, column);
    }

    private double getReducedCostORI(SPatient patient, ColumnPatient column) {
        double price = 0;
        if (patient.needSurgery() && patient.isElective()) {
            int sd = column.getDelay() + patient.getEarliestSD();
            if (sd < constrsORI.length) {
                price += dualORIdle[sd][patient.getSpec()] * patient.getSurDur();
            }
        }
        return price;
    }

    private void addConstrRG(SPatient patient, ColumnPatient column, GRBConstr[][][] constrsRG) {
        for (int i = 0; i < patient.getRestLOS(); i++) {
            int d = column.getDelay() + patient.getEarliestAD() + i;
            if (constrsRGMale[column.getRooms()[i]] != null) {
                constrs.add(constrsRG[column.getRooms()[i]][d][column.getID()]);
                coeffs.add(1);
            }
        }
    }

    GRBConstr[] getConstrsConvexity() {
        return constrsConvexity;
    }

    GRBConstr[][] getConstrsRC() {
        return constrsRC;
    }

    GRBConstr[][] getConstrsRO() {
        return constrsRO;
    }

    GRBConstr[][][] getConstrsRGMale() {
        return constrsRGMale;
    }

    GRBConstr[][][] getConstrsRGFemale() {
        return constrsRGFemale;
    }

    GRBConstr[] getConstrsORDO() {
        return constrsORDO;
    }

    GRBConstr[][] getConstrsORSO() {
        return constrsORSO;
    }

    GRBConstr[][] getConstrsORI() {
        return constrsORI;
    }

    GRBConstr[][] getConstrsRI() {
        return constrsRI;
    }

    GRBConstr[][][] getConstrAux1() {
        return constrAux1;
    }

    GRBConstr[][][] getConstrAux2() {
        return constrAux2;
    }

    GRBConstr[][] getConstrAux3() {
        return constrAux3;
    }

}
