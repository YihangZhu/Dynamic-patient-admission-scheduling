/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;


import dpas.Params;
import dpas.instance.Gender;
import dpas.instance.Patient;
import dpas.instance.Status;

import java.util.ArrayList;

public class SPatient {
    private final String name;
    private final int number;
    private final boolean male;
    private final boolean elective;
    private final int totalLOS;
    private final int variablity;
    private final int surgeryDuration;
    private int spec;
    //private boolean surgeryRequired = false;
    private int preSurgeryDay;
    private int earliestSD;
    private int[][] roomCost;
    private ArrayList<Integer> feasibleRooms;
    private ArrayList<Integer> rooms;

    private Status status;
    private int restLOS;
    private int priority;
    private int earliestAD;
    // The room the patient stay on the previous day.
    private int preRoomIndex = -1;
    private int maxDelay;
    private int maxAD;

    private int admissionDay;
    private int surgeryDay;
    private int dischargeDay;

    private int delayTemp = 0;
    private int roomIndexTemp = -1;

    private int admissionDelay = 0;  // the total admission delay. only used in restore final
    // result.

    public SPatient(Patient patient) {
        name = patient.getName();
        number = patient.getNumber();
        male = patient.getGender().equals(Gender.Male);
        elective = patient.isElective();
        variablity = patient.getVariability();
        surgeryDuration = patient.getTreatment().getDurationSurgery();
        spec = patient.getTreatment().getSpecialism();
        roomCost = patient.getRoomCost();
        feasibleRooms = patient.getFeasibleRooms();
        totalLOS = patient.getLOS();

        rooms = new ArrayList<>(totalLOS);
        restLOS = totalLOS;

        priority = patient.getPriority();

        maxAD = patient.getMaxAdmission();
        earliestAD = patient.getDateAdmission();
        maxDelay = maxAD - earliestAD;
        earliestSD = patient.getDateSurgery();
        if (earliestSD != -1) {
            preSurgeryDay = earliestSD - earliestAD;
        }
        admissionDay = patient.getDateAdmission();
        dischargeDay = patient.getDateDischarge();
        if (patient.getRoom() != null) {
            preRoomIndex = patient.getRoom().getNumber();
            status = Status.Admitted;
        } else {
            status = Status.Registered;
        }

    }


    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public boolean isMale() {
        return male;
    }

    public int getVariablity() {
        return variablity;
    }

    public int getPreSurgeryDay() {
        return preSurgeryDay;
    }

    public int getTotalLOS() {
        return totalLOS;
    }

    public ArrayList<Integer> getFeasibleRooms() {
        return feasibleRooms;
    }

    public int getRoomFeatureCost(int roomIndex) {
        return roomCost[roomIndex][1];
    }

    public int getRoomPreferCost(int roomIndex) {
        return roomCost[roomIndex][2];
    }

    public int getRoomSpecCost(int roomIndex) {
        return roomCost[roomIndex][3];
    }

    public int getRoomFixedGenderCost(int roomIndex) {
        return roomCost[roomIndex][4];
    }

    public int getSurDur() {
        return surgeryDuration;
    }

    public boolean isElective() {
        return elective;
    }

    public boolean needSurgery() {
        return earliestSD > -1;
        //return surgeryRequired;
    }

    public int getSpec() {
        return spec;
    }

    public boolean roomAvailable(int roomIndex) {
        return roomCost[roomIndex][0] != -1;
    }

    public int getRooms(int index) {
        return rooms.get(index);
    }

    public void addRoom(int roomIndex) {
        rooms.add(roomIndex);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public boolean isRegistered() {
        return status.equals(Status.Registered);
    }

    public int getPreRoomIndex() {
        return preRoomIndex;
    }

    public int getAdmissionDay() {
        return admissionDay;
    }

    public int getEarliestDD() {
        return earliestAD + restLOS;
    }

    public int getMaxDD() {
        return maxAD + restLOS;
    }

    void setAdmissionDay(int admissionDay) {
        this.admissionDay = admissionDay;
        if (status.equals(Status.Admitted)) {
            maxAD = admissionDay;
            earliestAD = admissionDay;
            maxDelay = 0;
            restLOS = dischargeDay - admissionDay;
        } else {
            delayTemp = admissionDay - earliestAD;
            if (earliestSD != -1) {
                surgeryDay = admissionDay + preSurgeryDay;
            }
            dischargeDay = admissionDay + totalLOS;
        }
    }

    public void setFinalAdmissionDay(int admissionDay) {
        this.admissionDelay = admissionDay - this.admissionDay;
        this.admissionDay = admissionDay;
        this.dischargeDay = admissionDay + totalLOS;
    }

    public int getRestLOS() {
        return restLOS;
    }

    public void setAdmissionDay() {
        admissionDay = dischargeDay - totalLOS;
    }

    public int getDischargeDay() {
        return dischargeDay;
    }

    public int getSD() {
        return surgeryDay;
    }

    public int getEarliestAD() {
        return earliestAD;
    }

    public int getEarliestSD() {
        return earliestSD;
    }

    public void setAdmissionDelay(int admissionDelay) {
        this.admissionDelay = admissionDelay;
    }

    public int getAdmissionDelay() {
        return admissionDelay;
    }

    public void setRoomIndexTemp(int roomIndexTemp) {
        this.roomIndexTemp = roomIndexTemp;
    }

    public int transfer() {
        if (roomIndexTemp == preRoomIndex || preRoomIndex == -1) {
            return 0;
        } else {
            return 1;
        }
    }

    public int transfer(int newRoom) {
        if (newRoom == preRoomIndex || preRoomIndex == -1) {
            return 0;
        } else {
            return 1;
        }
    }

    public int getRoomCost(int roomIndex) {
        return roomCost[roomIndex][0];
    }

    /**
     * @param newRoom new room index
     * @return total room cost during rest of stay
     */
    public int getRoomCosts(int newRoom) {
        return roomCost[newRoom][0] * restLOS;
    }

    /**
     * @return total room cost during rest of stay
     */
    public int getRoomCosts() {
        return roomCost[roomIndexTemp][0] * restLOS;
    }

    void setPreRoomIndex(int preRoomIndex) {
        this.preRoomIndex = preRoomIndex;
    }

    public int getPriority() {
        if (priority < 0) {
            throw new IllegalArgumentException("Priority cannot be less than 0: " + priority);
        }
        return Params.STATIC || Params.dynStrategy ? 1 : priority;
//        return Params.dynStrategy ? priority : 1;
    }

    public int getMaxAD() {
        return maxAD;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    void disableSurgery() {
        if (earliestSD == -1) {
            throw new IllegalArgumentException("The patient is not able to have surgery!");
        }
        earliestSD = -1;
        //surgeryRequired = false;
    }

    public int getRoomIndexTemp() {
        if (roomIndexTemp == -1) {
            throw new IllegalArgumentException("roomIndexTemp = -1!");
        }
        return roomIndexTemp;
    }

    public int getDelayTemp() {
        return delayTemp;
    }

    public int getDelayPriority() {
        return delayTemp * getPriority();
    }

    public int getDelayPriority(int newAdmission) {
        return (newAdmission - earliestAD) * getPriority();
    }

    public void setDelayTemp(int delayTemp) {
        if (!status.equals(Status.Registered)) {
            throw new IllegalStateException("The patient is already admitted!");
        }
        this.delayTemp = delayTemp;
        admissionDay = delayTemp + earliestAD;
        if (earliestSD != -1) {
            surgeryDay = admissionDay + preSurgeryDay;
        }
        dischargeDay = admissionDay + totalLOS;
    }

    void updateEarliestAdmissionDay(int currentDay) {
        earliestAD = currentDay;
        maxDelay--;
        if (earliestSD > -1) {
            earliestSD++;
        }
        //if (earliestAD > maxAD)
        if (earliestAD > maxAD) {
            throw new IllegalArgumentException("Earliest admission day is larger than the maximum" +
                    " admission day!");
        }
        if (!(maxDelay >= 0 && maxDelay == maxAD - earliestAD)) {
            throw new IllegalArgumentException("Maximum delay day is not correct!");
        }
    }

    public ArrayList<Integer> getRooms() {
        return rooms;
    }

    void updatePriority() {
        if (priority > 0) {
            this.priority--; // is it a good idea, it mean, the more days patient delayed, the
            // lower priority the patient will have
        }
    }

    @Override
    public String toString() {
        return name + "\t"
                + (male ? "male" : "female") + "\t"
                + status + "\t"
                + "earliestAD: " + earliestAD + "\t"
                + "restLOS: " + restLOS + "\t"
                + "earliestSD: " + earliestSD;
    }
}
