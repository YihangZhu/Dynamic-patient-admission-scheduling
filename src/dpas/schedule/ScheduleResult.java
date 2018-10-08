/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.instance.Instance;

import java.time.LocalDate;


/**
 * Created by zhuyi on 9/19/2016
 */

public class ScheduleResult {
    private Instance instance;

    private String name;
    private LocalDate startDate;
    private int extendedHorizonDays;
    private int currentDate;
    private SPatient[] sPatients; // has the same sequence as patients in class instances.
    private int[][] roomOccupancy;
    private int[][] roomMaleOccupancy;
    private int[][] roomFemaleOccupancy;
    private int[][] overRiskOccupancy;
    private int[][] OROccupancy;
    private int[] ORDayOccupancy;

    public ScheduleResult(Instance instance) {
        this.instance = instance;
        name = instance.getName();
        startDate = instance.getDateStart();
        extendedHorizonDays = instance.getNumDays();
        sPatients = new SPatient[instance.getPatientNum()];
        initializeOccupancy();
    }

    public void initializeOccupancy() {
        roomOccupancy = new int[extendedHorizonDays][instance.getRoomNum()];
        roomMaleOccupancy = new int[extendedHorizonDays][instance.getRoomNum()];
        roomFemaleOccupancy = new int[extendedHorizonDays][instance.getRoomNum()];
        overRiskOccupancy = new int[extendedHorizonDays + 1][instance.getRoomNum()];
        OROccupancy = new int[extendedHorizonDays][instance.getSpecNum()];
        ORDayOccupancy = new int[extendedHorizonDays];
    }

    public void addSPatient(SPatient sPatient) {
        sPatients[sPatient.getNumber()] = sPatient;
    }

    public SPatient getSPatients(int patientIndex) {
        return sPatients[patientIndex];
    }

    public String getName() {
        return name;
    }

    LocalDate getStartDate() {
        return startDate;
    }

    int getExtendedHorizonDays() {
        return extendedHorizonDays;
    }

    int getCurrentDate() {
        return currentDate;
    }

    void setCurrentDate(int currentDate) {
        this.currentDate = currentDate;
    }

    public void increaseRoomOccupancy(int day, int roomIndex, boolean male) {
        roomOccupancy[day][roomIndex]++;
        if (male) {
            roomMaleOccupancy[day][roomIndex]++;
        } else {
            roomFemaleOccupancy[day][roomIndex]++;
        }
    }

    public void increaseOverRiskOccupancy(int day, int roomIndex) {
        overRiskOccupancy[day][roomIndex]++;
    }

    public void incOROccupancy(int surgeryDay, int spec, int surgeryDuration) {
        OROccupancy[surgeryDay][spec] += surgeryDuration;
    }

    public void incORDayOccupancy(int surgeryDay, int surgeryDuration) {
        ORDayOccupancy[surgeryDay] += surgeryDuration;
    }

    public int minorGenderOccupancy(int day, int roomIndex) {
        return Math.min(roomMaleOccupancy[day][roomIndex], roomFemaleOccupancy[day][roomIndex]);
    }

    public int usedRoomCapacity(int day, int roomIndex) {
        return roomOccupancy[day][roomIndex];
    }

    public int usedORCapacity(int surgeryDay, int spec) {
        return OROccupancy[surgeryDay][spec];
    }

    public int usedORDayCapacity(int surgeryDay) {
        return ORDayOccupancy[surgeryDay];
    }

    public int getPotentialOccupancy(int day, int roomIndex) {
        return roomOccupancy[day][roomIndex] + overRiskOccupancy[day][roomIndex];
    }

}
