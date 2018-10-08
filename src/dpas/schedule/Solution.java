/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.Params;
import util.Util;

public class Solution {
    private ScheduleResult scheduleResult;
    private double runtime;
    private int[] roomAssignments;
    private int[][] rooms;
    private int[] admissionDelays;
    private int objectiveValue;
    private int[] costContainer;
    private int violations;
    private int[] OROORTO;
    private int[] ORUORTU;
    private int[] roomCostContainer;
    private float roomOccupancyRatio;
    private float OROccupancyRatio;
    //private schedule scheduleResult;

    public Solution(ScheduleResult scheduleResult) {
        this.scheduleResult = scheduleResult;
        initialize();
    }

    public Solution(int[] admissionDelays, int[] roomAssignments) {
        this.admissionDelays = admissionDelays;
        this.roomAssignments = roomAssignments;
        initialize();
    }

    public Solution(int[] admissionDelays, int[][] rooms) {
        this.admissionDelays = admissionDelays;
        this.rooms = rooms;
        initialize();
    }

    public Solution(Solution solution) {
        objectiveValue = solution.getObjectiveValue();
        costContainer = solution.getCostContainer().clone();
        OROORTO = solution.getOROORTO().clone();
        ORUORTU = solution.getORUORTU().clone();
        roomCostContainer = solution.getRoomCostContainer().clone();
        roomAssignments = solution.getRoomID().clone();
        admissionDelays = solution.getAdDelay().clone();
        //scheduleResult = solution.getScheduleResult();
    }

    private void initialize() {
        costContainer = new int[10];
        OROORTO = new int[2];
        ORUORTU = new int[2];
        roomCostContainer = new int[4];
    }

/*
    public Solution() {
        // 0: RoomCost; 1: GenderCost; 2: TransferCost; 3: DelayCost; 4: OverCrowdRisk; 5:
        IdleRoomCost; 6: IdleORCost; 7: OROverTimeCost; 8: OverRoomTimeCost; 9: ORUORTU
        costContainer = new int[10];
        OROORTO = new int[2];
        ORUORTU = new int[2];
        roomCostContainer = new int[4]; // 0: FeatureCost; 1: PreferenceCost: 2: DeptCost: 3:
        FixedGenderCost;
        //this.scheduleResult = scheduleResult;

    }
*/
//    public void setRoomAssignments(int[] roomAssignments) {
//        this.roomAssignments = roomAssignments;
//    }

    public int getRoomID(int index) {
        return roomAssignments[index];
    }

    private int[] getRoomID() {
        return roomAssignments;
    }

    /*
        public int[] getRoomID() {
            return roomAssignments;
        }
    */
    public void setRoomAssignments(int index, int roomIndex) {
        roomAssignments[index] = roomIndex;
    }

    //    public void setAdmissionDelays(int[] admissionDelays) {
//        this.admissionDelays = admissionDelays;
//    }
    public void setAdmissionDelays(int index, int delay) {
        admissionDelays[index] = delay;
    }

    public int getAdDelay(int patientIndex) {
        return admissionDelays[patientIndex];
    }

    private int[] getAdDelay() {
        return admissionDelays;
    }

    public void setRoomPropertyCost(int roomPropertyCost) {
        roomCostContainer[0] = roomPropertyCost;
    }

    public void setRoomPreferenceCost(int roomPreferenceCost) {
        roomCostContainer[1] = roomPreferenceCost;
    }

    public void setSpecialismCost(int specialismCost) {
        roomCostContainer[2] = specialismCost;
    }

    public void setFixedGenderCost(int fixedGenderCost) {
        roomCostContainer[3] = fixedGenderCost;
    }

    public void updateRoomCosts() {
        int roomCost = 0;
        for (Integer costs : roomCostContainer) {
            roomCost += costs;
        }
        costContainer[0] = roomCost;
    }

    public void setRoomCost(int roomCost) {
        costContainer[0] = roomCost;
    }

    public int getRoomCost() {
        return costContainer[0];
    }

    public void updateRoomCost(int delta) {
        setRoomCost(delta + getRoomCost());
    }

    public void setRGCost(int RGCost) {
        costContainer[1] = RGCost;
    }

    public int getRGCost() {
        return costContainer[1];
    }

    public void updateRGCost(int delta) {
        setRGCost(getRGCost() + delta);
    }

    public void setTrCost(int TrCost) {
        costContainer[2] = TrCost;
    }

    public int getTrCost() {
        return costContainer[2];
    }

    public void updateTrCost(int delta) {
        setTrCost(getTrCost() + delta);
    }

    public void setDeCost(int DeCost) {
        costContainer[3] = DeCost;
    }

    public int getDeCost() {
        return costContainer[3];
    }

    public void updateDeCost(int delta) {
        setDeCost(getDeCost() + delta);
    }

    public void setRiCost(int RiCost) {
        costContainer[4] = RiCost;
    }

    public int getRiCost() {
        return costContainer[4];
    }

    public void updateRiCost(int delta) {
        setRiCost(getRiCost() + delta);
    }

    public void setIRCost(int IRCost) {
        costContainer[5] = IRCost;
    }

    public int getIRCost() {
        return costContainer[5];
    }

    public void updateIRCost(int delta) {
        setIRCost(getIRCost() + delta);
    }

    public void setORIdleTime(int ORIdleTime) {
        costContainer[6] = ORIdleTime;
    }

    public int getORIdleTime() {
        return costContainer[6];
    }

    public void updateORIdleTime(int delta) {
        setORIdleTime(getORIdleTime() + delta);
    }

    public void setOROORTO(int ORO, int ORTO) {
        OROORTO[0] = ORO;
        OROORTO[1] = ORTO;
        costContainer[7] = (OROORTO[0] + OROORTO[1]);
    }

    public void setOROORTO(int OROORTO) {
        costContainer[7] = OROORTO;
    }

    private int[] getOROORTO() {
        return OROORTO;
    }

    public void updateOROORTO(int delta1, int delta2) {
        setOROORTO(getORO() + delta1, getORTO() + delta2);
    }

    public int getORO() {
        return OROORTO[0];
    }

    public int getORTO() {
        return OROORTO[1];
    }

    public void setOverRoomCost(int overRoomCost) {
        costContainer[8] = overRoomCost;
    }

    public int getOverRoomCost() {
        return costContainer[8];
    }

    public void updateOverRoomCost(int delta) {
        setOverRoomCost(getOverRoomCost() + delta);
    }

    public void setORUORTU(int ORU, int ORTU) {
        ORUORTU[0] = ORU;
        ORUORTU[1] = ORTU;
        costContainer[9] = ORU + ORTU;
    }

    public void setORUORTU(int oruortu) {
        costContainer[9] = oruortu;
    }

    private int[] getORUORTU() {
        return ORUORTU;
    }

    public int getORU() {
        return ORUORTU[0];
    }

    public int getORTU() {
        return ORUORTU[1];
    }

    public void updateORUORTU(int delta1, int delta2) {
        setORUORTU(getORU() + delta1, getORTU() + delta2);
    }

    public void updateObjectiveValue(boolean validate) {
        int obj = 0;
        for (Integer cost : costContainer) {
            obj += cost;
        }
        if (validate){
            Util.isUnequal(objectiveValue,obj,false);
        }else {
            objectiveValue = obj;
        }
    }

    public void setObjectiveValue(int obj) {
        objectiveValue = obj;
        updateObjectiveValue(true);
    }

    public int getObjectiveValue() {
        return objectiveValue;
    }

    //public schedule getScheduleResult() {
    //  return scheduleResult;
    //}

    public int[] getCostContainer() {
        return costContainer;
    }

    public int[] getRoomCostContainer() {
        return roomCostContainer;
    }

    public int getViolations() {
        return violations;
    }

    public void setViolations() {
        violations += costContainer[8];
        violations += costContainer[9];
        costContainer[8] = 0;
        costContainer[9] = 0;
        violations /= Params.HARD_CONSTRAINTS;
    }

    public void setViolations(int violations) {
        this.violations = violations;
    }

    public void setRuntime(double runtime) {
        this.runtime = runtime;
    }

    public ScheduleResult getScheduleResult() {
        return scheduleResult;
    }

    public int[][] getRooms() {
        return rooms;
    }

    public double getRuntime() {
        return runtime;
    }

    public float getRoomOccupancyRatio() {
        return roomOccupancyRatio;
    }

    public void setRoomOccupancyRatio(float roomOccupancyRatio) {
        this.roomOccupancyRatio = roomOccupancyRatio;
    }

    public float getOROccupancyRatio() {
        return OROccupancyRatio;
    }

    public void setOROccupancyRatio(float OROccupancyRatio) {
        this.OROccupancyRatio = OROccupancyRatio;
    }
}
