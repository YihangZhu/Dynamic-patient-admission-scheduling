/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_ll;

import dpas.Params;
import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_l.ShAEvaluate_l;
import dpas.algoritm.ls.Ceschia_2014.MoveList.ShAMove;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.instance.Instance;
import dpas.schedule.Solution;
import dpas.schedule.ValuesStorage;

/**
 * Created by zhuyi on 2/10/2017.
 */
public class ShAEvaluate_ll extends ObjCosts_ll implements Evaluation<QuadMoves> {
    private ShAMove move;
    private ShAEvaluate_l shAEvaluate_l;
    private int remove_start_day, remove_end_day, add_start_day, add_end_day;

    @SuppressWarnings("unused")
    public ShAEvaluate_ll(Instance instance, ValuesStorage vs) {
        super(instance, vs);
    }

    @SuppressWarnings("unused")
    public ShAEvaluate_ll(Instance instance, ValuesStorage vs, ShAEvaluate_l shAEvaluate_l) {
        super(instance, vs);
        this.shAEvaluate_l = shAEvaluate_l;
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (ShAMove) move;
        getObjCosts().initialize();
    }

    @Override
    public int evaluation() {
        setDays();
        ShA_DeltaRoomCapacity();
        ShA_DeltaRoomGender();
        ShA_DeltaDelayCost();
        ShA_DeltaOverCrowdRisk();
        ShA_DeltaORCapacity();
        ShA_DeltaOvertime();
        ShA_DeltaIdleOR();
        ShA_DeltaIdleRoomCost();
        if (shAEvaluate_l != null) {
            shAEvaluate_l.check(getObjCosts());
        }
        return getTotalCostsDelta();
    }

    private void setDays() {
        int LOS = move.getsPatient().getRestLOS();
        if (move.getDelay() > move.getsPatient().getDelayTemp()) {
            remove_start_day = move.getOldStartDay();
            remove_end_day = Math.min(move.getOldStartDay() + LOS, move.getNewStartDay());
            add_start_day = Math.max(move.getOldStartDay() + LOS, move.getNewStartDay());
            add_end_day = move.getNewStartDay() + LOS;
        } else {
            remove_start_day = Math.max(move.getNewStartDay() + LOS, move.getOldStartDay());
            remove_end_day = move.getOldStartDay() + LOS;
            add_start_day = move.getNewStartDay();
            add_end_day = Math.min(move.getNewStartDay() + LOS, move.getOldStartDay());
        }
    }


    int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() + getObjCosts().getTotalORCostsDelta() +
                getObjCosts().getDeCostD();
    }

    @Override
    public void makeMove(Solution solution) {
        for (int d = remove_start_day; d < remove_end_day; d++) {
            getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).removePatient(move
                    .getsPatient());
        }

        for (int d = add_start_day; d < add_end_day; d++) {
            getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).addPatient(move
                    .getsPatient());
        }

        if (move.getsPatient().getVariablity() > 0) {
            if (move.getNewEndDay() < getInstance().getNumDays()) {
                getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), move.getNewEndDay()
                ).incPotentialStay();
            }
            if (move.getOldEndDay() < getInstance().getNumDays()) {
                getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), move.getOldEndDay()
                ).decPotentialStay();
            }
        }

        if (move.getsPatient().needSurgery()) {
            int surgeryDuration = move.getsPatient().getSurDur();
            getVs().getORStorage(move.getOldSurgeryDay()).decORDayOccupancy(surgeryDuration);
            getVs().getORStorage(move.getNewSurgeryDay()).incORDayOccupancy(surgeryDuration);
            //      if (in.GetPatient(mv.patient)->Elective())  Patients is surely elective (no
            // need to check)
            int spec = getInstance().getSpecialism(move.getsPatient().getNumber());
            getVs().getORStorage(move.getOldSurgeryDay()).getSpecOR(spec).decOccupancy
                    (surgeryDuration);
            getVs().getORStorage(move.getNewSurgeryDay()).getSpecOR(spec).incOccupancy
                    (surgeryDuration);
        }

        getObjCosts().updateSolDeCost(solution);
        if (move.getsPatient().needSurgery()) {
            getObjCosts().updateSolORRCosts(solution);
        }
        getObjCosts().updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);
        move.acceptMove(solution);
    }


    private void ShA_DeltaDelayCost() {
        int cost = move.getsPatient().getPriority() * (move.getDelay() - move.getsPatient()
                .getDelayTemp());
        getObjCosts().setDeCostD(cost);
    }

    private void ShA_DeltaRoomCapacity() {
        int cost = 0;

        int capacity = getInstance().getRooms(move.getsPatient().getRoomIndexTemp()).getCapacity();
        for (int d = remove_start_day; d < remove_end_day; d++) {
            if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).getOccupancy() >
                    capacity) {
                cost--;
            }
        }

        for (int d = add_start_day; d < add_end_day; d++) {
            if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).getOccupancy()
                    >= capacity) {
                cost++;
            }
        }
        getObjCosts().setOverRCCostD(cost);
    }

    private void ShA_DeltaRoomGender() {
        int cost = 0;
        int d;
        boolean male = move.getsPatient().isMale();
        if (getInstance().roomForSG(move.getsPatient().getRoomIndexTemp())) {
            for (d = remove_start_day; d < remove_end_day; d++) {
                if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d)
                        .getMalePatients() > 0 && getVs().getRoomStorage(move.getsPatient()
                        .getRoomIndexTemp(), d).getFemalePatients() > 0) {
                    if ((getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d)
                            .getMalePatients() <= getVs().getRoomStorage(move.getsPatient()
                            .getRoomIndexTemp(), d).getFemalePatients() && male) || (getVs()
                            .getRoomStorage(move.getsPatient().getRoomIndexTemp(), d)
                            .getFemalePatients() <= getVs().getRoomStorage(move.getsPatient()
                            .getRoomIndexTemp(), d).getMalePatients() && !male)) {
                        cost--;
                    }
                }
            }
            for (d = add_start_day; d < add_end_day; d++) {
                if ((getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d)
                        .getMalePatients() < getVs().getRoomStorage(move.getsPatient()
                        .getRoomIndexTemp(), d).getFemalePatients() && male) || (getVs()
                        .getRoomStorage(move.getsPatient().getRoomIndexTemp(), d)
                        .getFemalePatients() < getVs().getRoomStorage(move.getsPatient()
                        .getRoomIndexTemp(), d).getMalePatients() && !male)) {
                    cost++;
                }
            }
        }

        getObjCosts().setRGCostD(cost);
    }

    private void ShA_DeltaOverCrowdRisk() {

        int remove_start_day, remove_end_day, add_start_day, add_end_day;
        int cost = 0;
        int LOS = move.getsPatient().getRestLOS();
        if (move.getDelay() > move.getsPatient().getDelayTemp()) {
            remove_start_day = move.getOldStartDay();
            if (move.getsPatient().getVariablity() > 0) {
                remove_end_day = Math.min(move.getOldStartDay() + LOS + 1, move.getNewStartDay());
                add_start_day = Math.max(move.getOldStartDay() + LOS + 1, move.getNewStartDay());
                add_end_day = move.getNewStartDay() + LOS + 1;
            } else {
                remove_end_day = Math.min(move.getOldStartDay() + LOS, move.getNewStartDay());
                add_start_day = Math.max(move.getOldStartDay() + LOS, move.getNewStartDay());
                add_end_day = move.getNewStartDay() + LOS;
            }
        } else {
            add_start_day = move.getNewStartDay();
            if (move.getsPatient().getVariablity() > 0) {
                remove_start_day = Math.max(move.getNewStartDay() + LOS + 1, move.getOldStartDay());
                remove_end_day = move.getOldStartDay() + LOS + 1;
                add_end_day = Math.min(move.getNewStartDay() + LOS + 1, move.getOldStartDay());
            } else {
                remove_start_day = Math.max(move.getNewStartDay() + LOS, move.getOldStartDay());
                remove_end_day = move.getOldStartDay() + LOS;
                add_end_day = Math.min(move.getNewStartDay() + LOS, move.getOldStartDay());
            }
        }

        add_end_day = Math.min(getInstance().getNumDays(), add_end_day); //in case of
        // max_admission_day
        remove_end_day = Math.min(getInstance().getNumDays(), remove_end_day);
        int capacity = getInstance().getRoomCapacity(move.getsPatient().getRoomIndexTemp());
        for (int d = remove_start_day; d < remove_end_day; d++) {
            if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d)
                    .getTotalPOccupancy() > capacity) {
                cost--;
            }
        }

        for (int d = add_start_day; d < add_end_day; d++) {
            if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).
                    getTotalPOccupancy() >= capacity) {
                cost++;
            }
        }
        getObjCosts().setPOverRCCostD(cost);
    }

    private void ShA_DeltaIdleRoomCost() {
        int cost = 0;
        int capacity = getInstance().getRoomCapacity(move.getsPatient().getRoomIndexTemp());
        int stop = Math.min(remove_end_day, getInstance().getPlanningHorizon());
        for (int d = remove_start_day; d < stop; d++) {
            if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).getOccupancy()
                    <= capacity) {
                cost++;
            }
        }
        stop = Math.min(add_end_day, getInstance().getPlanningHorizon());
        for (int d = add_start_day; d < stop; d++) {
            if (getVs().getRoomStorage(move.getsPatient().getRoomIndexTemp(), d).
                    getOccupancy() < capacity) {
                cost--;
            }
        }

        getObjCosts().setIdleRCCostD(cost);
    }

    private void ShA_DeltaORCapacity() {
        if (move.getsPatient().needSurgery()) {
            int ORU = 0;
            int ORTU = 0;
            int spec = getInstance().getSpecialism(move.getsPatient().getNumber());
            int surgeryDuration = move.getsPatient().getSurDur();
            int oldORUsedSpecCapacity = getVs().getORStorage(move.getOldSurgeryDay()).
                    getSpecOR(spec).getOccupancy();
            int oldORSpecCapacity = getInstance().getORSpecCapacity(move.getOldSurgeryDay(), spec);
            int newORSpecCapacity = getInstance().getORSpecCapacity(move.getNewSurgeryDay(), spec);
            int newORUsedSpecCapacity = getVs().getORStorage(move.getNewSurgeryDay()).
                    getSpecOR(spec).getOccupancy();
            int oldORUsedDayCapacity = getVs().getORStorage(move.getOldSurgeryDay()).getOccupancy();
            int oldORDayCapacity = getInstance().getORDayCapacity(move.getOldSurgeryDay());
            int newORUsedDayCapacity = getVs().getORStorage(move.getNewSurgeryDay()).getOccupancy();
            int newORDayCapacity = getInstance().getORDayCapacity(move.getNewSurgeryDay());

            if (oldORUsedSpecCapacity > oldORSpecCapacity + getVs().getORStorage(
                    move.getOldSurgeryDay()).getSpecOR(spec).getAdmittedOverTime()) {
                ORU -= Math.min(oldORUsedSpecCapacity - oldORSpecCapacity - getVs().getORStorage(
                        move.getOldSurgeryDay()).getSpecOR(spec).getAdmittedOverTime(),
                        surgeryDuration);
            }
            if (newORUsedSpecCapacity + surgeryDuration > newORSpecCapacity + getVs().getORStorage(
                    move.getNewSurgeryDay()).getSpecOR(spec).getAdmittedOverTime()) {
                ORU += Math.min(newORUsedSpecCapacity + surgeryDuration - newORSpecCapacity -
                                getVs().getORStorage(
                                        move.getNewSurgeryDay()).getSpecOR(spec)
                                        .getAdmittedOverTime(),
                        surgeryDuration);
            }

            if (oldORUsedDayCapacity > oldORDayCapacity + admittedTotalOvertime()) {
                ORTU -= Math.min(oldORUsedDayCapacity - oldORDayCapacity -
                        admittedTotalOvertime(), surgeryDuration);
            }
            if (newORUsedDayCapacity + surgeryDuration > newORDayCapacity + admittedTotalOvertime
                    ()) {
                ORTU += Math.min(newORUsedDayCapacity + surgeryDuration -
                        newORDayCapacity - admittedTotalOvertime(), surgeryDuration);
            }

            getObjCosts().setORUCostD(ORU);
            getObjCosts().setORTUCostD(ORTU);
        }
    }

    private void ShA_DeltaOvertime() {

        if (move.getsPatient().needSurgery()) {
            int ORO = 0, ORTO = 0, spec = getInstance().getSpecialism(move.getsPatient()
                    .getNumber());
            int surgeryDuration = move.getsPatient().getSurDur();
            int oldORSpecCapacity = getInstance().getORSpecCapacity(move.getOldSurgeryDay(), spec);
            int newORSpecCapacity = getInstance().getORSpecCapacity(move.getNewSurgeryDay(), spec);
            int newORUsedSpecCapacity = getVs().getORStorage(move.getNewSurgeryDay()).
                    getSpecOR(spec).getOccupancy();
            int oldORUsedDayCapacity = getVs().getORStorage(move.getOldSurgeryDay()).getOccupancy();
            int oldORDayCapacity = getInstance().getORDayCapacity(move.getOldSurgeryDay());
            int newORUsedDayCapacity = getVs().getORStorage(move.getNewSurgeryDay()).getOccupancy();
            int newORDayCapacity = getInstance().getORDayCapacity(move.getNewSurgeryDay());

            int old_surgery_spec_ass = getVs().getORStorage(move.getOldSurgeryDay()).
                    getSpecOR(spec).getOccupancy(),
                    new_surgery_spec_ass = newORUsedSpecCapacity,
                    old_surgery_day_ass = oldORUsedDayCapacity,
                    new_surgery_day_ass = newORUsedDayCapacity;

            if (old_surgery_spec_ass > oldORSpecCapacity) {
                ORO -= Math.min(old_surgery_spec_ass - oldORSpecCapacity, getVs().
                        getORStorage(move.getOldSurgeryDay()).getSpecOR(spec).getAdmittedOverTime
                        ());
            }
            if (new_surgery_spec_ass > newORSpecCapacity) {
                ORO -= Math.min(new_surgery_spec_ass - newORSpecCapacity,
                        getVs().getORStorage(move.getNewSurgeryDay()).
                                getSpecOR(spec).getAdmittedOverTime());
            }

            if (old_surgery_day_ass > oldORDayCapacity) {
                ORTO -= Math.min(old_surgery_day_ass - oldORDayCapacity, admittedTotalOvertime());
            }
            if (new_surgery_day_ass > newORDayCapacity) {
                ORTO -= Math.min(new_surgery_day_ass - newORDayCapacity, admittedTotalOvertime());
            }

            old_surgery_spec_ass = old_surgery_spec_ass - surgeryDuration;
            new_surgery_spec_ass = new_surgery_spec_ass + surgeryDuration;
            old_surgery_day_ass = old_surgery_day_ass - surgeryDuration;
            new_surgery_day_ass = new_surgery_day_ass + surgeryDuration;

            if (old_surgery_spec_ass > oldORSpecCapacity) {
                ORO += Math.min(old_surgery_spec_ass - oldORSpecCapacity, getVs().
                        getORStorage(move.getOldSurgeryDay()).
                        getSpecOR(spec).getAdmittedOverTime());
            }
            if (new_surgery_spec_ass > newORSpecCapacity) {
                ORO += Math.min(new_surgery_spec_ass - newORSpecCapacity, getVs().
                        getORStorage(move.getNewSurgeryDay()).getSpecOR(spec).
                        getAdmittedOverTime());
            }
            if (old_surgery_day_ass > oldORDayCapacity) {
                ORTO += Math.min(old_surgery_day_ass - oldORDayCapacity,
                        admittedTotalOvertime());
            }
            if (new_surgery_day_ass > newORDayCapacity) {
                ORTO += Math.min(new_surgery_day_ass - newORDayCapacity, admittedTotalOvertime());
            }

            getObjCosts().setOROCostD(ORO);
            getObjCosts().setORTOCostD(ORTO);
        }


    }

    private int admittedTotalOvertime() {
        return Params.ADMITTED_TOTAL_OVERTIME;
    }

    private void ShA_DeltaIdleOR() {
        if (move.getsPatient().needSurgery()) {
            int cost = 0;
            int surgeryDuration = move.getsPatient().getSurDur();
            if (move.getOldSurgeryDay() < getInstance().getPlanningHorizon()
                    && getVs().getORStorage(move.getOldSurgeryDay()).
                    getOccupancy() - surgeryDuration <
                    getInstance().getORDayCapacity(move.getOldSurgeryDay())) {
                cost += Math.min(getInstance().getORDayCapacity(move.getOldSurgeryDay()) - getVs().
                                getORStorage(move.getOldSurgeryDay()).getOccupancy() +
                                surgeryDuration,
                        surgeryDuration);
            }
            if (move.getNewSurgeryDay() < getInstance().getPlanningHorizon() &&
                    getVs().getORStorage(move.getNewSurgeryDay()).getOccupancy() <
                            getInstance().getORDayCapacity(move.getNewSurgeryDay())) {
                cost -= Math.min(getInstance().getORDayCapacity(move.getNewSurgeryDay()) -
                                getVs().getORStorage(move.getNewSurgeryDay()).getOccupancy(),
                        surgeryDuration);
            }

            getObjCosts().setIORCostD(cost);
        }
    }
}
