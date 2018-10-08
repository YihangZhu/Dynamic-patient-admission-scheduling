/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls;


import algorithm.SimulatedAnnealing;
import dpas.Params;
import dpas.instance.Instance;
import dpas.schedule.*;
import dpas.schedule.buffer.BufferPRoom;


/**
 * Created by zhuyi on 2/8/2017.
 */
public class EvaOperations {
    private ValuesStorage vs;
    private ValuesBuffer vb;
    private Instance instance;

    public EvaOperations(ValuesStorage vs, ValuesBuffer vb, Instance instance) {
        this.vs = vs;
        this.vb = vb;
        this.instance = instance;
    }

    private SOR addInvolvedORD(int surgeryDay, int[] ORCosts) {
        SOR ORTemp = vs.getORStorage(surgeryDay);
        ORCosts[2] -= ORTemp.getOverORT();
        ORCosts[3] -= ORTemp.getOverORTU();
        if (surgeryDay < instance.getPlanningHorizon()) {
            ORCosts[4] -= ORTemp.getIOS();
        }
        SOR buf = new SOR(ORTemp);
        vb.addInvolvedOR(buf);
        return buf;
    }

    private SSpecOR addInvolvedORSpec(SOR buf, int surgeryDay, int spec, int[] ORCosts) {

        SSpecOR ORSpecTemp = vs.getORStorage(surgeryDay).getSpecOR(spec);
        ORCosts[0] -= ORSpecTemp.getOverOR();
        ORCosts[1] -= ORSpecTemp.getOverORU();
        SSpecOR bufSpec = new SSpecOR(ORSpecTemp);
        buf.addSpecOR(bufSpec);
        return bufSpec;
    }

    public void addInvolvedOR(boolean remove, SPatient sPatient, int surgeryDay, int[] ORCosts) {
        //ORO, ORU, ORTO, ORTU, idleOR
        int surgeryDuration = sPatient.getSurDur();
        SOR buf = vb.getORStorage(surgeryDay);
        if (buf == null) {
            buf = addInvolvedORD(surgeryDay, ORCosts);
        }
        if (remove) {
            buf.decORDayOccupancy(surgeryDuration);
        } else {
            buf.incORDayOccupancy(surgeryDuration);
        }

        if (sPatient.isElective()) {
            int spec = sPatient.getSpec();
            SSpecOR bufSpec = buf.getSpecOR(spec);
            if (bufSpec == null) {
                bufSpec = addInvolvedORSpec(buf, surgeryDay, spec, ORCosts);
            }
            if (remove) {
                bufSpec.decOccupancy(surgeryDuration);
            } else {
                bufSpec.incOccupancy(surgeryDuration);
            }
        }
    }

    public void ORValuesEvaluation(int[] ORCosts) {
        for (SOR buf : vb.getInvolvedOR()) {
            ORCosts[0] += buf.getORO();
            ORCosts[1] += buf.getORU();
            ORCosts[2] += buf.getOverORT();
            ORCosts[3] += buf.getOverORTU();
            if (buf.getDay() < instance.getPlanningHorizon()) {
                ORCosts[4] += buf.getIOS();
            }
        }
    }

    public void addInvolvedRoom(boolean remove, SPatient patient, int roomIndex, int day, int[]
            IR_OR_RG_RI) {
        SRoomValues buf = vb.getRoomStorage(roomIndex, day);
        if (buf == null) {
            buf = addInvolvedRoom(roomIndex, day, IR_OR_RG_RI);
        }
        if (remove) {
            buf.removePatient(patient);
        } else {
            buf.addPatient(patient);
        }
    }

    private SRoom addInvolvedRoom(int roomIndex, int day, int[] IR_OR_RG_RI) {
        SRoom room = vs.getRoomStorage(roomIndex, day);
        if (day < instance.getPlanningHorizon()) {
            IR_OR_RG_RI[0] -= room.getIdleRC();
        }
        IR_OR_RG_RI[1] -= room.getOverRC();
        IR_OR_RG_RI[2] -= room.getGenderPolicyConflict();
        IR_OR_RG_RI[3] -= room.getPOverRC();
        SRoom buf = new SRoom(room);
        vb.addInvolvedRoom(buf);
        return buf;
    }

    public void addVariabilityDay(boolean remove, int roomIndex, int day, int[] IR_OR_RG_Ri) {
        if (day < instance.getNumDays()) {
            SRoomValues buf = vb.getRoomStorage(roomIndex, day);
            if (buf == null) {
                SRoom room = vs.getRoomStorage(roomIndex, day);
                IR_OR_RG_Ri[3] -= room.getPOverRC();//vs.getMatrixRiCost(roomIndex, day);
                buf = new BufferPRoom(room);
                vb.addInvolvedPRoom((BufferPRoom) buf);
            }
            if (remove) {
                buf.decPotentialStay();
            } else {
                buf.incPotentialStay();
            }

        }
    }

    public void calRoomValues(int[] IR_OR_RG_Ri) {
        for (SRoomValues buf : vb.getInvolvedRooms()) {
            if (buf.getDay() < instance.getPlanningHorizon()) {
                IR_OR_RG_Ri[0] += buf.getIdleRC();
            }
            IR_OR_RG_Ri[1] += buf.getOverRC();

            IR_OR_RG_Ri[2] += buf.getGenderPolicyConflict();

            IR_OR_RG_Ri[3] += buf.getPOverRC();
        }

        for (SRoomValues buf : vb.getInvolvedPotentialRooms()) {
            IR_OR_RG_Ri[3] += buf.getPOverRC();
        }
    }

    public void updateRoomValues() {
        vb.updateRoomStorageValues();
    }

    public void updateORValues() {
        vb.updateORStorageValues();
    }

//    public ArrayList<Integer> roomSelection(int roomNum, int currentDay){
//        ArrayList<Integer> targetRooms = Util.selection(1,vs.getWeights(currentDay, true));
//        ArrayList<Integer> roomScope = instance.getRoomScope(targetRooms.get(0));
//        targetRooms.addAll(Util.selection(roomNum-1,vs.getWeights(currentDay,roomScope, true)));
//        return targetRooms;
//    }

//    private int[] ORCostTemp;
//    private int[] roomRCostsTemp;
//    private void initializeCosts(){
//        ORCostTemp = new int[5];
//        roomRCostsTemp = new int[4];
//    }
//
//    public int[] getORCostTemp() {
//        return ORCostTemp;
//    }
//
//    public int[] getRoomRCostsTemp() {
//        return roomRCostsTemp;
//    }
//
//    public void roomAssignment(Participants move){
//        initializeCosts();
//        move.initialize();
//        for (int p = 0; p< move.getTargetPatients().size();p++) {
//            SPatient patient = move.getTargetPatients().get(p);
//            int[] temp = roomAssignment(patient, patient.getFeasibleRooms(), vb);
//            move.setAdmissionDay(p,temp[0]);
//            move.setRooms(p, temp[1]);
//        }
//    }
//
//    private int[] roomAssignment(SPatient patient, ArrayList<Integer> selectedRooms,
// Storage<SRoomValues> storage){
//        ArrayList<SolutionSelection> ss = new ArrayList<>();
//        int cost;
//        //int betterCost = Integer.MAX_VALUE;
//        int betterRoom = -1;
//        int betterAD = -1;
//        //boolean skip = true;
//        //count++;
//        //if (count == 3774)
//          //  System.out.println(count);
//        for (Integer roomIndex : selectedRooms) {
//            if (patient.roomAvailable(roomIndex)) {
//                int ad = patient.getAdmissionDay();
//                do {
//                    //skip = false;
//                    cost = (patient.getDelayPriority(ad)) * Params.DELAY +
//                            patient.getRoomCosts(roomIndex) +
//                            patient.transfer(roomIndex) * Params.TRANSFER;
//                    for (int d = ad; d < ad + patient.getRestLOS(); d++) {
//                        SRoomValues sRoom = storage.getRoomStorage(roomIndex, d);
//                        if (sRoom == null) {
//                            assert storage instanceof ValuesBuffer;
//                            sRoom = vs.getRoomStorage(roomIndex,d);
//                        }
//                        if (sRoom.getIdleRC()>0){
//                            if (d < instance.getHorizon()) {
//                                cost -= Params.IDLE_ROOM_CAPACITY;
//                            }
//                        }else {
//                            cost += Params.HARD_CONSTRAINTS;
//                        }
//                        if (sRoom.getTotalPOccupancy()>= sRoom.getRoomCapacity()){
//                            cost += Params.OVERCROWD_RISK;
//                        }
//                        if (instance.roomForSG(roomIndex)){
//                            if (sRoom.getMalePatients()<sRoom.getFemalePatients()&& patient
// .isMale()){
//                                cost += Params.GENDER_POLICY;
//                            }else if (sRoom.getFemalePatients() < sRoom.getMalePatients() &&
// !patient.isMale()){
//                                cost += Params.GENDER_POLICY;
//                            }
//                        }
//                    }
//
//                    if (patient.needSurgery()) {
//                        int surgeryDay = ad + patient.getPreSurgeryDay();
//                        int surgeryDuration = patient.getSurDur();
//                        int spec = patient.getSpec();
//                        SOR or = storage.getORStorage(surgeryDay);
//                        if (or == null){
//                            assert storage instanceof ValuesBuffer;
//                            or = vs.getORStorage(surgeryDay);
//                        }
//
//                        int temp1 = or.getOverORT() * Params.OVERTIME +
//                                or.getOverORTU() * Params.HARD_CONSTRAINTS +
//                                or.getIOS() * Params.IDLE_OPERATING_ROOM;
//                        or.incORDayOccupancy(surgeryDuration);
//
//                        cost += or.getOverORT() * Params.OVERTIME +
//                                or.getOverORTU() * Params.HARD_CONSTRAINTS +
//                                or.getIOS() * Params.IDLE_OPERATING_ROOM - temp1;
//
//                        or.decORDayOccupancy(surgeryDuration);
//
//                        if (patient.isElective()) {
//                            SSpecOR sSpecOR = or.getSpecOR(spec);
//                            if (sSpecOR == null){
//                                assert storage instanceof ValuesBuffer;
//                                sSpecOR = vs.getORStorage(surgeryDay).getSpecOR(spec);
//                            }
//                            int temp2 = sSpecOR.getOverOR() * Params.OVERTIME +
//                                    sSpecOR.getOverORU() * Params.HARD_CONSTRAINTS;
//                            sSpecOR.incOccupancy(surgeryDuration);
//
//                            cost += sSpecOR.getOverOR() * Params.OVERTIME +
//                                    sSpecOR.getOverORU() * Params.HARD_CONSTRAINTS - temp2;
//                            sSpecOR.decOccupancy(surgeryDuration);
//                        }
//                    }
//
//                    ss.add(new SolutionSelection(roomIndex,ad,cost));
//                    if (ad < patient.getMaxAD()){
//                        ad ++;
//                    }else {
//                        break;
//                    }
//                }while (true);
//            }
//        }
//        ss.sort(new SolutionSelection());
//
//        double p = 0.2;
//        int restOption = ss.size();
//        for (SolutionSelection s : ss) {
//            int key = restOption >1? 1:0;
//            if (Util.rand.nextDouble() < (1-p*(key))) {
//                betterAD = s.getAdmissionDay();
//                betterRoom = s.getRoomIndex();
//                break;
//            }
//            restOption--;
//        }
//        assert betterRoom != -1;
//        assert betterAD >= patient.getEarliestAD() && betterAD <= patient.getMaxAD();
//        /*
//        if (betterAD < patient.getEarliestAD() || betterAD > patient.getMaxAD()){
//            System.out.println();
//        }
//        */
//        // do the assignment.
//        for (int d = betterAD; d < betterAD + patient.getRestLOS(); d++) {
//            SRoomValues room = storage.getRoomStorage(betterRoom,d);
//            if (room == null){
//                assert storage instanceof ValuesBuffer;
//                room = addInvolvedRoom(betterRoom, d, roomRCostsTemp);
//            }
//            room.addPatient(patient);
//        }
//        if (patient.needSurgery()){
//            int surgeryDay = betterAD+patient.getPreSurgeryDay();
//            int surgeryDuration = patient.getSurDur();
//            SOR or = storage.getORStorage(surgeryDay);
//            if (or == null){
//                assert storage instanceof ValuesBuffer;
//                or = addInvolvedORD(surgeryDay,ORCostTemp);
//            }
//            or.incORDayOccupancy(surgeryDuration);
//            if (patient.isElective()){
//                SSpecOR sSpecOR = storage.getORStorage(surgeryDay).getSpecOR(patient.getSpec());
//                if (sSpecOR == null){
//                    assert storage instanceof ValuesBuffer;
//                    sSpecOR = addInvolvedORSpec(or,surgeryDay,patient.getSpec(),ORCostTemp);
//                }
//                sSpecOR.incOccupancy(surgeryDuration);
//            }
//        }
//        return new int[]{betterAD,betterRoom};
//    }
//

    public boolean compareBestSolution(SimulatedAnnealing sa, Solution solution, Solution
            bestSolution, boolean display) {
        boolean better = false;
        if (solution.getObjectiveValue() <= bestSolution.getObjectiveValue()) {
            better = true;
            if (display) {
                showResult(sa, solution);
            }
            /*
            if (solution.getOverRoomCost() == 0 && solution.getORTU() == 0 && solution.getORU()
            ==0&& Params.recordCurves){
                if (count1%300 == 0) {
                    int i = 0;
                    for (; i < solution.getCostContainer().length; i++) {
                        System.out.print(Params.getCostNames()[i] + ":\t" + solution
                        .getCostContainer()[i] + "\t");
                        DataRecordExcel.recordData(Params.dataFileName,0, count1, i, solution
                        .getCostContainer()[i]);
                    }
                    DataRecordExcel.recordData(Params.dataFileName,0, count1, i, solution
                    .getObjectiveValue());
                    count1++;
                    System.out.print("\n");
                }
                count1++;
            }
            */
        }
        return better;
    }

    public void showResult(SimulatedAnnealing sa, Solution bestSolution) {
        double runTime = (double) (System.currentTimeMillis() - Params.dayStateTime) / 1000;
        System.out.print(
                "H ObjVal:\t" + bestSolution.getObjectiveValue() +
                        "\tProgress:\t" + sa.getT() + ", " + sa.getIteration() +
                        "\truntime:\t" + runTime + "\t");
        for (int i = 0; i < bestSolution.getCostContainer().length; i++) {
            System.out.print(Params.getCostNames()[i] + ":\t" + bestSolution.getCostContainer()
                    [i] + "\t");
        }
        System.out.println();
    }
}
