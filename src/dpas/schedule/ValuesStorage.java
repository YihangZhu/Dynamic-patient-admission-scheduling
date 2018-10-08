/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.Params;
import dpas.instance.Instance;

import java.util.ArrayList;

/**
 * Created by zhuyi on 10/12/2016.
 * this class used to store the data get in the current day.
 */
public class ValuesStorage implements Storage<SRoomValues> {
    private Instance instance;
    private SRoom[][] roomOccupancyTemp;

    private SOR[] ORTemp;

    public ValuesStorage(Instance instance) {
        this.instance = instance;
        roomOccupancyTemp = new SRoom[instance.getRoomNum()][instance.getNumDays()];
        ORTemp = new SOR[instance.getNumDays()];
        for (int d = 0; d < instance.getNumDays(); d++) {
            for (int r = 0; r < instance.getRoomNum(); r++) {
                roomOccupancyTemp[r][d] = new SRoom(instance.getRooms(r), d);
            }
            ORTemp[d] = new SOR(d, instance.getORDayCapacity(d), instance.getORSpecCapacity(d),
                    instance.slotsNum(d));
        }
    }

    public void initialize(int day) {
        for (int d = day; d < instance.getNumDays(); d++) {
            ORTemp[d].initialize();
            for (int r = 0; r < instance.getRoomNum(); r++) {
                roomOccupancyTemp[r][d].initialize();
            }
        }
    }

    public SRoom getRoomStorage(int roomIndex, int day) {
        return roomOccupancyTemp[roomIndex][day];
    }

    public SOR getORStorage(int day) {
        return ORTemp[day];
    }

    public void incRoomOccupancy(int roomIndex, SPatient sPatient) {
        int endDay = sPatient.getDischargeDay();
        for (int d = sPatient.getAdmissionDay(); d < endDay; d++) {
            roomOccupancyTemp[roomIndex][d].addPatient(sPatient);
        }
        if (sPatient.getVariablity() > 0 && endDay < instance.getNumDays()) {
            roomOccupancyTemp[roomIndex][endDay].incPotentialStay();
        }
    }

    //    public void incRoomOccupancy(int[] roomIndex, SPatient patient){
//        for (int d = 0; d < patient.getRestLOS(); d++) {
//            int day = d + patient.getEarliestAD();
//            int r = roomIndex[d];
//            roomOccupancyTemp[r][day].addPatient(patient);
//        }
//        if (patient.getVariablity() > 0 && patient.getDischargeDay()<instance.getNumDays()){
//            roomOccupancyTemp[roomIndex[patient.getRestLOS()]][patient.getDischargeDay()]
// .incPotentialStay();
//        }
//    }

    public ArrayList<double[]> getWeights(int currentDay, ArrayList<Integer> roomScope, boolean
            all) {
        ArrayList<double[]> selectionWeight = new ArrayList<>();
        for (Integer roomIndex : roomScope) {
            selectionWeight.add(new double[]{roomIndex, getWeight(currentDay, roomIndex, all)});
        }
        return selectionWeight;
    }


    public ArrayList<double[]> getWeights(int currentDay, boolean all) {
        ArrayList<double[]> selectionWeight = new ArrayList<>();
        for (int roomIndex = 0; roomIndex < instance.getRoomNum(); roomIndex++) {
            selectionWeight.add(new double[]{roomIndex, getWeight(currentDay, roomIndex, all)});
        }
        return selectionWeight;
    }

    private int getWeight(int currentDay, int roomIndex, boolean all) {
        int gender = 0;
        int overRC = 0;
        //int delay = 0;
        //int transfer = 0;
        int idleCapacity = 0;
        int roomCosts = 0;
        int pOverRC = 0;
        for (int d = currentDay; d < instance.getNumDays(); d++) {
            SRoom room = roomOccupancyTemp[roomIndex][d];
            if (d < instance.getPlanningHorizon()) {
                idleCapacity += room.getIdleRC();
            }
            roomCosts += room.getRoomCost();
            if (all) {
                gender += room.getGenderPolicyConflict();
                overRC += room.getOverRC();
                //delay += room.getDelayCost();
                //transfer += room.getTransfer();
                pOverRC += room.getPOverRC();
            }
        }
        return gender * Params.GENDER_POLICY +
                overRC * Params.HARD_CONSTRAINTS +
                //delay*Params.DELAY +
                //transfer*Params.TRANSFER +
                idleCapacity * Params.IDLE_ROOM_CAPACITY +
                roomCosts +
                pOverRC * Params.OVERCROWD_RISK;
    }

}
