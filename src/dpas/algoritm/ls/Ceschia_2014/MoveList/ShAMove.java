/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.MoveList;

import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.instance.Status;
import dpas.schedule.SPatient;
import dpas.schedule.Solution;

import java.util.ArrayList;

/**
 * Created by zhuyi on 11/3/2016.
 */
public class ShAMove extends QuadMoves {
    private SPatient sPatient;
    private int delay;
    private int oldStartDay;
    private int newStartDay;
    private int oldEndDay;
    private int newEndDay;
    private int oldSurgeryDay;
    private int newSurgeryDay;

    public ShAMove(ArrayList<SPatient> unadmittedPatients) {
        super(unadmittedPatients);
    }

    public SPatient getsPatient() {
        return sPatient;
    }

    public int getDelay() {
        return delay;
    }

    public int getOldStartDay() {
        return oldStartDay;
    }

    public int getNewStartDay() {
        return newStartDay;
    }

    public int getOldEndDay() {
        return oldEndDay;
    }

    public int getNewEndDay() {
        return newEndDay;
    }

    public int getOldSurgeryDay() {
        return oldSurgeryDay;
    }

    public int getNewSurgeryDay() {
        return newSurgeryDay;
    }


    @Override
    public void search() {
        SPatient sPatient;
        int delay;
        do {
            int patientIndex = random.nextInt(getsPatientsList().size());
            sPatient = getsPatientsList().get(patientIndex);
            delay = random.nextInt(sPatient.getMaxDelay() + 1);
        } while (delay == sPatient.getDelayTemp());
        assert sPatient.getStatus().equals(Status.Registered);
        this.sPatient = sPatient;
        this.delay = delay;
        oldStartDay = sPatient.getAdmissionDay();
        newStartDay = sPatient.getEarliestAD() + delay;
        oldEndDay = sPatient.getDischargeDay();
        newEndDay = newStartDay + sPatient.getRestLOS();
        if (sPatient.needSurgery()) {
            oldSurgeryDay = sPatient.getSD();
            newSurgeryDay = newStartDay + sPatient.getPreSurgeryDay();
        }
    }

    @Override
    public void acceptMove(Solution solution) {
        sPatient.setDelayTemp(delay);
        solution.setAdmissionDelays(getsPatientsList().indexOf(sPatient), delay);
    }

    public void search(ArrayList<SPatient> patients) {
        SPatient sPatient;
        int delay;
        do {
            int patientIndex = random.nextInt(patients.size());
            sPatient = patients.get(patientIndex);
            delay = random.nextInt(sPatient.getMaxDelay() + 1);
        } while (delay == sPatient.getDelayTemp());
        assert sPatient.getStatus().equals(Status.Registered);
        this.sPatient = sPatient;
        this.delay = delay;
        oldStartDay = sPatient.getAdmissionDay();
        newStartDay = sPatient.getEarliestAD() + delay;
        oldEndDay = sPatient.getDischargeDay();
        newEndDay = newStartDay + sPatient.getRestLOS();
        if (sPatient.needSurgery()) {
            oldSurgeryDay = sPatient.getSD();
            newSurgeryDay = newStartDay + sPatient.getPreSurgeryDay();
        }
    }
}
