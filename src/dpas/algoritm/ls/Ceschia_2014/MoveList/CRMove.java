/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.MoveList;


import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.instance.Instance;
import dpas.schedule.SPatient;
import dpas.schedule.Solution;

import java.util.ArrayList;

/**
 * Created by zhuyi on 2/8/2017.
 */
public class CRMove extends QuadMoves {
    private SPatient sPatient;
    private int oldRoom;
    private int newRoom;

    private Instance instance;

    public CRMove(Instance instance, ArrayList<SPatient> registeredPatients) {
        super(registeredPatients);
        this.instance = instance;
    }

    public SPatient getsPatient() {
        return sPatient;
    }

    public int getOldRoom() {
        return oldRoom;
    }

    public int getNewRoom() {
        return newRoom;
    }

    public void search() {
        SPatient sPatient;
        int oldRoomIndex;
        int newRoomIndex;
        do {
            int patientIndex = random.nextInt(getsPatientsList().size());
            sPatient = getsPatientsList().get(patientIndex);
            oldRoomIndex = sPatient.getRoomIndexTemp();
            newRoomIndex = random.nextInt(instance.getRoomNum());//sPatient.getFeasibleRooms()
            // .get(rand.nextInt(sPatient.getFeasibleRooms().size()));//
        } while (newRoomIndex == oldRoomIndex || !sPatient.roomAvailable(newRoomIndex));
        this.sPatient = sPatient;
        this.oldRoom = oldRoomIndex;
        this.newRoom = newRoomIndex;
    }

    @Override
    public void acceptMove(Solution solution) {
        sPatient.setRoomIndexTemp(newRoom);
        solution.setRoomAssignments(getsPatientsList().indexOf(sPatient), newRoom);
    }

    public void search(ArrayList<SPatient> patients, ArrayList<Integer> roomScope) {
        SPatient sPatient;
        int oldRoomIndex;
        int newRoomIndex;
        do {
            int patientIndex = random.nextInt(patients.size());
            sPatient = patients.get(patientIndex);
            oldRoomIndex = sPatient.getRoomIndexTemp();
            newRoomIndex = roomScope.get(random.nextInt(roomScope.size()));//sPatient
            // .getFeasibleRooms().get(rand.nextInt(sPatient.getFeasibleRooms().size()));//
        } while (newRoomIndex == oldRoomIndex || !sPatient.roomAvailable(newRoomIndex));
        this.sPatient = sPatient;
        this.oldRoom = oldRoomIndex;
        this.newRoom = newRoomIndex;
    }
}
