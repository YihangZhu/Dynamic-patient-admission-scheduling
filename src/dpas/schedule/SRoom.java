/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.instance.GenderPolicy;
import dpas.instance.Room;
//import dpas.instance.Status;

/**
 * Created by zhuyi on 1/31/2017.
 */
public class SRoom extends SRoomValues {

    private boolean forSameGender;
    private int malePatients;
    private int femalePatients;
    private int roomCost;

    //    private int transfer;
//    private int delayCost;
    SRoom(Room room, int day) {
        super(room.getNumber(), day, room.getCapacity());
        forSameGender = room.getGenderPolicy().equals(GenderPolicy.SameGender);
    }

    public SRoom(SRoom room) {
        super(room);
        malePatients = room.getMalePatients();
        femalePatients = room.getFemalePatients();
        forSameGender = room.isForSameGender();
        roomCost = room.getRoomCost();
//        transfer = room.getTransfer();
//        delayCost = room.getDelayCost();
    }

    public void initialize() {
        super.initialize();
        malePatients = 0;
        femalePatients = 0;
        roomCost = 0;
//        transfer = 0;
//        delayCost = 0;
    }

    private boolean isForSameGender() {
        return forSameGender;
    }

    public int getMalePatients() {
        return malePatients;
    }

    public int getFemalePatients() {
        return femalePatients;
    }

    public void addPatient(SPatient patient) {
//        getPatients().add(patient);
        incOccupancy();
        if (patient.isMale()) {
            malePatients++;
        } else {
            femalePatients++;
        }

        //if (!patient.roomAvailable(getRoomNum()))
        assert roomCost >= 0;
        roomCost += patient.getRoomCost(getRoomNum());
    }

    public void removePatient(SPatient patient) {
//        assert getPatients().remove(patient);
        decOccupancy();
        if (patient.isMale()) {
            malePatients--;
            assert malePatients >= 0;
        } else {
            femalePatients--;
            assert femalePatients >= 0;
        }
        roomCost -= patient.getRoomCost(getRoomNum());
        assert roomCost >= 0;
    }

    public void updateOccupancy(SRoomValues buf) {
        super.updateOccupancy(buf);
        malePatients = buf.getMalePatients();
        femalePatients = buf.getFemalePatients();
        roomCost = buf.getRoomCost();
//        transfer = 0;
//        delayCost = 0;
//        for (SPatient patient:getPatients()){
//            transfer += patient.transfer();
//        }
//        for (SPatient patient:getPatients()){
//            delayCost += patient.getDelayPriority();
//        }
//        delayCost = buf.getDelayCost();
    }

    public int getGenderPolicyConflict() {
        if (forSameGender) {
            return Math.min(malePatients, femalePatients);
        } else {
            return 0;
        }
    }

    public int getRoomCost() {
        return roomCost;
    }

//    public int getTransfer() {
//        return transfer;
//    }
//
//    public int getDelayCost() {
//        return delayCost;
//    }
}
