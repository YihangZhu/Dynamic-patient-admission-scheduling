/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.Params;
import dpas.instance.Instance;
import dpas.instance.Patient;
import dpas.instance.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by zhuyi on 2/19/2017.
 */
public class Problem {
    private Instance instance;
    private int current;
    private ScheduleResult scheduleResult;
    private ArrayList<SPatient> unadmittedPatients = new ArrayList<>();
    private ArrayList<SPatient> initialPatients = new ArrayList<>();
    private ArrayList<SPatient> registeredPatients = new ArrayList<>();
    private ArrayList<Patient> patientsSet = new ArrayList<>();
    private int roomDemand;
    private int orDemand;

    public Problem(Instance instance, ScheduleResult scheduleResult) {
        Collections.addAll(patientsSet, instance.getPatients());
        this.scheduleResult = scheduleResult;
        this.instance = instance;
    }

    public ArrayList<SPatient> getUnadmittedPatients() {
        return unadmittedPatients;
    }

    public ArrayList<SPatient> getPatientSet() {
        return registeredPatients;
    }

    public void addPatients() {
        if (Params.STATIC){ // load all patient requests when static.
            patientsSet.forEach(patient -> registeredPatients.add(new SPatient(patient)));
        }else {
            Iterator<Patient> patientIterator = patientsSet.iterator();
            while (patientIterator.hasNext()) {
                Patient patient = patientIterator.next();
                if (patient.getRoom() != null) {
                    SPatient sPatient = new SPatient(patient);
                    scheduleResult.addSPatient(sPatient);
                    initialPatients.add(sPatient);
                    patientIterator.remove();
                }
            }
        }
    }

    public boolean updatePatientList(int currentDay) {
        // discharge patients
        this.current = currentDay;
        Iterator<SPatient> sPatientIterator = initialPatients.iterator();
        while (sPatientIterator.hasNext()) {
            SPatient sPatient = sPatientIterator.next();
            if (!sPatient.getStatus().equals(Status.Admitted)) {
                throw new IllegalStateException("The patient is not admitted!");
            }
            if (sPatient.getDischargeDay() == currentDay) {
                sPatient.setStatus(Status.Discharged);
                sPatientIterator.remove();
            } else {
                if (!(sPatient.getAdmissionDay() == currentDay - 1 || (sPatient.getAdmissionDay()
                        == currentDay && currentDay == 0))) {
                    throw new IllegalStateException("Illegal things happen!");
                }
                sPatient.setAdmissionDay(currentDay);
                if (sPatient.needSurgery()) {
                    if (currentDay > sPatient.getSD()) {
                        sPatient.disableSurgery();
                    }
                }
            }
        }
        // adjust the date for unadmitted patients
        sPatientIterator = unadmittedPatients.iterator();
        while (sPatientIterator.hasNext()) {
            SPatient sPatient = sPatientIterator.next();
            if (!sPatient.getStatus().equals(Status.Registered)) {
                throw new IllegalArgumentException("The patient is not registered!");
            }
            if (sPatient.getEarliestAD() < currentDay) {
                if (sPatient.getEarliestAD() != currentDay - 1) {
                    throw new IllegalStateException("The earliest admission day is not correct!");
                }
                sPatient.updateEarliestAdmissionDay(currentDay);
                sPatient.updatePriority();
            }
        }

        // add registered patients
        Iterator<Patient> patientIterator = patientsSet.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getDateRegister() == currentDay) {
                SPatient sPatient = new SPatient(patient);
                scheduleResult.addSPatient(sPatient);
                unadmittedPatients.add(sPatient);
                patientIterator.remove();
            }
        }

        return updateRegisteredPatients();
    }

    private boolean updateRegisteredPatients() {
        registeredPatients.clear();
        registeredPatients.addAll(unadmittedPatients);
        registeredPatients.addAll(initialPatients);
        System.out.println("Inpatient number: "+initialPatients.size() +
                ", new patient number: "+unadmittedPatients.size());
        Params.ePatientNum += registeredPatients.size();

        roomDemand = 0;
        orDemand = 0;
        for (SPatient patient : registeredPatients) {
                roomDemand += patient.getRestLOS();
                if (patient.needSurgery()) {
                    orDemand += patient.getSurDur();
            }
        }

        return registeredPatients.size() > 0;
    }

    //for dynamic version
    public void storeResult(Solution solution) {
        for (int p = 0; p < registeredPatients.size(); p++) {
            SPatient sPatient = registeredPatients.get(p);
            int roomIndex = solution.getRoomID(p);
            sPatient.setRoomIndexTemp(roomIndex);
            if (sPatient.getStatus().equals(Status.Admitted)) {
                sPatient.addRoom(roomIndex);
                sPatient.setPreRoomIndex(roomIndex);
            } else if (sPatient.getStatus().equals(Status.Registered)) {
                int delay = solution.getAdDelay(p);
                sPatient.setDelayTemp(delay);
                if (sPatient.getAdmissionDay() == current) {
                    unadmittedPatients.remove(sPatient);
                    sPatient.setStatus(Status.Admitted);
                    initialPatients.add(sPatient);
                    sPatient.addRoom(roomIndex);
                    sPatient.setPreRoomIndex(roomIndex);
                }
            } else {
                throw new IllegalStateException("The state of the patient is not correct!");
            }
        }
    }

    public void storeStaticResult(Solution solution) {
        if (Params.transfer) {
            for (int p = 0; p < registeredPatients.size(); p++) {
                SPatient patient = registeredPatients.get(p);
                if (patient.isRegistered()) {
                    patient.setDelayTemp(solution.getAdDelay(p));
                }
                for (int i = 0; i < patient.getRestLOS(); i++) {
                    patient.addRoom(solution.getRooms()[p][i]);
                }
            }
        } else {
            // currently use model without room transfer
            for (int p = 0; p < registeredPatients.size(); p++) {
                SPatient patient = registeredPatients.get(p);
                if (patient.isRegistered()) {
                    patient.setDelayTemp(solution.getAdDelay(p));
                }
                for (int i = 0; i < patient.getRestLOS(); i++) {
                    patient.addRoom(solution.getRoomID(p));
                }
            }
        }
    }

    // only for dynamic version
    public void completeSchedule(int currentDay) {
        scheduleResult.setCurrentDate(currentDay);
        for (SPatient sPatient : registeredPatients) {
            int roomIndex = sPatient.getRoomIndexTemp(), day;
            for (day = Math.max(currentDay, sPatient.getAdmissionDay());
                 day < sPatient.getDischargeDay(); day++) {
                sPatient.addRoom(roomIndex);
            }

        }


    }

    public Instance getInstance() {
        return instance;
    }

    public int getCurrent() {
        return current;
    }

    public ScheduleResult getScheduleResult() {
        return scheduleResult;
    }

    public int getRoomDemand() {
        return roomDemand;
    }

    public int getOrDemand() {
        return orDemand;
    }
}
