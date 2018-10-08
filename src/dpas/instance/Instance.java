/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.instance;

import dpas.Params;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by zhuyi on 8/30/2016.
 */

public class Instance {
    private String name;
    private int specialismNum;
    private int featureNum;
    private int departmentNum;
    private int roomNum;
    private int ORNum;
    private int patientNum;
    private int planningHorizon;
    private int numDays;
    private LocalDate dateStart;
    private int slotLengthOR;

    private ArrayList<String> specialisms;
    private ArrayList<String> features;
    private ArrayList<String> departmentNames;
    private Department[] departments;
    private HashMap<String, Treatment> treatments;  //only used in reading instance from data.
    private int[][] operatingRooms;
    private int[][] OR_Slots;
    private int[] ORDayCapacity;
    private ArrayList<String> roomNames;
    private Room[] rooms;
    private ArrayList<String> patientNames;
    private Patient[] patients;
    private int totalRoomCapacity = 0;
    private int totalRequiredRoomCapacity = 0;
    private int totalORCapacity = 0;
    private int totalRequiredORCapacity = 0;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    void setDepNum(int depNum) {
        departmentNum = depNum;
        departments = new Department[depNum];
        departmentNames = new ArrayList<>(depNum);
    }

    int getDepartmentNum() {
        return departmentNum;
    }

    void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
        roomNames = new ArrayList<>(roomNum);
        rooms = new Room[roomNum];
    }

    public int getRoomNum() {
        return roomNum;
    }

    void setFeatureNum(int featureNum) {
        this.featureNum = featureNum;
        features = new ArrayList<>(featureNum);
    }

    int getFeatureNum() {
        return featureNum;
    }

    void setPatientNum(int patientNum) {
        this.patientNum = patientNum;
        patientNames = new ArrayList<>(patientNum);
        patients = new Patient[patientNum];
    }

    public int getPatientNum() {
        return patientNum;
    }

    void setSpecialismNum(int specialismNum) {
        this.specialismNum = specialismNum;
        specialisms = new ArrayList<>(specialismNum);
    }

    public int getSpecNum() {
        return specialismNum;
    }

    void setPlanningHorizon(int planningHorizon) {
        this.planningHorizon = planningHorizon;
        numDays = Params.REPEATED_HORIZONTAL * planningHorizon;
        operatingRooms = new int[numDays][specialismNum];
        OR_Slots = new int[numDays][specialismNum];
        ORDayCapacity = new int[numDays];
    }

    public int getPlanningHorizon() {
        return this.planningHorizon;
    }

    void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getDateStart() {
        return this.dateStart;
    }

    void setTreatmentNum(int treatmentNum) {
        this.treatments = new HashMap<>(treatmentNum);
    }

    void addSpecialisms(String specialism) {
        specialisms.add(specialism);
    }

    public ArrayList<String> getSpecialisms() {
        return this.specialisms;
    }

    void addFeatures(String feature) {
        features.add(feature);
    }

    int getFeature(String str) {
        for (int f = 0; f < featureNum; f++) {
            if (str.equals(features.get(f))) {
                return f;
            }
        }
        assert false;
        return -1;
    }

    void addDepartments(Department department) {
        departmentNames.add(department.getName());
        int index = departmentNames.indexOf(department.getName());
        departments[index] = department;
    }

    ArrayList<String> getDepartmentNames() {
        return departmentNames;
    }

    Department[] getDepartments() {
        return departments;
    }

    public ArrayList<String> getRoomNames() {
        return roomNames;
    }

    void addRooms(Room room) {
        roomNames.add(room.getName());
        int index = roomNames.indexOf(room.getName());
        room.setNumber(index);
        rooms[index] = room;
    }

    void addTreatments(Treatment treatment) {
        this.treatments.put(treatment.getName(), treatment);
    }

    HashMap<String, Treatment> getTreatments() {
        return this.treatments;
    }

    void setSlotLengthOR(int slotLengthOR) {
        this.slotLengthOR = slotLengthOR;
    }

    void addOperatingRoom(int day, int specialism, int slotNum) {
        operatingRooms[day][specialism] = slotNum * slotLengthOR;
        OR_Slots[day][specialism] = slotNum;
    }

    void extendOperatingRooms() {
        for (int day = planningHorizon; day < numDays; day++) {
            operatingRooms[day] = operatingRooms[day % planningHorizon].clone();
            OR_Slots[day] = OR_Slots[day % planningHorizon].clone();
        }
    }

    void addPatients(Patient patient) {
        patientNames.add(patient.getName());
        int index = patientNames.indexOf(patient.getName());
        patient.setNumber(index);
        patients[index] = patient;
    }

    public ArrayList<String> getPatientNames() {
        return patientNames;
    }

    //private int count = 0;
    void preparation() {
        int preferenceCostWeight = Params.ROOM_PREFERENCE;
        int genderCostWeight = Params.GENDER_POLICY;
        int specialismCostWeight = Params.SPECIALISM;
        int featureCostWeight = Params.ROOM_PROPERTY;

        for (Room room : rooms) {
            totalRoomCapacity = totalRoomCapacity + room.getCapacity();
        }


        for (int day = 0; day < numDays; day++) {
            for (int s = 0; s < specialismNum; s++) {
                ORDayCapacity[day] += operatingRooms[day][s];
            }
            if (day < planningHorizon) {
                totalORCapacity += ORDayCapacity[day];
            }
            if (ORDayCapacity[day] / slotLengthOR > ORNum) {
                ORNum = ORDayCapacity[day] / slotLengthOR;
            }
        }

        for (Patient patient : patients) {
            patient.identifyElective();
            if (patient.isRegistered()) {
                patient.setPriority(planningHorizon - patient.getDateAdmission());
            }
            if (patient.getMaxAdmission() == -1) {
                if (patient.isRegistered()) {
                    patient.setMaxAdmission(numDays - (patient.getDateDischarge() - patient
                            .getDateAdmission()));
                } else {
                    patient.setMaxAdmission(patient.getDateAdmission());
                }
            }
            if (patient.needSurgery()) {
                int sd = patient.getTreatment().getDurationSurgery();
                if (patient.isElective()) {
                    int spec = patient.getTreatment().getSpecialism();
                    for (int day = patient.getDateAdmission(); day <= patient.getMaxAdmission();
                         day++) {
                        if (operatingRooms[day][spec] + Params.ADMITTED_OVERTIME >= sd &&
                                ORDayCapacity[day] + Params.ADMITTED_TOTAL_OVERTIME >= sd) {
                            patient.getFeasibleAdmissionDays().add(day);
                        }
                    }
                } else {
                    for (int day = patient.getDateAdmission(); day <= patient.getMaxAdmission();
                         day++) {
                        if (ORDayCapacity[day] + Params.ADMITTED_TOTAL_OVERTIME >= sd) {
                            patient.getFeasibleAdmissionDays().add(day);
                        }
                    }
                }
            }


            if (patient.getDateSurgery() != -1) {
                totalRequiredORCapacity += patient.getTreatment().getDurationSurgery();
            }

            int endDay = Math.min(planningHorizon, patient.getDateDischarge());
            for (int day = patient.getDateAdmission(); day < endDay; day++) {
                totalRequiredRoomCapacity++;
            }

            int[][] RMatrix = new int[roomNum][5];
            for (Room room : rooms) {
                int roomIndex = room.getNumber();
                int featureCost = 0;
                int preferenceCost = 0;
                int specialismCost = 0;
                int genderPolicyCost = 0;
                boolean specialismMain;
                boolean specialismAux;
                boolean specialism, feature = true;
                specialismAux = room.getDepartment().getMinorSpecialisms(patient.getTreatment()
                        .getSpecialism());
                specialismMain = room.getDepartment().getMainSpecialisms(patient.getTreatment
                        ().getSpecialism());
                specialism = specialismAux || specialismMain;
                for (int f : patient.getFeaturesNeed()) {
                    if (!room.getFeatures()[f]) {
                        feature = false;
                    }
                }
                boolean roomAvailable = specialism && feature;
                if (roomAvailable) {
                    if (specialismAux) {
                        specialismCost++;
                    }

                    for (int f : patient.getFeaturesPreferred()) {
                        if (!room.getFeatures()[f]) {
                            featureCost++;
                        }
                    }
                    if (room.getCapacity() > patient.getPreferredCapacity()) {
                        preferenceCost = 1;
                    }

                    if (room.getGenderPolicy().equals(GenderPolicy.FemaleOnly) && patient
                            .getGender().equals(Gender.Male)) {
                        genderPolicyCost = 1;
                    } else if (room.getGenderPolicy().equals(GenderPolicy.MaleOnly) && patient
                            .getGender().equals(Gender.Female)) {
                        genderPolicyCost = 1;
                    }

                    // 0: RoomCost; 1: FeatureCost; 2: PreferenceCost: 3: DeptCost: 4:
                    // FixedGenderCost;
                    int roomCost = preferenceCost * preferenceCostWeight + genderPolicyCost *
                            genderCostWeight +
                            specialismCost * specialismCostWeight + featureCost * featureCostWeight;
                    RMatrix[roomIndex][0] = roomCost;
                    RMatrix[roomIndex][1] = featureCost;
                    RMatrix[roomIndex][2] = preferenceCost;
                    RMatrix[roomIndex][3] = specialismCost;
                    RMatrix[roomIndex][4] = genderPolicyCost;
                    if (roomCost == 0) {
                        patient.addPerfectRoom(roomIndex);
                    }
                    patient.addFeasibleRoom(roomIndex);
                } else {
                    RMatrix[roomIndex][0] = -1;
                }
            }
            patient.setRoomCost(RMatrix);
        }
    }

    public boolean roomForSG(int roomIndex) {
        return rooms[roomIndex].getGenderPolicy().equals(GenderPolicy.SameGender);
    }

    public int getRoomCapacity(int roomIndex) {
        return rooms[roomIndex].getCapacity();
    }

    public int getSpecialism(int patientIndex) {
        return patients[patientIndex].getTreatment().getSpecialism();
    }


    public int getORDayCapacity(int day) {
        return ORDayCapacity[day];
    }

    public Patient[] getPatients() {
        return patients;
    }

    public Room getRooms(int roomIndex) {
        return rooms[roomIndex];
    }

    public Room[] getRooms() {
        return rooms;
    }

    public int slotsNum(int day, int spec) {
        return OR_Slots[day][spec];
    }

    public int[] slotsNum(int day) {
        return OR_Slots[day];
    }

    public int getORSpecCapacity(int day, int spec) {
        return operatingRooms[day][spec];
    }

    public int[] getORSpecCapacity(int day) {
        return operatingRooms[day];
    }

    public int ORDayCapacity(int day) {
        return ORDayCapacity[day];
    }

    public int getTotalRoomCapacity(int currentDay) {
        int days = planningHorizon - currentDay;
        return totalRoomCapacity * (days > 0 ? days : 0);
    }

    public int getTotalRoomCapacity() {
        return totalRoomCapacity;
    }

    public int getTotalORCapacity(int currentDay) {
        int totalOR = 0;
        for (int d = currentDay; d < planningHorizon; d++) {
            totalOR += getORDayCapacity(d);
        }
        return totalOR;
    }

    public int maximumRoomUsage() {
        int totalRoomCapacity = this.totalRoomCapacity * planningHorizon;
        return Params.STATIC ? Math.min(totalRoomCapacity, totalRequiredRoomCapacity) :
                totalRoomCapacity;
    }

    public int maximumORUsage() {
        return Params.STATIC ? Math.min(totalORCapacity, totalRequiredORCapacity) : totalORCapacity;
    }

    public int redundantRoomCap() {
        return Math.max(0, totalRoomCapacity * planningHorizon - totalRequiredRoomCapacity);
    }

    public int redundantORCap() {
        return Math.max(0, totalORCapacity - totalRequiredORCapacity);
    }

    public int getNumDays() {
        return numDays;
    }

    public int getSpecAOT(int day, int spec) {
        return OR_Slots[day][spec] * Params.ADMITTED_OVERTIME;
    }

    public int getTotORSpecCap(int day, int spec) {
        return OR_Slots[day][spec] * Params.ADMITTED_OVERTIME + getORSpecCapacity(day, spec);
    }

    public int getTotORDayCap(int day) {
        return Params.ADMITTED_TOTAL_OVERTIME + getORDayCapacity(day);
    }

    public int getORNum() {
        return ORNum;
    }
}
