/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

/**
 * Created by zhuyi on 2/7/2017.
 */
public abstract class SRoomValues {
    private int roomNumber;
    private int day;
    private int roomCapacity;
    //private ArrayList<SPatient> patients;
    private int occupancy;
    private int pOccupancy;

    public SRoomValues(SRoom room) {
        roomNumber = room.getRoomNum();
        day = room.getDay();
        roomCapacity = room.getRoomCapacity();
        //patients = new ArrayList<>(room.getPatients());
        occupancy = room.getOccupancy();
        pOccupancy = room.getPOccupancy();
    }

    SRoomValues(int roomNumber, int day, int roomCapacity) {
        this.roomNumber = roomNumber;
        this.day = day;
        this.roomCapacity = roomCapacity;
    }

    public int getRoomNum() {
        return roomNumber;
    }

    public int getDay() {
        return day;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    void incOccupancy() {
        occupancy++;
    }

    void decOccupancy() {
        occupancy--;
        assert occupancy >= 0;
    }

    public int getOccupancy() {
        return occupancy;
    }

    //    public ArrayList<SPatient> getPatients() {
//        return patients;
//    }
    public int getPOccupancy() {
        return pOccupancy;
    }

    public int getTotalPOccupancy() {
        return pOccupancy + occupancy;
    }

    void updatePOccupancy(int potentialOccupancy) {
        this.pOccupancy = potentialOccupancy;
    }

    public void incPotentialStay() {
        pOccupancy++;
    }

    public void decPotentialStay() {
        pOccupancy--;
        assert pOccupancy >= 0;
    }

    public int getPOverRC() {
        return Integer.max(0, occupancy + pOccupancy - roomCapacity);
    }

    public void initialize() {
//        patients = new ArrayList<>();
        occupancy = 0;
        pOccupancy = 0;
    }

    public void addPatient(SPatient patient) {
        throw new UnsupportedOperationException();
    }

    public void removePatient(SPatient patient) {
        throw new UnsupportedOperationException();
    }

    public int getMalePatients() {
        throw new UnsupportedOperationException();
    }

    public int getFemalePatients() {
        throw new UnsupportedOperationException();
    }


    public void updateOccupancy(SRoomValues buf) {
//        patients = buf.getPatients();
        occupancy = buf.getOccupancy();
        pOccupancy = buf.getPOccupancy();
    }

    public int getGenderPolicyConflict() {
        throw new UnsupportedOperationException();
    }

    public int getRoomCost() {
        throw new UnsupportedOperationException();
    }

    public int getTransfer() {
        throw new UnsupportedOperationException();
    }

    //    public int getDelayCost(){
//        throw new UnsupportedOperationException();
//    }
    public int getIdleRC() {
        return Integer.max(0, roomCapacity - occupancy);
    }

    public int getOverRC() {
        return Integer.max(0, occupancy - roomCapacity);
    }
}
