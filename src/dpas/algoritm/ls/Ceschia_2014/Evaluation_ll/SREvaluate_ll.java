/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_ll;

import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_l.SREvaluate_l;
import dpas.algoritm.ls.Ceschia_2014.MoveList.SRMove;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.instance.Instance;
import dpas.schedule.Solution;
import dpas.schedule.ValuesStorage;

/**
 * Created by zhuyi on 2/10/2017.
 */
public class SREvaluate_ll extends ObjCosts_ll implements Evaluation<QuadMoves> {
    private SRMove move;
    private SREvaluate_l srEvaluate_l;

    @SuppressWarnings("unused")
    public SREvaluate_ll(Instance instance, ValuesStorage vs) {
        super(instance, vs);
    }

    @SuppressWarnings("unused")
    public SREvaluate_ll(Instance instance, ValuesStorage vs, SREvaluate_l srEvaluate_l) {
        super(instance, vs);
        this.srEvaluate_l = srEvaluate_l;
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (SRMove) move;
        getObjCosts().initialize();
    }

    @Override
    public int evaluation() {
        SR_DeltaRoomCapacity();
        SR_DeltaTransfer();
        SR_DeltaRoomGender();
        SR_DeltaRoomCost();
        SR_DeltaOverCrowdRisk();
        SR_DeltaIdleRoomCost();
        if (srEvaluate_l != null) {
            srEvaluate_l.check(getObjCosts());
        }
        return getTotalCostsDelta();
    }

    @Override
    int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() + getObjCosts().getTrCostD() + getObjCosts
                ().getRoomCostD();
    }

    public void makeMove(Solution solution) {
        for (int d = move.getsPatient1().getAdmissionDay(); d < move.getsPatient1()
                .getDischargeDay(); d++) {
            getVs().getRoomStorage(move.getRoom1(), d).removePatient(move.getsPatient1());
            getVs().getRoomStorage(move.getRoom2(), d).addPatient(move.getsPatient1());
        }
        for (int d = move.getsPatient2().getAdmissionDay(); d < move.getsPatient2()
                .getDischargeDay(); d++) {
            getVs().getRoomStorage(move.getRoom2(), d).removePatient(move.getsPatient2());
            getVs().getRoomStorage(move.getRoom1(), d).addPatient(move.getsPatient2());
        }
        if (move.getsPatient1().getVariablity() > 0
                && move.getsPatient1().getDischargeDay() < getInstance().getNumDays()) {
            getVs().getRoomStorage(move.getRoom1(), move.getsPatient1().getDischargeDay())
                    .decPotentialStay();
            getVs().getRoomStorage(move.getRoom2(), move.getsPatient1().getDischargeDay())
                    .incPotentialStay();
        }
        if (move.getsPatient2().getVariablity() > 0
                && move.getsPatient2().getDischargeDay() < getInstance().getNumDays()) {
            getVs().getRoomStorage(move.getRoom2(), move.getsPatient2().getDischargeDay())
                    .decPotentialStay();
            getVs().getRoomStorage(move.getRoom1(), move.getsPatient2().getDischargeDay())
                    .incPotentialStay();
        }
        getObjCosts().updateSolTrCost(solution);
        getObjCosts().updateSolRoomCost(solution);
        getObjCosts().updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);
        move.acceptMove(solution);
    }

    @SuppressWarnings("Duplicates")
    private void SR_DeltaTransfer() {
        int cost = 0;
        int preRoom1 = move.getsPatient1().getPreRoomIndex();
        if (preRoom1 != -1) {
            if (move.getRoom2() == preRoom1) {
                cost--;
            } else if (move.getRoom1() == preRoom1) {
                cost++;
            }
        }
        int preRoom2 = move.getsPatient2().getPreRoomIndex();
        if (preRoom2 != -1) {
            if (move.getRoom1() == preRoom2) {
                cost--;
            } else if (move.getRoom2() == preRoom2) {
                cost++;
            }
        }

        getObjCosts().setTrCostD(cost);
    }

    private void SR_DeltaRoomCost() {
        int cost = 0;
        cost -= move.getsPatient1().getRoomCost(move.getRoom1())
                * (move.getsPatient1().getDischargeDay() - move.getsPatient1().getAdmissionDay());
        cost += move.getsPatient1().getRoomCost(move.getRoom2())
                * (move.getsPatient1().getDischargeDay() - move.getsPatient1().getAdmissionDay());
        cost -= move.getsPatient2().getRoomCost(move.getRoom2())
                * (move.getsPatient2().getDischargeDay() - move.getsPatient2().getAdmissionDay());
        cost += move.getsPatient2().getRoomCost(move.getRoom1())
                * (move.getsPatient2().getDischargeDay() - move.getsPatient2().getAdmissionDay());

        getObjCosts().setRoomCostD(cost);
    }

    private void SR_DeltaRoomCapacity() {
        int ORCost = 0;
        int capacity1 = getInstance().getRoomCapacity(move.getRoom1());
        int capacity2 = getInstance().getRoomCapacity(move.getRoom2());
        for (int d = move.getsPatient1().getAdmissionDay(); d < move.getsPatient1()
                .getDischargeDay(); d++) {
            if (d < move.getsPatient2().getAdmissionDay() || d >= move.getsPatient2()
                    .getDischargeDay()) {
                if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() > capacity1) {
                    ORCost--;
                }
                if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() >= capacity2) {
                    ORCost++;
                }
            }
        }

        for (int d = move.getsPatient2().getAdmissionDay(); d < move.getsPatient2()
                .getDischargeDay(); d++) {
            if (d < move.getsPatient1().getAdmissionDay() || d >= move.getsPatient1()
                    .getDischargeDay()) {
                if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() > capacity2) {
                    ORCost--;
                }
                if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() >= capacity1) {
                    ORCost++;
                }
            }
        }

        getObjCosts().setOverRCCostD(ORCost);
    }

    private void SR_DeltaRoomGender() {
        int cost = 0;
        boolean male1 = move.getsPatient1().isMale();
        boolean male2 = move.getsPatient2().isMale();
        boolean genderPolicy1 = getInstance().roomForSG(move.getRoom1());
        boolean genderPolicy2 = getInstance().roomForSG(move.getRoom2());
        for (int d = move.getsPatient1().getAdmissionDay(); d < move.getsPatient1()
                .getDischargeDay(); d++) {
            if (d < move.getsPatient2().getAdmissionDay() || d >= move.getsPatient2()
                    .getDischargeDay()) {
                if (genderPolicy1) {
                    if (getVs().getRoomStorage(move.getRoom1(), d).getMalePatients() > 0
                            && getVs().getRoomStorage(move.getRoom1(), d).getFemalePatients() > 0) {
                        if (getVs().getRoomStorage(move.getRoom1(), d).getMalePatients() <= getVs()
                                .getRoomStorage(move.getRoom1(), d).getFemalePatients() && male1
                                || (getVs().getRoomStorage(move.getRoom1(), d)
                                .getFemalePatients() <= getVs()
                                .getRoomStorage(move.getRoom1(), d).getMalePatients() && !male1)) {
                            cost--;
                        }
                    }
                }
                if (genderPolicy2) {
                    if (getVs().getRoomStorage(move.getRoom2(), d).getMalePatients() < getVs()
                            .getRoomStorage(move.getRoom2(), d).getFemalePatients() && male1
                            || (getVs().getRoomStorage(move.getRoom2(), d).getFemalePatients() <
                            getVs().
                                    getRoomStorage(move.getRoom2(), d).getMalePatients() &&
                            !male1)) {
                        cost++;
                    }
                }
            } else if (male1 ^ male2) {
                // Remove the costs of the current situation
                if (genderPolicy1) {
                    cost -= getVs().getRoomStorage(move.getRoom1(), d).getGenderPolicyConflict();
                }
                if (genderPolicy2) {
                    cost -= getVs().getRoomStorage(move.getRoom2(), d).getGenderPolicyConflict();
                }
                if (male1) {
                    if (genderPolicy1) {
                        cost += Math.min(getVs().getRoomStorage(move.getRoom1(), d)
                                        .getMalePatients() - 1,
                                getVs().getRoomStorage(move.getRoom1(), d).getFemalePatients() + 1);
                    }
                    if (genderPolicy2) {
                        cost += Math.min(getVs().getRoomStorage(move.getRoom2(), d)
                                        .getMalePatients() + 1,
                                getVs().getRoomStorage(move.getRoom2(), d).getFemalePatients() - 1);
                    }
                } else {
                    if (genderPolicy1) {
                        cost += Math.min(getVs().getRoomStorage(move.getRoom1(), d)
                                        .getMalePatients() + 1,
                                getVs().getRoomStorage(move.getRoom1(), d).getFemalePatients() - 1);
                    }
                    if (genderPolicy2) {
                        cost += Math.min(getVs().getRoomStorage(move.getRoom2(), d)
                                        .getMalePatients() - 1,
                                getVs().getRoomStorage(move.getRoom2(), d).getFemalePatients() + 1);
                    }
                }
            }
        }

        for (int d = move.getsPatient2().getAdmissionDay(); d < move.getsPatient2()
                .getDischargeDay(); d++) {
            if (d < move.getsPatient1().getAdmissionDay() || d >= move.getsPatient1()
                    .getDischargeDay()) {
                if (genderPolicy2) {
                    if (getVs().getRoomStorage(move.getRoom2(), d).getMalePatients() > 0
                            && getVs().getRoomStorage(move.getRoom2(), d).getFemalePatients() > 0) {
                        if ((getVs().getRoomStorage(move.getRoom2(), d).getMalePatients() <= getVs()
                                .getRoomStorage(move.getRoom2(), d).getFemalePatients() && male2)
                                || (getVs().getRoomStorage(move.getRoom2(), d)
                                .getFemalePatients() <= getVs().getRoomStorage(move.getRoom2(), d)
                                .getMalePatients() && !male2)) {
                            cost--;
                        }
                    }
                }
                if (genderPolicy1) {
                    if (getVs().getRoomStorage(move.getRoom1(), d).getMalePatients() < getVs()
                            .getRoomStorage(move.getRoom1(), d).getFemalePatients() && male2
                            || (getVs().getRoomStorage(move.getRoom1(), d).getFemalePatients() <
                            getVs()
                                    .getRoomStorage(move.getRoom1(), d).getMalePatients()
                            && !male2)) {
                        cost++;
                    }
                }
            }
            // else: already computed
        }

        getObjCosts().setRGCostD(cost);
    }

    private void SR_DeltaOverCrowdRisk() {
        int actual_end_day1, actual_end_day2;
        int cost = 0;

        if (move.getsPatient1().getVariablity() > 0 && move.getsPatient1()
                .getDischargeDay() < getInstance().getNumDays()) {
            actual_end_day1 = move.getsPatient1().getDischargeDay() + 1;
        } else {
            actual_end_day1 = move.getsPatient1().getDischargeDay();
        }

        if (move.getsPatient2().getVariablity() > 0 && move.getsPatient2()
                .getDischargeDay() < getInstance().getNumDays()) {
            actual_end_day2 = move.getsPatient2().getDischargeDay() + 1;
        } else {
            actual_end_day2 = move.getsPatient2().getDischargeDay();
        }
        int capacity1 = getInstance().getRoomCapacity(move.getRoom1());
        int capacity2 = getInstance().getRoomCapacity(move.getRoom2());
        for (int d = move.getsPatient1().getAdmissionDay(); d < actual_end_day1; d++) {
            if (d < move.getsPatient2().getAdmissionDay() || d >= actual_end_day2) {
                if (getVs().getRoomStorage(move.getRoom1(), d).getTotalPOccupancy() > capacity1) {
                    cost--;
                }
                if (getVs().getRoomStorage(move.getRoom2(), d).getTotalPOccupancy() >= capacity2) {
                    cost++;
                }
            }
        }


        for (int d = move.getsPatient2().getAdmissionDay(); d < actual_end_day2; d++) {
            if (d < move.getsPatient1().getAdmissionDay() || d >= actual_end_day1) {
                if (getVs().getRoomStorage(move.getRoom2(), d).getTotalPOccupancy() > capacity2) {
                    cost--;
                }
                if (getVs().getRoomStorage(move.getRoom1(), d).getTotalPOccupancy() >= capacity1) {
                    cost++;
                }
            }
        }

        getObjCosts().setPOverRCCostD(cost);
    }

    private void SR_DeltaIdleRoomCost() {
        int cost = 0;
        int capacity1 = getInstance().getRoomCapacity(move.getRoom1());
        int capacity2 = getInstance().getRoomCapacity(move.getRoom2());
        int stop = Math.min(move.getsPatient1().getDischargeDay(), getInstance()
                .getPlanningHorizon());
        for (int d = move.getsPatient1().getAdmissionDay(); d < stop; d++) {
            if (d < move.getsPatient2().getAdmissionDay() || d >= move.getsPatient2()
                    .getDischargeDay()) {
                if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() <= capacity1) {
                    cost++;
                }
                if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() < capacity2) {
                    cost--;
                }
            }
        }
        stop = Math.min(move.getsPatient2().getDischargeDay(), getInstance().getPlanningHorizon());
        for (int d = move.getsPatient2().getAdmissionDay(); d < stop; d++) {
            if (d < move.getsPatient1().getAdmissionDay() || d >= move.getsPatient1()
                    .getDischargeDay()) {
                if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() <= capacity2) {
                    cost++;
                }
                if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() < capacity1) {
                    cost--;
                }
            }
        }

        getObjCosts().setIdleRCCostD(cost);
    }
}
