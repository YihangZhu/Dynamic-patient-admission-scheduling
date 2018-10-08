/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.instance;


import java.util.ArrayList;

public class Patient {
    private String name;
    private int number;
    private int preferredCapacity = Integer.MAX_VALUE;
    private int variability = 0;
    private Gender gender;
    private int age;
    private ArrayList<Integer> featuresPreferred = new ArrayList<>();
    private ArrayList<Integer> featuresNeed = new ArrayList<>();
    private Treatment treatment;
    private int dateRegister = -1;
    private int dateAdmission = -1;
    private int dateDischarge = -1;
    private ArrayList<Integer> feasibleRooms = new ArrayList<>();
    private ArrayList<Integer> perfectRooms = new ArrayList<>();
    private ArrayList<Integer> feasibleAdmissionDays = new ArrayList<>();
    private int dateSurgery = -1;
    private Room room = null;
    private int maxAdmission = -1;
    private String[] types = {"preferred", "needed"};
    private int[][] roomCost;
    private PatientType patientType;
    private int priority = 1;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    void setNumber(int number) {
        this.number = number;
    }

    void setPreferredCapacity(int preferredCapacity) {
        this.preferredCapacity = preferredCapacity;
    }

    int getPreferredCapacity() {
        return this.preferredCapacity;
    }

    public void setVariability(int variability) {
        this.variability = (variability > 0) ? 1 : 0;
    }

    public int getVariability() {
        return variability;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    ArrayList<Integer> getFeaturesPreferred() {
        return featuresPreferred;
    }

    ArrayList<Integer> getFeaturesNeed() {
        return featuresNeed;
    }

    void setFeatures(String type, int feature) {
        if (type.equals(types[0])) {
            featuresPreferred.add(feature);
        } else if (type.equals(types[1])) {
            featuresNeed.add(feature);
        }
    }

    public void setTreatment(Treatment treatment) {
        this.treatment = treatment;
    }

    public Treatment getTreatment() {
        return treatment;
    }

    void setDateRegister(int date) {
        dateRegister = date;
    }

    public int getDateRegister() {
        return dateRegister;
    }

    void setDateAdmission(int date) {
        dateAdmission = date;
    }

    public int getDateAdmission() {
        return dateAdmission;
    }

    void setDateDischarge(int date) {
        dateDischarge = date;
    }

    public int getDateDischarge() {
        return dateDischarge;
    }

    public int getMaxAdmission() {
        return maxAdmission;
    }

    public int getPriority() {
        return priority;
    }

    void setDateSurgery(int dateSurgery) {
        this.dateSurgery = dateSurgery;
    }

    public int getDateSurgery() {
        return dateSurgery;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;

    }

    public int getLOS() {
        return dateDischarge - dateAdmission;
    }

    public int[][] getRoomCost() {
        return roomCost;
    }

    public boolean needSurgery() {
        return dateSurgery != -1;
    }

    public ArrayList<Integer> getFeasibleRooms() {
        return feasibleRooms;
    }

    public ArrayList<Integer> getPerfectRooms() {
        return perfectRooms;
    }

    ArrayList<Integer> getFeasibleAdmissionDays() {
        return feasibleAdmissionDays;
    }

    boolean isRegistered() {
        return room == null;
    }
    //****************************************************************************************************************************


    void setMaxAdmission(int date) {
        maxAdmission = date;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    void identifyElective() {
        if (maxAdmission == dateAdmission && dateAdmission == dateRegister) {
            patientType = PatientType.Urgent;
        } else {
            patientType = PatientType.Elective;
        }
    }

    public boolean isElective() {
        return patientType.equals(PatientType.Elective);
    }

    void setRoomCost(int[][] roomCost) {
        this.roomCost = roomCost;
    }

    void addFeasibleRoom(int feasibleRoom) {
        feasibleRooms.add(feasibleRoom);
    }

    void addPerfectRoom(int perfectRoom) {
        perfectRooms.add(perfectRoom);
    }

    public void addFeasibleAdmissionDays(int feasibleAdmissionDay) {
        feasibleAdmissionDays.add(feasibleAdmissionDay);
    }


}