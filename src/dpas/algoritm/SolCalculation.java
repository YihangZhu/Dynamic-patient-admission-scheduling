/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm;


import dpas.Params;
import dpas.instance.Instance;
import dpas.instance.Status;
import dpas.schedule.*;
import util.Util;
import java.util.ArrayList;

public class SolCalculation {
    private Instance instance;
    private Solution solution;
    private Problem problem;
    private int currentDay;
    private ArrayList<SPatient> registeredPatients;
    private ValuesStorage vs;

    public SolCalculation(Problem problem,
                          ValuesStorage vs) {
        this.problem = problem;
        this.instance = problem.getInstance();
        this.registeredPatients = problem.getPatientSet();
        this.vs = vs;
    }

    public void objectiveValueCalculation(Solution solution, boolean display) {
        currentDay = problem.getCurrent();
        this.solution = solution;
        patientRelatedCost();
        ORRelatedCost();
        roomRelatedCost();
        solution.updateObjectiveValue(false);
        key1 = true;

        if (display) {
            System.out.println("\tObjVal:\t" + solution.getObjectiveValue());
            for (int i = 0; i < solution.getCostContainer().length; i++) {
                System.out.print(Params.getCostNames()[i] + ":\t" + solution.getCostContainer()
                        [i] + "\t");
            }
            System.out.print("\n");
        }
    }

    public void assign(Solution solution) {   // set the data basis for the current day searching
        for (int p = 0; p < registeredPatients.size(); p++) {
            SPatient sPatient = registeredPatients.get(p);
            if (sPatient.getStatus().equals(Status.Registered)) {
                int delay = solution.getAdDelay(p);
                sPatient.setDelayTemp(delay);
            }

            int roomIndex = solution.getRoomID(p);
            sPatient.setRoomIndexTemp(roomIndex);
            vs.incRoomOccupancy(roomIndex, sPatient);

            if (sPatient.needSurgery()) {
                int surgeryDay = sPatient.getSD();
                int surgeryDuration = sPatient.getSurDur();
                vs.getORStorage(surgeryDay).incORDayOccupancy(surgeryDuration);
                if (sPatient.isElective()) {
                    vs.getORStorage(surgeryDay).getSpecOR(sPatient.getSpec()).incOccupancy
                            (surgeryDuration);
                }
            }
        }
        key1 = false;
    }

    private boolean key1 = false;

    private void patientRelatedCost() {
        int TrCosts = 0, roomCosts = 0, DeCosts = 0;
        //int [] costs = new int[3]; // 0:TrCosts, 1:roomRCosts, 2: DeCosts
        for (SPatient sPatient : registeredPatients) {
            //int patientIndex = sPatient.getNumber();
            int roomIndex = sPatient.getRoomIndexTemp();
            if (sPatient.getPreRoomIndex() != -1) {
                if (roomIndex != sPatient.getPreRoomIndex()) {
                    int TrCost = 1;
                    TrCosts += TrCost;
                }
            } else {
                assert sPatient.getStatus().equals(Status.Registered);
                DeCosts += sPatient.getDelayPriority();
            }
            int roomCost = sPatient.getRoomCost(roomIndex) * (sPatient.getDischargeDay() -
                    sPatient.getAdmissionDay());// sara use full LOS
            roomCosts += roomCost;
        }
        if (key1) {
            if (Util.isUnequal(TrCosts * Params.TRANSFER, solution.getTrCost(), false)) {
                throw new IllegalMonitorStateException("room transfer cost is not correct");
            }
            if (Util.isUnequal(roomCosts, solution.getRoomCost(), false)) {
                throw new IllegalMonitorStateException("room cost is not correct!");
            }
            if (Util.isUnequal(DeCosts * Params.DELAY, solution.getDeCost(), false)) {
                throw new IllegalMonitorStateException("delay cost is not correct!");
            }
        }
        solution.setTrCost(TrCosts * Params.TRANSFER);
        solution.setRoomCost(roomCosts);
        solution.setDeCost(DeCosts * Params.DELAY);
    }

    private void roomRelatedCost() {
        int idleRC = 0, overRC = 0, roomGender = 0, pOverRC = 0;
        for (int day = currentDay; day < instance.getNumDays(); day++) {
            for (int r = 0; r < instance.getRoomNum(); r++) {
                SRoom room = vs.getRoomStorage(r, day);
                if (day < instance.getPlanningHorizon()) {
                    idleRC += room.getIdleRC();
                }
                overRC += room.getOverRC();
                if (instance.roomForSG(r)) {
                    roomGender += room.getGenderPolicyConflict();
                }
                pOverRC += room.getPOverRC();
            }
        }
//        idleRC -= instance.redundantRoomCap();
        if (key1) {
            if (Util.isUnequal(
                    idleRC * Params.IDLE_ROOM_CAPACITY, solution.getIRCost(), false)) {
                throw new IllegalMonitorStateException("idle room cost is not correct!");
            }
            if (Util.isUnequal(
                    overRC * Params.HARD_CONSTRAINTS, solution.getOverRoomCost(), false)) {
                throw new IllegalMonitorStateException("over room capacity is not correct!");
            }
            if (Util.isUnequal(
                    roomGender * Params.GENDER_POLICY, solution.getRGCost(), false)) {
                throw new IllegalMonitorStateException("room gender cost is not correct!");
            }
            if (Util.isUnequal(
                    pOverRC * Params.OVERCROWD_RISK, solution.getRiCost(), false)) {
                throw new IllegalMonitorStateException("room over crowd risk cost is not correct!");
            }
        }

        solution.setIRCost(idleRC * Params.IDLE_ROOM_CAPACITY);
        solution.setOverRoomCost(overRC * Params.HARD_CONSTRAINTS);
        solution.setRGCost(roomGender * Params.GENDER_POLICY);
        solution.setRiCost(pOverRC * Params.OVERCROWD_RISK);
    }

    private void ORRelatedCost() {
        int OROCost = 0, ORUCost = 0, ORTOCost = 0, ORTUCost = 0, idleORCost = 0;
        for (int day = currentDay; day < instance.getNumDays(); day++) {
            SOR ORTemp = vs.getORStorage(day);
            for (int specialism = 0; specialism < instance.getSpecNum(); specialism++) {
                OROCost += ORTemp.getSpecOR(specialism).getOverOR();
                ORUCost += ORTemp.getSpecOR(specialism).getOverORU();
            }
            ORTUCost += ORTemp.getOverORTU();
            ORTOCost += ORTemp.getOverORT();
            if (day < instance.getPlanningHorizon()) {
                idleORCost += ORTemp.getIOS();
            }
        }
        if (key1) {
            if (Util.isUnequal(
                    OROCost * Params.OVERTIME, solution.getORO(), false)) {
                throw new IllegalMonitorStateException("OR specialism overtime cost is not " +
                        "correct!");
            }
            if (Util.isUnequal(
                    ORTOCost * Params.OVERTIME, solution.getORTO(), false)) {
                throw new IllegalMonitorStateException("OR daily overtime cost is not correct!");
            }
            if (Util.isUnequal(
                    ORUCost * Params.HARD_CONSTRAINTS, solution.getORU(), false)) {
                throw new IllegalMonitorStateException("OR specialism capacity overtime cost is " +
                        "not correct!");
            }
            if (Util.isUnequal(
                    ORTUCost * Params.HARD_CONSTRAINTS, solution.getORTU(), false)) {
                throw new IllegalMonitorStateException("OR daily capacity overtime cost is not " +
                        "correct!");
            }
            if (Util.isUnequal(
                    idleORCost * Params.IDLE_OPERATING_ROOM, solution.getORIdleTime(), false)) {
                throw new IllegalMonitorStateException("OR daily capacity idle cost is not " +
                        "correct!");
            }
        }
        solution.setOROORTO(OROCost * Params.OVERTIME, ORTOCost * Params.OVERTIME);
        solution.setORUORTU(ORUCost * Params.HARD_CONSTRAINTS, ORTUCost * Params.HARD_CONSTRAINTS);
        solution.setORIdleTime(idleORCost * Params.IDLE_OPERATING_ROOM);
    }
}





