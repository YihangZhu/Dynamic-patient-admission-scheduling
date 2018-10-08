/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm;

import dpas.Params;
import dpas.instance.GenderPolicy;
import dpas.instance.Instance;
import dpas.instance.Patient;
import dpas.instance.Room;
import dpas.schedule.Problem;
import dpas.schedule.SPatient;
import dpas.schedule.ScheduleResult;
import dpas.schedule.Solution;

/**
 * Created by zhuyi on 2/9/2017.
 */
public class FinalComputing {
    private ScheduleResult scheduleResult;
    private Instance instance;
    private Solution solution;

    public FinalComputing(Problem problem) {
        instance = problem.getInstance();
        scheduleResult = problem.getScheduleResult();
        solution = new Solution(scheduleResult);
    }

    public FinalComputing(Solution solution, Instance instance) {
        this.instance = instance;
        this.scheduleResult = solution.getScheduleResult();
        this.solution = solution;
    }

    public Solution start(boolean validate) {
        assign();
        patientCostsCal();
        roomCostsCal();
        ORCostsCal();
        roomOccupancyRatioCal();
        OROccupancyRatioCal();
        solution.setViolations();
        solution.updateObjectiveValue(validate);
        return solution;
    }

    private void assign() {
        scheduleResult.initializeOccupancy();
        for (int p = 0; p < instance.getPatientNum(); p++) {
            SPatient sPatient = scheduleResult.getSPatients(p);
            sPatient.setAdmissionDay();
            int roomIndex = -1, day = 0;
            for (; day < sPatient.getTotalLOS(); day++) {
                roomIndex = sPatient.getRooms(day);
                scheduleResult.increaseRoomOccupancy(day + sPatient.getAdmissionDay(),
                        roomIndex, sPatient.isMale());
            }
            if (sPatient.getVariablity() > 0) {
//                if (Params.transfer){
//                    scheduleResult.increaseOverRiskOccupancy(sPatient.getDischargeDay(),
// sPatient.getRooms(day));
//                }else {
                scheduleResult.increaseOverRiskOccupancy(sPatient.getDischargeDay(), roomIndex);
//                }
            }
            if (instance.getPatients()[p].needSurgery()) {
                int surgeryDay = sPatient.getAdmissionDay() + sPatient.getPreSurgeryDay();
                int surgeryDuration = sPatient.getSurDur();
                scheduleResult.incORDayOccupancy(surgeryDay, surgeryDuration);
                if (sPatient.isElective()) {
                    int specialism = sPatient.getSpec();
                    scheduleResult.incOROccupancy(surgeryDay, specialism, surgeryDuration);
                }
            }
        }
    }

    private void patientCostsCal() {
        int TrCost = 0;
        int DeCost = 0;
        int fixedGenderCost = 0;
        int preferenceCost = 0;
        int featureCost = 0;
        int specialismCost = 0;
        for (Patient patient : instance.getPatients()) {
            SPatient sPatient = scheduleResult.getSPatients(patient.getNumber());
            if (sPatient != null) {
                sPatient.setAdmissionDelay(sPatient.getDischargeDay() - patient.getDateDischarge());
                DeCost += sPatient.getAdmissionDelay();
                sPatient.setAdmissionDay();
                for (int i = 0; i < patient.getLOS(); i++) {
                    int roomIndex = sPatient.getRooms(i);
                    int preRoomIndex;
                    if (i == 0) {
                        if (patient.getRoom() != null) {
                            preRoomIndex = patient.getRoom().getNumber();
                            if (preRoomIndex != roomIndex) {
                                TrCost++;
                            }
                        }
                    } else {
                        preRoomIndex = sPatient.getRooms(i - 1);
                        if (preRoomIndex != roomIndex) {
                            TrCost++;
                        }
                    }
                    // 0: RoomCost; 1: FeatureCost; 2: PreferenceCost: 3: DeptCost: 4:
                    // FixedGenderCost;
                    fixedGenderCost += sPatient.getRoomFixedGenderCost(roomIndex);
                    preferenceCost += sPatient.getRoomPreferCost(roomIndex);
                    featureCost += sPatient.getRoomFeatureCost(roomIndex);
                    specialismCost += sPatient.getRoomSpecCost(roomIndex);
                }
            }
        }
        solution.setFixedGenderCost(fixedGenderCost * Params.GENDER_POLICY);
        solution.setRoomPreferenceCost(preferenceCost * Params.ROOM_PREFERENCE);
        solution.setRoomPropertyCost(featureCost * Params.ROOM_PROPERTY);
        solution.setSpecialismCost(specialismCost * Params.SPECIALISM);
        solution.updateRoomCosts();
        solution.setTrCost(TrCost * Params.TRANSFER);
        solution.setDeCost(DeCost * Params.DELAY);
    }

    private void roomCostsCal() {
        int ORCosts = 0;
        int RiCosts = 0;
        int RGCosts = 0;
        int IRCosts = -instance.redundantRoomCap();
        for (int r = 0; r < instance.getRoomNum(); r++) {
            Room room = instance.getRooms(r);
            // for other room related cost.
            for (int day = 1; day < instance.getNumDays(); day++) {
                RiCosts += Math.max(0, (scheduleResult.getPotentialOccupancy(day, r) - room
                        .getCapacity()));
            }

            for (int day = 0; day < instance.getNumDays(); day++) {
                if (room.getGenderPolicy().equals(GenderPolicy.SameGender)) {
                    if (Params.genderPolicyConstr.equals("1") || Params.genderPolicyConstr.equals
                            ("2") || Params.genderPolicyConstr.equals("3")) {
                        RGCosts += scheduleResult.minorGenderOccupancy(day, room.getNumber());
                    } else {
                        if (scheduleResult.minorGenderOccupancy(day, room.getNumber()) > 0) {
                            RGCosts++;
                        }
                    }
                }
                int usedCapacity = scheduleResult.usedRoomCapacity(day, r);
                if (usedCapacity < room.getCapacity()) {
                    if (day < instance.getPlanningHorizon()) {
                        IRCosts += room.getCapacity() - usedCapacity;
                    }
                } else if (usedCapacity > room.getCapacity()) {
                    ORCosts += usedCapacity - room.getCapacity();
                }
            }
        }

        solution.setRGCost(RGCosts * Params.GENDER_POLICY);
        solution.setIRCost(IRCosts * Params.IDLE_ROOM_CAPACITY);
        solution.setOverRoomCost(ORCosts * Params.HARD_CONSTRAINTS);
        solution.setRiCost(RiCosts * Params.OVERCROWD_RISK);
    }

    private int admittedOvertime(int day, int spec) {
        return instance.slotsNum(day, spec) * Params.ADMITTED_OVERTIME;
    }

    private void ORCostsCal() {
        int[] costs = new int[5]; //  0: ORO, 1:ORU, 2:ORTO,  3:ORTU, 4: idleRoom
        costs[4] = -instance.redundantORCap();
        for (int day = 0; day < instance.getNumDays(); day++) {
            for (int specialism = 0; specialism < instance.getSpecialisms().size(); specialism++) {
                int ORUsedCapacity = scheduleResult.usedORCapacity(day, specialism);
                int ORCapacity = instance.getORSpecCapacity(day, specialism);

                if (day < instance.getPlanningHorizon()) {
                    if (ORUsedCapacity < ORCapacity) {
                        costs[4] += ORCapacity - ORUsedCapacity;
                    }
                }
                if (ORUsedCapacity > ORCapacity) {
                    int admittedOvertime = admittedOvertime(day, specialism);
                    if (ORUsedCapacity > ORCapacity + admittedOvertime) {
                        costs[0] += admittedOvertime;
                        costs[1] += ORUsedCapacity - ORCapacity - admittedOvertime;
                    } else {
                        costs[0] += ORUsedCapacity - ORCapacity;
                    }
                }
            }
            int ORCapacityTotal = instance.ORDayCapacity(day);
            int ORDayUsedCapacity = scheduleResult.usedORDayCapacity(day);
            if (ORDayUsedCapacity > ORCapacityTotal + Params.ADMITTED_TOTAL_OVERTIME) {
                costs[2] += Params.ADMITTED_TOTAL_OVERTIME;
                costs[3] += ORDayUsedCapacity - ORCapacityTotal - Params.ADMITTED_TOTAL_OVERTIME;
            } else {
                costs[2] += Math.max(0, ORDayUsedCapacity - ORCapacityTotal);
            }
        }
        solution.setORIdleTime(costs[4] * Params.IDLE_OPERATING_ROOM);
        solution.setOROORTO(costs[0] * Params.OVERTIME, costs[2] * Params.OVERTIME);
        solution.setORUORTU(costs[1] * Params.HARD_CONSTRAINTS, costs[3] * Params.HARD_CONSTRAINTS);
    }

    private void roomOccupancyRatioCal() {
        float ratio = 0;
        float count = 0;
//        System.out.println("Room Occupancy:");
        for (int d = 0; d < instance.getPlanningHorizon(); d++) {
            for (int r = 0; r < instance.getRoomNum(); r++) {
                double temp = (double) scheduleResult.usedRoomCapacity(d, r) / (double)instance.getRoomCapacity(r);
                ratio += temp;
//                System.out.println(temp);
                count++;
            }
        }
        ratio /= count;
        solution.setRoomOccupancyRatio(ratio);
    }

    private void OROccupancyRatioCal() {
        float ratio = 0;
        float count = 0;
//        System.out.println("OR Occupancy:");
        for (int d = 0; d < instance.getPlanningHorizon(); d++) {
            if (instance.getORDayCapacity(d) > 0) {
                double temp = (double)scheduleResult.usedORDayCapacity(d) / (double)instance.getORDayCapacity(d);
                ratio += temp;
//                System.out.println(temp);
                count++;
            }
        }
        ratio /= count;
        solution.setOROccupancyRatio(ratio);
    }
}
