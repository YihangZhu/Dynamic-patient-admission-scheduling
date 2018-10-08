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
public class SRMove extends QuadMoves {
    private SPatient sPatient1;
    private SPatient sPatient2;
    private int room1;
    private int room2;

    public SRMove(ArrayList<SPatient> registeredPatients) {
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

    @Override
    public void search() {
        SPatient sPatient1;
        SPatient sPatient2;
        int roomIndex1;
        int roomIndex2;
        do {
            int patientIndex1 = random.nextInt(getsPatientsList().size());
            sPatient1 = getsPatientsList().get(patientIndex1);
            roomIndex1 = sPatient1.getRoomIndexTemp();
            int patientIndex2 = random.nextInt(getsPatientsList().size());
            sPatient2 = getsPatientsList().get(patientIndex2);
            roomIndex2 = sPatient2.getRoomIndexTemp();
        } while (roomIndex1 == roomIndex2
                || !sPatient1.roomAvailable(roomIndex2)
                || !sPatient2.roomAvailable(roomIndex1));
        this.sPatient1 = sPatient1;
        this.sPatient2 = sPatient2;
        this.room1 = roomIndex1;
        this.room2 = roomIndex2;

    }

    @Override
    public void acceptMove(Solution solution) {
        solution.setRoomAssignments(getsPatientsList().indexOf(sPatient1), room2);
        solution.setRoomAssignments(getsPatientsList().indexOf(sPatient2), room1);

        sPatient1.setRoomIndexTemp(room2);
        sPatient2.setRoomIndexTemp(room1);
    }

    public void search(ArrayList<SPatient> patients) {
        SPatient sPatient1;
        SPatient sPatient2;
        int roomIndex1;
        int roomIndex2;
        do {
            sPatient1 = patients.get(random.nextInt(patients.size()));
            roomIndex1 = sPatient1.getRoomIndexTemp();
            sPatient2 = patients.get(random.nextInt(patients.size()));
            roomIndex2 = sPatient2.getRoomIndexTemp();
        } while (roomIndex1 == roomIndex2
                || !sPatient1.roomAvailable(roomIndex2)
                || !sPatient2.roomAvailable(roomIndex1));
        this.sPatient1 = sPatient1;
        this.sPatient2 = sPatient2;
        this.room1 = roomIndex1;
        this.room2 = roomIndex2;
    }
}
