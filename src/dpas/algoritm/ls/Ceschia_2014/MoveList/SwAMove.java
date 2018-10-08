/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.MoveList;

import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.schedule.SPatient;
import dpas.schedule.Solution;

import java.util.ArrayList;

/**
 * Created by zhuyi on 11/3/2016.
 */
public class SwAMove extends QuadMoves {

    private SPatient sPatient1;
    private SPatient sPatient2;
    private int room1;
    private int room2;
    private int startDay1;
    private int startDay2;

    public SwAMove(ArrayList<SPatient> registeredPatients) {
        super(registeredPatients);
    }

    public SPatient getsPatient1() {
        return sPatient1;
    }

    public SPatient getsPatient2() {
        return sPatient2;
    }

    public int getRoom1() {
        return room1;
    }

    public int getRoom2() {
        return room2;
    }

    public int getStartDay1() {
        return startDay1;
    }

    public int getStartDay2() {
        return startDay2;
    }

    @Override
    public void search() {
        SPatient sPatient1;
        int admissionDay1;
        int roomIndex1;

        SPatient sPatient2;
        int admissionDay2;
        int roomIndex2;
        do {
            int patientIndex1 = random.nextInt(getsPatientsList().size());//
            sPatient1 = getsPatientsList().get(patientIndex1);
            admissionDay1 = sPatient1.getAdmissionDay();
            roomIndex1 = sPatient1.getRoomIndexTemp();

            int patientIndex2 = random.nextInt(getsPatientsList().size());//
            sPatient2 = getsPatientsList().get(patientIndex2);
            admissionDay2 = sPatient2.getAdmissionDay();
            roomIndex2 = sPatient2.getRoomIndexTemp();
        } while ((admissionDay1 == admissionDay2 && roomIndex1 == roomIndex2)
                || !sPatient1.roomAvailable(roomIndex2) //!patient1.roomAvailability(roomIndex2)
                || !sPatient2.roomAvailable(roomIndex1) //!patient2.roomAvailability(roomIndex1)
                || admissionDay2 < sPatient1.getEarliestAD() || admissionDay2 > sPatient1.getMaxAD()
                || admissionDay1 < sPatient2.getEarliestAD() || admissionDay1 >
                sPatient2.getMaxAD());

        this.sPatient1 = sPatient1;
        this.sPatient2 = sPatient2;
        this.room1 = roomIndex1;
        this.room2 = roomIndex2;
        this.startDay1 = sPatient1.getAdmissionDay();
        this.startDay2 = sPatient2.getAdmissionDay();
    }

    @Override
    public void acceptMove(Solution solution) {
        if (room1 != room2) {
            solution.setRoomAssignments(getsPatientsList().indexOf(sPatient1), room2);
            solution.setRoomAssignments(getsPatientsList().indexOf(sPatient2), room1);

            sPatient1.setRoomIndexTemp(room2);
            sPatient2.setRoomIndexTemp(room1);
        }
        if (startDay1 != startDay2) {
            int delay1 = startDay2 - sPatient1.getEarliestAD();
            int delay2 = startDay1 - sPatient2.getEarliestAD();
            solution.setAdmissionDelays(getsPatientsList().indexOf(sPatient1), delay1);
            solution.setAdmissionDelays(getsPatientsList().indexOf(sPatient2), delay2);

            sPatient1.setDelayTemp(delay1);
            sPatient2.setDelayTemp(delay2);
        }
    }


    public void search(ArrayList<SPatient> patients) {
        SPatient sPatient1;
        int admissionDay1;
        int roomIndex1;

        SPatient sPatient2;
        int admissionDay2;
        int roomIndex2;
        do {
            int patientIndex1 = random.nextInt(patients.size());//
            sPatient1 = patients.get(patientIndex1);
            admissionDay1 = sPatient1.getAdmissionDay();
            roomIndex1 = sPatient1.getRoomIndexTemp();

            int patientIndex2 = random.nextInt(patients.size());//
            sPatient2 = patients.get(patientIndex2);
            admissionDay2 = sPatient2.getAdmissionDay();
            roomIndex2 = sPatient2.getRoomIndexTemp();
        } while ((admissionDay1 == admissionDay2 && roomIndex1 == roomIndex2)
                || !sPatient1.roomAvailable(roomIndex2) //!patient1.roomAvailability(roomIndex2)
                || !sPatient2.roomAvailable(roomIndex1) //!patient2.roomAvailability(roomIndex1)
                || admissionDay2 < sPatient1.getEarliestAD() || admissionDay2 > sPatient1.getMaxAD()
                || admissionDay1 < sPatient2.getEarliestAD() || admissionDay1 >
                sPatient2.getMaxAD());

        this.sPatient1 = sPatient1;
        this.sPatient2 = sPatient2;
        this.room1 = roomIndex1;
        this.room2 = roomIndex2;
        this.startDay1 = sPatient1.getAdmissionDay();
        this.startDay2 = sPatient2.getAdmissionDay();
    }
}
