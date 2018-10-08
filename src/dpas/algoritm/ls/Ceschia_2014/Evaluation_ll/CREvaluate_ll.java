/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_ll;


import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_l.CREvaluate_l;
import dpas.algoritm.ls.Ceschia_2014.MoveList.CRMove;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.instance.Instance;
import dpas.schedule.Solution;
import dpas.schedule.ValuesStorage;

/**
 * Created by zhuyi on 2/10/2017.
 */
public class CREvaluate_ll extends ObjCosts_ll implements Evaluation<QuadMoves> {
    private CRMove move;
    private CREvaluate_l crEvaluate_l;

    @SuppressWarnings("unused")
    public CREvaluate_ll(Instance instance, ValuesStorage vs, CREvaluate_l crEvaluate_l) {
        super(instance, vs);
        this.crEvaluate_l = crEvaluate_l;
    }

    @SuppressWarnings("unused")
    public CREvaluate_ll(Instance instance, ValuesStorage vs) {
        super(instance, vs);
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (CRMove) move;
        getObjCosts().initialize();
    }

    @Override
    public int evaluation() {
        CR_DeltaRoomCapacity();
        CR_DeltaTransfer();
        CR_DeltaRoomGender();
        CR_DeltaRoomCost();
        CR_DeltaOverCrowdRisk();
        CR_DeltaIdleRoomCost();
        if (crEvaluate_l != null) {
            crEvaluate_l.check(getObjCosts());
        }
        return getTotalCostsDelta();
    }

    @Override
    int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() +
                getObjCosts().getTrCostD() + getObjCosts().getRoomCostD();
    }

    public void makeMove(Solution solution) {
        int d;
        for (d = move.getsPatient().getAdmissionDay();
             d < move.getsPatient().getDischargeDay(); d++) {
            getVs().getRoomStorage(move.getOldRoom(), d).removePatient(move.getsPatient());
            getVs().getRoomStorage(move.getNewRoom(), d).addPatient(move.getsPatient());
        }
        if (move.getsPatient().getVariablity() > 0 && d < getInstance().getNumDays()) {
            getVs().getRoomStorage(move.getOldRoom(), d).decPotentialStay();
            getVs().getRoomStorage(move.getNewRoom(), d).incPotentialStay();
        }
        getObjCosts().updateSolTrCost(solution);
        getObjCosts().updateSolRoomCost(solution);
        getObjCosts().updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);
        move.acceptMove(solution);
    }

    private void CR_DeltaTransfer() {
        int cost = 0;
        int preRoom = move.getsPatient().getPreRoomIndex();

        if (move.getNewRoom() == preRoom) {
            cost = -1;
        } else if (move.getOldRoom() == preRoom) {
            cost = 1;
        }
        getObjCosts().setTrCostD(cost);
    }

    private void CR_DeltaRoomCost() {
        int cost = 0;
        cost -= move.getsPatient().getRoomCost(move.getOldRoom())
                * (move.getsPatient().getDischargeDay() - move.getsPatient().getAdmissionDay());
        cost += move.getsPatient().getRoomCost(move.getNewRoom())
                * (move.getsPatient().getDischargeDay() - move.getsPatient().getAdmissionDay());
        getObjCosts().setRoomCostD(cost);
    }

    private void CR_DeltaRoomCapacity() {
        int cost = 0;
        int oldCapacity = getInstance().getRoomCapacity(move.getOldRoom());
        int newCapacity = getInstance().getRoomCapacity(move.getNewRoom());
        int endDay = move.getsPatient().getDischargeDay();
        for (int d = move.getsPatient().getAdmissionDay(); d < endDay; d++) {
            int oldUsedCapacity = getVs().getRoomStorage(move.getOldRoom(), d).getOccupancy();
            int newUsedCapacity = getVs().getRoomStorage(move.getNewRoom(), d).getOccupancy();
            if (oldUsedCapacity > oldCapacity) {
                cost--;
            }
            if (newUsedCapacity >= newCapacity) {
                cost++;
            }
        }
        getObjCosts().setOverRCCostD(cost);
    }

    private void CR_DeltaRoomGender() {
        int RGCost = 0;
        boolean male = move.getsPatient().isMale();
        boolean oldGenderPolicy = getInstance().roomForSG(move.getOldRoom());
        boolean newGenderPolicy = getInstance().roomForSG(move.getNewRoom());
        for (int d = move.getsPatient().getAdmissionDay();
             d < move.getsPatient().getDischargeDay(); d++) {
            if (oldGenderPolicy) {
                if (getVs().getRoomStorage(move.getOldRoom(), d).getMalePatients() > 0 && getVs()
                        .getRoomStorage(move
                                .getOldRoom(), d).getFemalePatients() > 0) {
                    if (getVs().getRoomStorage(move.getOldRoom(), d).getMalePatients() <= getVs()
                            .getRoomStorage(move
                                    .getOldRoom(), d).getFemalePatients() &&
                            male || (getVs().getRoomStorage(move.getOldRoom(), d)
                            .getFemalePatients() <= getVs()
                            .getRoomStorage(move.getOldRoom(), d).getMalePatients() && !male)) {
                        RGCost--;
                    }
                }
            }
            if (newGenderPolicy) {
                if ((getVs().getRoomStorage(move.getNewRoom(), d).getMalePatients() < getVs()
                        .getRoomStorage(move
                                .getNewRoom(), d).getFemalePatients() &&
                        male) || (getVs().getRoomStorage(move.getNewRoom(), d).getFemalePatients
                        () < getVs()
                        .getRoomStorage(move.getNewRoom(), d).getMalePatients() && !male)) {
                    RGCost++;
                }
            }
        }
        getObjCosts().setRGCostD(RGCost);
    }

    private void CR_DeltaOverCrowdRisk() {
        int RiCost = 0;
        int oldCapacity = getInstance().getRoomCapacity(move.getOldRoom());
        int newCapacity = getInstance().getRoomCapacity(move.getNewRoom());
        int endDay = move.getsPatient().getDischargeDay();
        for (int d = move.getsPatient().getAdmissionDay(); d < endDay; d++) {
            if (getVs().getRoomStorage(move.getOldRoom(), d).getTotalPOccupancy() > oldCapacity) {
                RiCost--;
            }
            if (getVs().getRoomStorage(move.getNewRoom(), d).getTotalPOccupancy() >= newCapacity) {
                RiCost++;
            }
        }
        if (move.getsPatient().getVariablity() > 0 && endDay < getInstance().getNumDays()) {
            if (getVs().getRoomStorage(move.getOldRoom(), endDay).getTotalPOccupancy() >
                    oldCapacity) {
                RiCost--;
            }
            if (getVs().getRoomStorage(move.getNewRoom(), endDay).getTotalPOccupancy() >=
                    newCapacity) {
                RiCost++;
            }
        }

        getObjCosts().setPOverRCCostD(RiCost);
    }

    private void CR_DeltaIdleRoomCost() {
        int IRCost = 0;
        int oldCapacity = getInstance().getRoomCapacity(move.getOldRoom());
        int newCapacity = getInstance().getRoomCapacity(move.getNewRoom());
        int stop = Math.min(move.getsPatient().getDischargeDay(), getInstance()
                .getPlanningHorizon());
        for (int d = move.getsPatient().getAdmissionDay(); d < stop; d++) {
            if (getVs().getRoomStorage(move.getOldRoom(), d).getOccupancy() <= oldCapacity) {
                IRCost++;
            }
            if (getVs().getRoomStorage(move.getNewRoom(), d).getOccupancy() < newCapacity) {
                IRCost--;
            }
        }
        getObjCosts().setIdleRCCostD(IRCost);
    }
}
