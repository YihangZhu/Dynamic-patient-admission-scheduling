/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_ll;

import dpas.Params;
import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_l.SwAEvaluate_l;
import dpas.algoritm.ls.Ceschia_2014.MoveList.SwAMove;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.instance.Instance;
import dpas.schedule.Solution;
import dpas.schedule.ValuesStorage;

/**
 * Created by zhuyi on 2/10/2017.
 */
@SuppressWarnings("all")
public class SwAEvaluate_ll extends ObjCosts_ll implements Evaluation<QuadMoves> {
    private SwAMove move;
    private SwAEvaluate_l swAEvaluate_l;
    private int old_end_day1;
    private int old_end_day2;

    @SuppressWarnings("unused")
    public SwAEvaluate_ll(Instance instance, ValuesStorage vs) {
        super(instance, vs);
    }

    @SuppressWarnings("unused")
    public SwAEvaluate_ll(Instance instance, ValuesStorage vs, SwAEvaluate_l swAEvaluate_l) {
        super(instance, vs);
        this.swAEvaluate_l = swAEvaluate_l;
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (SwAMove) move;
        old_end_day1 = this.move.getsPatient1().getDischargeDay();
        old_end_day2 = this.move.getsPatient2().getDischargeDay();
        getObjCosts().initialize();
    }

    public int evaluation() {
        SwA_DeltaRoomCapacity();
        SwA_DeltaTransfer();
        SwA_DeltaRoomGender();
        SwA_DeltaRoomCost();
        SwA_DeltaDelayCost();
        SwA_DeltaOverCrowdRisk();
        SwA_DeltaORCapacity();
        SwA_DeltaOvertime();
        SwA_DeltaIdleOR();
        SwA_DeltaIdleRoomCost();
        if (swAEvaluate_l != null) {
            swAEvaluate_l.check(getObjCosts());
        }
        return getTotalCostsDelta();
    }

    @Override
    int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() + getObjCosts().getTotalORCostsDelta() +
                getObjCosts().getDeCostD() + getObjCosts().getTrCostD() + getObjCosts()
                .getRoomCostD();
    }

    public void makeMove(Solution solution) {
        int new_end_day1, new_end_day2;
        int LOS1 = move.getsPatient1().getRestLOS();
        int LOS2 = move.getsPatient2().getRestLOS();

        new_end_day1 = move.getStartDay2() + LOS1;
        new_end_day2 = move.getStartDay1() + LOS2;

        for (int d = move.getStartDay1(); d < Math.max(old_end_day1, new_end_day2); d++) {
            if (d < old_end_day1) {
                getVs().getRoomStorage(move.getRoom1(), d).removePatient(move.getsPatient1());
            }

            if (d < new_end_day2) {
                getVs().getRoomStorage(move.getRoom1(), d).addPatient(move.getsPatient2());
            }
        }

        for (int d = move.getStartDay2(); d < Math.max(old_end_day2, new_end_day1); d++) {
            if (d < old_end_day2) {
                getVs().getRoomStorage(move.getRoom2(), d).removePatient(move.getsPatient2());
            }
            if (d < new_end_day1) {
                getVs().getRoomStorage(move.getRoom2(), d).addPatient(move.getsPatient1());
            }
        }

        if (move.getsPatient1().getVariablity() > 0) {
            if (new_end_day1 < getInstance().getNumDays()) {
                getVs().getRoomStorage(move.getRoom2(), new_end_day1).incPotentialStay();
            }
            if (old_end_day1 < getInstance().getNumDays()) {
                getVs().getRoomStorage(move.getRoom1(), old_end_day1).decPotentialStay();
            }
        }

        if (move.getsPatient2().getVariablity() > 0) {
            if (new_end_day2 < getInstance().getNumDays()) {
                getVs().getRoomStorage(move.getRoom1(), new_end_day2).incPotentialStay();
            }
            if (old_end_day2 < getInstance().getNumDays()) {
                getVs().getRoomStorage(move.getRoom2(), old_end_day2).decPotentialStay();
            }
        }

        if (move.getStartDay1() != move.getStartDay2()) {
            int old_surgery_day1, old_surgery_day2, new_surgery_day1, new_surgery_day2;

            if (move.getsPatient1().needSurgery()) {
                old_surgery_day1 = move.getsPatient1().getSD();
                new_surgery_day1 = move.getStartDay2() + move.getsPatient1().getPreSurgeryDay();
                getVs().getORStorage(old_surgery_day1).decORDayOccupancy(
                        move.getsPatient1().getSurDur());
                getVs().getORStorage(new_surgery_day1).incORDayOccupancy(
                        move.getsPatient1().getSurDur());
                if (move.getsPatient1().isElective()) {
                    int spec = move.getsPatient1().getSpec();
                    getVs().getORStorage(old_surgery_day1).getSpecOR(spec).decOccupancy(
                            move.getsPatient1().getSurDur());
                    getVs().getORStorage(new_surgery_day1).getSpecOR(spec).incOccupancy(
                            move.getsPatient1().getSurDur());
                }
            }

            if (move.getsPatient2().needSurgery()) {
                old_surgery_day2 = move.getsPatient2().getSD();
                new_surgery_day2 = move.getStartDay1() + move.getsPatient2().getPreSurgeryDay();
                getVs().getORStorage(old_surgery_day2).decORDayOccupancy(
                        move.getsPatient2().getSurDur());
                getVs().getORStorage(new_surgery_day2).incORDayOccupancy(
                        move.getsPatient2().getSurDur());
                if (move.getsPatient2().isElective()) {
                    int spec = move.getsPatient2().getSpec();
                    getVs().getORStorage(old_surgery_day2).getSpecOR(spec).decOccupancy(
                            move.getsPatient2().getSurDur()
                    );
                    getVs().getORStorage(new_surgery_day2).getSpecOR(spec).incOccupancy(
                            move.getsPatient2().getSurDur());
                }
            }

        }

        if (move.getRoom1() != move.getRoom2()) {
            getObjCosts().updateSolTrCost(solution);
            getObjCosts().updateSolRoomCost(solution);
        }
        if (move.getStartDay1() != move.getStartDay2()) {
            getObjCosts().updateSolDeCost(solution);
            getObjCosts().updateSolORRCosts(solution);
        }
        getObjCosts().updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);
        move.acceptMove(solution);
    }

    private void SwA_DeltaTransfer() {
        int cost = 0;
        if (move.getRoom1() != move.getRoom2()) {
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
        }

        getObjCosts().setTrCostD(cost);
    }

    private void SwA_DeltaRoomCost() {
        int cost = 0;
        if (move.getRoom1() != move.getRoom2()) {
            cost -= move.getsPatient1().getRoomCost(move.getRoom1()) * (old_end_day1 - move
                    .getStartDay1());
            cost += move.getsPatient1().getRoomCost(move.getRoom2()) * (old_end_day1 - move
                    .getStartDay1());
            cost -= move.getsPatient2().getRoomCost(move.getRoom2()) * (old_end_day2 - move
                    .getStartDay2());
            cost += move.getsPatient2().getRoomCost(move.getRoom1()) * (old_end_day2 - move
                    .getStartDay2());
        }
        getObjCosts().setRoomCostD(cost);
    }

    private void SwA_DeltaDelayCost() {
        int cost = move.getsPatient1().getPriority() * (move.getStartDay2() - move.getStartDay1())
                + move.getsPatient2().getPriority() * (move.getStartDay1() - move.getStartDay2());
        getObjCosts().setDeCostD(cost);
    }

    private void SwA_DeltaRoomCapacity() {
        int cost = 0;
        int newEndDay1, newEndDay2;
        // when the patient is admitted, the rest of stay is not equal (should be less) with LOS.
        newEndDay1 = move.getStartDay2() + move.getsPatient1().getRestLOS();
        newEndDay2 = move.getStartDay1() + move.getsPatient2().getRestLOS();
        int capacity1 = getInstance().getRoomCapacity(move.getRoom1());
        int capacity2 = getInstance().getRoomCapacity(move.getRoom2());
        if (move.getRoom1() == move.getRoom2()) {
            int first_day = Math.min(move.getStartDay1(), move.getStartDay2()),
                    last_stay = Math.max(Math.max(old_end_day1, old_end_day2), Math.max
                            (newEndDay1, newEndDay2));

            int[] room_assignments = new int[last_stay - first_day];
            for (int d = first_day; d < last_stay; d++) {
                // current cost
                int d1 = d - first_day; //shifted day
                room_assignments[d1] = getVs().getRoomStorage(move.getRoom1(), d).getOccupancy();
                if (room_assignments[d1] > capacity1) {
                    cost -= room_assignments[d1] - capacity1;
                }
                // new cost
                // remove the patients
                if (d >= move.getStartDay1() && d < old_end_day1) {
                    room_assignments[d1]--;
                }
                if (d >= move.getStartDay2() && d < old_end_day2) {
                    room_assignments[d1]--;
                }
                //shift the patients and insert the room_assignments
                if (d >= move.getStartDay2() && d < newEndDay1) {
                    room_assignments[d1]++;
                }
                if (d >= move.getStartDay1() && d < newEndDay2) {
                    room_assignments[d1]++;
                }
                // compute the cost
                if (room_assignments[d1] > capacity1) {
                    cost += room_assignments[d1] - capacity1;
                }
            }
        } else {
            for (int d = Math.min(old_end_day1, newEndDay2); d < Math.max(old_end_day1,
                    newEndDay2); d++) {
                if (newEndDay2 < old_end_day1) {
                    if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() > capacity1) {
                        cost--;
                    }
                }

                if (newEndDay2 > old_end_day1) {
                    if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() >= capacity1) {
                        cost++;
                    }
                }
            }

            for (int d = Math.min(old_end_day2, newEndDay1); d < Math.max(old_end_day2,
                    newEndDay1); d++) {
                if (newEndDay1 < old_end_day2) {
                    if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() > capacity2) {
                        cost--;
                    }
                }
                if (newEndDay1 > old_end_day2) {
                    if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() >= capacity2) {
                        cost++;
                    }
                }
            }
        }
        getObjCosts().setOverRCCostD(cost);
    }

    private void SwA_DeltaRoomGender() {
        int cost = 0;
        int new_end_day1, new_end_day2;

        new_end_day1 = move.getStartDay2() + move.getsPatient1().getRestLOS();
        new_end_day2 = move.getStartDay1() + move.getsPatient2().getRestLOS();
        boolean gender1 = move.getsPatient1().isMale();
        boolean gender2 = move.getsPatient2().isMale();
        boolean genderPolicy1 = getInstance().roomForSG(move.getRoom1());
        boolean genderPolicy2 = getInstance().roomForSG(move.getRoom2());
        if (move.getRoom1() == move.getRoom2() && genderPolicy1) {
            int first_day = Math.min(move.getStartDay1(), move.getStartDay2()),
                    last_stay = Math.max(Math.max(old_end_day1, old_end_day2), Math.max
                            (new_end_day1, new_end_day2));

            int[] room_assignment_female = new int[last_stay - first_day];
            int[] room_assignment_male = new int[last_stay - first_day];

            for (int d = first_day; d < last_stay; d++) {
                // current cost
                int d1 = d - first_day; //shifted day
                room_assignment_female[d1] = getVs().getRoomStorage(move.getRoom1(), d)
                        .getFemalePatients();
                room_assignment_male[d1] = getVs().getRoomStorage(move.getRoom1(), d)
                        .getMalePatients();
                cost -= Math.min(room_assignment_male[d1], room_assignment_female[d1]);
                // new cost
                if (gender1) {
                    if (d >= move.getStartDay1() && d < old_end_day1) {
                        room_assignment_male[d1]--;
                    }
                    if (d >= move.getStartDay2() && d < new_end_day1) {
                        room_assignment_male[d1]++;
                    }
                } else {
                    if (d >= move.getStartDay1() && d < old_end_day1) {
                        room_assignment_female[d1]--;
                    }
                    if (d >= move.getStartDay2() && d < new_end_day1) {
                        room_assignment_female[d1]++;
                    }
                }

                if (gender2) {
                    if (d >= move.getStartDay2() && d < old_end_day2) {
                        room_assignment_male[d1]--;
                    }
                    if (d >= move.getStartDay1() && d < new_end_day2) {
                        room_assignment_male[d1]++;
                    }
                } else {
                    if (d >= move.getStartDay2() && d < old_end_day2) {
                        room_assignment_female[d1]--;
                    }
                    if (d >= move.getStartDay1() && d < new_end_day2) {
                        room_assignment_female[d1]++;
                    }
                }
                // compute the cost
                cost += Math.min(room_assignment_male[d1], room_assignment_female[d1]);
            }
        } else {
            for (int d = move.getStartDay1(); d < Math.max(old_end_day1, new_end_day2); d++) {
                if (d >= new_end_day2) {
                    if (genderPolicy1) {
                        if (getVs().getRoomStorage(move.getRoom1(), d).getMalePatients() > 0
                                && getVs().getRoomStorage(move.getRoom1(), d).getFemalePatients()
                                > 0) {
                            if ((getVs().getRoomStorage(move.getRoom1(), d).getMalePatients() <=
                                    getVs()
                                            .getRoomStorage(move.getRoom1(), d).getFemalePatients
                                            () &&
                                    gender1)
                                    || (getVs().getRoomStorage(move.getRoom1(), d)
                                    .getFemalePatients() <= getVs()
                                    .getRoomStorage(move.getRoom1(), d).getMalePatients() &&
                                    !gender1)) {
                                cost--;
                            }
                        }
                    }
                }

                if (d >= old_end_day1) {
                    if (genderPolicy1) {
                        if (getVs().getRoomStorage(move.getRoom1(), d).getMalePatients() < getVs
                                ().getRoomStorage
                                (move.getRoom1(), d).getFemalePatients()
                                && gender2
                                || (getVs().getRoomStorage(move.getRoom1(), d).getFemalePatients
                                () < getVs()
                                .getRoomStorage(move.getRoom1(), d).getMalePatients()
                                && !gender2)) {
                            cost++;
                        }
                    }

                }

                if ((gender1 ^ gender2) && genderPolicy1) {
                    if (d < Math.min(old_end_day1, new_end_day2)) {
                        // Remove the costs of the current situation
                        cost -= getVs().getRoomStorage(move.getRoom1(), d)
                                .getGenderPolicyConflict();
                        if (gender1) {
                            cost += Math.min(getVs().getRoomStorage(move.getRoom1(), d)
                                    .getMalePatients() - 1, getVs().getRoomStorage(move.getRoom1
                                    (), d)
                                    .getFemalePatients() + 1);
                        } else {// same with male/female swapped
                            cost += Math.min(getVs().getRoomStorage(move.getRoom1(), d)
                                    .getMalePatients() + 1, getVs().getRoomStorage(move.getRoom1
                                    (), d)
                                    .getFemalePatients() - 1);
                        }
                    }
                }
            }

            for (int d = move.getStartDay2(); d < Math.max(old_end_day2, new_end_day1); d++) {
                if (d >= new_end_day1) {
                    if (genderPolicy2) {
                        if (getVs().getRoomStorage(move.getRoom2(), d).getMalePatients() > 0 &&
                                getVs()
                                        .getRoomStorage(move.getRoom2(), d).getFemalePatients() >
                                        0) {
                            if ((getVs().getRoomStorage(move.getRoom2(), d).getMalePatients() <=
                                    getVs()
                                            .getRoomStorage(move.getRoom2(), d).getFemalePatients
                                            () &&
                                    gender2)
                                    || (getVs().getRoomStorage(move.getRoom2(), d)
                                    .getFemalePatients() <= getVs()
                                    .getRoomStorage(move.getRoom2(), d).getMalePatients() &&
                                    !gender2)) {
                                cost--;
                            }
                        }
                    }
                }

                if (d >= old_end_day2) {
                    if (genderPolicy2) {
                        if ((getVs().getRoomStorage(move.getRoom2(), d).getMalePatients() < getVs()
                                .getRoomStorage(move.getRoom2(), d).getFemalePatients() && gender1)
                                || (getVs().getRoomStorage(move.getRoom2(), d).getFemalePatients
                                () < getVs()
                                .getRoomStorage(move.getRoom2(), d).getMalePatients() &&
                                !gender1)) {
                            cost++;
                        }
                    }
                }

                if ((gender1 ^ gender2) && (genderPolicy2)) {
                    if (d < Math.min(old_end_day2, new_end_day1)) {
                        // Remove the costs of the current situation
                        cost -= getVs().getRoomStorage(move.getRoom2(), d)
                                .getGenderPolicyConflict();
                        if (gender2) {
                            cost += Math.min(getVs().getRoomStorage(move.getRoom2(), d)
                                    .getMalePatients() - 1, getVs().getRoomStorage(move.getRoom2
                                    (), d)
                                    .getFemalePatients() + 1);
                        } else {
                            cost += Math.min(getVs().getRoomStorage(move.getRoom2(), d)
                                    .getMalePatients() + 1, getVs().getRoomStorage(move.getRoom2
                                    (), d)
                                    .getFemalePatients() - 1);
                        }
                    }
                }

            }
        }

        getObjCosts().setRGCostD(cost);
    }

    private void SwA_DeltaOverCrowdRisk() {

        int actual_end_day1, actual_end_day2, new_end_day1, new_end_day2, actual_new_end_day1,
                actual_new_end_day2;
        int cost = 0;

        new_end_day1 = move.getStartDay2() + move.getsPatient1().getRestLOS();
        new_end_day2 = move.getStartDay1() + move.getsPatient2().getRestLOS();

        if (move.getsPatient1().getVariablity() > 0) {
            if (old_end_day1 < getInstance().getNumDays()) {
                actual_end_day1 = old_end_day1 + 1;
            } else {
                actual_end_day1 = old_end_day1;
            }
            if (new_end_day1 < getInstance().getNumDays()) {
                actual_new_end_day1 = new_end_day1 + 1;
            } else {
                actual_new_end_day1 = new_end_day1;
            }
        } else {
            actual_end_day1 = old_end_day1;
            actual_new_end_day1 = new_end_day1;
        }

        if (move.getsPatient2().getVariablity() > 0) {
            if (old_end_day2 < getInstance().getNumDays()) {
                actual_end_day2 = old_end_day2 + 1;
            } else {
                actual_end_day2 = old_end_day2;
            }
            if (new_end_day2 < getInstance().getNumDays()) {
                actual_new_end_day2 = new_end_day2 + 1;
            } else {
                actual_new_end_day2 = new_end_day2;
            }
        } else {
            actual_end_day2 = old_end_day2;
            actual_new_end_day2 = new_end_day2;
        }
        int capacity1 = getInstance().getRoomCapacity(move.getRoom1());
        int capacity2 = getInstance().getRoomCapacity(move.getRoom2());

        if (move.getRoom1() == move.getRoom2()) {
            int first_day = Math.min(Math.min(actual_end_day1, actual_new_end_day2),
                    Math.min(actual_end_day2, actual_new_end_day1));
            int final_day = Math.max(Math.max(actual_end_day1, actual_new_end_day2),
                    Math.max(actual_end_day2, actual_new_end_day1));
            int shifted_day;
            int[] room_day_potential_crowd = new int[final_day - first_day];

            for (int d = 0; d < room_day_potential_crowd.length; d++) {
                shifted_day = first_day + d;
                room_day_potential_crowd[d] = getVs().getRoomStorage(move.getRoom1(), shifted_day)
                        .getTotalPOccupancy();

                if (room_day_potential_crowd[d] > capacity1) {
                    cost -= (room_day_potential_crowd[d] - capacity1);
                }

                if (shifted_day >= move.getStartDay1() && shifted_day < actual_end_day1) {
                    room_day_potential_crowd[d]--;
                }
                if (shifted_day >= move.getStartDay2() && shifted_day < actual_end_day2) {
                    room_day_potential_crowd[d]--;
                }
                if (shifted_day >= move.getStartDay2() && shifted_day < actual_new_end_day1) {
                    room_day_potential_crowd[d]++;
                }
                if (shifted_day >= move.getStartDay1() && shifted_day < actual_new_end_day2) {
                    room_day_potential_crowd[d]++;
                }

                if (room_day_potential_crowd[d] > capacity1) {
                    cost += (room_day_potential_crowd[d] - capacity1);
                }
            }
        } else {
            for (int d = Math.min(actual_end_day1, actual_new_end_day2);
                 d < Math.max(actual_end_day1, actual_new_end_day2); d++) {
                if (actual_new_end_day2 < actual_end_day1) {
                    if (getVs().getRoomStorage(move.getRoom1(), d).getTotalPOccupancy() >
                            capacity1) {
                        cost--;
                    }
                }
                if (actual_new_end_day2 > actual_end_day1) {
                    if (getVs().getRoomStorage(move.getRoom1(), d).getTotalPOccupancy() >=
                            capacity1) {
                        cost++;
                    }
                }
            }

            for (int d = Math.min(actual_end_day2, actual_new_end_day1); d < Math.max
                    (actual_end_day2,
                            actual_new_end_day1); d++) {
                if (actual_new_end_day1 < actual_end_day2) {
                    if (getVs().getRoomStorage(move.getRoom2(), d).getTotalPOccupancy() >
                            capacity2) {
                        cost--;
                    }
                }

                if (actual_new_end_day1 > actual_end_day2) {
                    if (getVs().getRoomStorage(move.getRoom2(), d).getTotalPOccupancy() >=
                            capacity2) {
                        cost++;
                    }
                }
            }
        }
        getObjCosts().setPOverRCCostD(cost);
    }

    private void SwA_DeltaIdleRoomCost() {

        int LOS1 = move.getsPatient1().getRestLOS();
        int LOS2 = move.getsPatient2().getRestLOS();
        if (LOS1 != LOS2) {
            int cost = 0;
            int new_end_day1, new_end_day2;

            new_end_day1 = move.getStartDay2() + move.getsPatient1().getRestLOS();
            new_end_day2 = move.getStartDay1() + move.getsPatient2().getRestLOS();
            int capacity1 = getInstance().getRoomCapacity(move.getRoom1());
            int capacity2 = getInstance().getRoomCapacity(move.getRoom2());
            if (move.getRoom1() == move.getRoom2()) {
                int first_day = Math.min(move.getStartDay1(), move.getStartDay2()),
                        last_stay = Math.max(Math.max(old_end_day1, old_end_day2),
                                Math.max(new_end_day1, new_end_day2));

                int[] room_assignments = new int[last_stay - first_day];
                for (int d = first_day; d < Math.min(last_stay, getInstance().getPlanningHorizon
                        ()); d++) {
                    // current cost
                    int d1 = d - first_day; //shifted day
                    room_assignments[d1] = getVs().getRoomStorage(move.getRoom1(), d)
                            .getOccupancy();
                    if (room_assignments[d1] < capacity1) {
                        cost -= capacity1 - room_assignments[d1];
                    }
                    // new cost
                    // remove the patients
                    if (d >= move.getStartDay1() && d < old_end_day1) {
                        room_assignments[d1]--;
                    }
                    if (d >= move.getStartDay2() && d < old_end_day2) {
                        room_assignments[d1]--;
                    }
                    //shift the patients and insert the room_assignments
                    if (d >= move.getStartDay2() && d < new_end_day1) {
                        room_assignments[d1]++;
                    }
                    if (d >= move.getStartDay1() && d < new_end_day2) {
                        room_assignments[d1]++;
                    }
                    // compute the cost
                    if (room_assignments[d1] < capacity1) {
                        cost += capacity1 - room_assignments[d1];
                    }
                }
            } else {
                for (int d = Math.min(old_end_day1, new_end_day2); d < Math
                        .min(Math.max(old_end_day1, new_end_day2), getInstance()
                                .getPlanningHorizon()); d++) {
                    if (new_end_day2 < old_end_day1) {
                        if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() <=
                                capacity1) {
                            cost++;
                        }
                    }

                    if (new_end_day2 > old_end_day1) {
                        if (getVs().getRoomStorage(move.getRoom1(), d).getOccupancy() < capacity1) {
                            cost--;
                        }
                    }
                }

                for (int d = Math.min(old_end_day2, new_end_day1); d < Math.min(Math
                        .max(old_end_day2, new_end_day1), getInstance().getPlanningHorizon());
                     d++) {
                    if (new_end_day1 < old_end_day2) {
                        if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() <=
                                capacity2) {
                            cost++;
                        }
                    }

                    if (new_end_day1 > old_end_day2) {
                        if (getVs().getRoomStorage(move.getRoom2(), d).getOccupancy() < capacity2) {
                            cost--;
                        }
                    }
                }
            }

            getObjCosts().setIdleRCCostD(cost);
        }
    }

    private void SwA_DeltaORCapacity() {
        boolean haveSurgery1 = move.getsPatient1().needSurgery();
        boolean haveSurgery2 = move.getsPatient2().needSurgery();
        if (!haveSurgery1 && !haveSurgery2) {
//            System.out.println();
        } else if (move.getStartDay1() == move.getStartDay2()) {
//            System.out.println();
        } else {
            //if((haveSurgery1 || haveSurgery2)&& move.getStartDay1()!= move.getStartDay2()) {
            int spec1 = getInstance().getSpecialism(move.getsPatient1().getNumber()), spec2 =
                    getInstance()
                            .getSpecialism(move.getsPatient2().getNumber());
            int old_surgery_day1 = 0, new_surgery_day1 = 0, old_surgery_day2 = 0,
                    new_surgery_day2 = 0;
            int ORU = 0;
            int ORTU = 0;

            boolean isElective1 = move.getsPatient1().isElective();
            boolean isElective2 = move.getsPatient2().isElective();
            int surgeryDuration1 = 0, oldORSpecCapacity1 = 0, oldORDayCapacity1 = 0,
                    newORSpecCapacity1 = 0,
                    newORDayCapacity1 = 0;
            int surgeryDuration2 = 0, oldORSpecCapacity2 = 0, oldORDayCapacity2 = 0,
                    newORSpecCapacity2 = 0,
                    newORDayCapacity2 = 0;
            int oldORUsedSpecCapacity1 = 0, newORUsedSpecCapacity1 = 0, oldORUsedDayCapacity1 = 0,
                    newORUsedDayCapacity1 = 0;
            int oldORUsedSpecCapacity2 = 0, newORUsedSpecCapacity2 = 0, oldORUsedDayCapacity2 = 0,
                    newORUsedDayCapacity2 = 0;


            //Special cases:
            //A. Both patients must be operated, they have the same specialism and the same
            // new_surgery_day
            //B. Both patients must be operated, they have the same specialism and the same
            // old_surgery_day
            //C. Both patients must be operated, they have the same specialism and the swap the
            // surgery_day
            //D. Both patients must be operated, they have the same specialism and the same the
            // surgery_day

            if (haveSurgery1) {
                old_surgery_day1 = move.getsPatient1().getSD();
                new_surgery_day1 = move.getStartDay2() + move.getsPatient1().getPreSurgeryDay();
                surgeryDuration1 = move.getsPatient1().getSurDur();
                oldORSpecCapacity1 = getInstance().getORSpecCapacity(old_surgery_day1, spec1);
                newORSpecCapacity1 = getInstance().getORSpecCapacity(new_surgery_day1, spec1);
                oldORDayCapacity1 = getInstance().getORDayCapacity(old_surgery_day1);
                newORDayCapacity1 = getInstance().getORDayCapacity(new_surgery_day1);
                oldORUsedSpecCapacity1 = getVs().getORStorage(old_surgery_day1).getSpecOR(spec1)
                        .getOccupancy();
                newORUsedSpecCapacity1 = getVs().getORStorage(new_surgery_day1).getSpecOR(spec1)
                        .getOccupancy();
                oldORUsedDayCapacity1 = getVs().getORStorage(old_surgery_day1).getOccupancy();
                newORUsedDayCapacity1 = getVs().getORStorage(new_surgery_day1).getOccupancy();
            }

            if (haveSurgery2) {
                old_surgery_day2 = move.getsPatient2().getSD();
                new_surgery_day2 = move.getStartDay1() + move.getsPatient2().getPreSurgeryDay();
                surgeryDuration2 = move.getsPatient2().getSurDur();
                oldORSpecCapacity2 = getInstance().getORSpecCapacity(old_surgery_day2, spec2);
                newORSpecCapacity2 = getInstance().getORSpecCapacity(new_surgery_day2, spec2);
                oldORDayCapacity2 = getInstance().getORDayCapacity(old_surgery_day2);
                newORDayCapacity2 = getInstance().getORDayCapacity(new_surgery_day2);
                oldORUsedSpecCapacity2 = getVs().getORStorage(old_surgery_day2).getSpecOR(spec2)
                        .getOccupancy();
                newORUsedSpecCapacity2 = getVs().getORStorage(new_surgery_day2).getSpecOR(spec2)
                        .getOccupancy();
                oldORUsedDayCapacity2 = getVs().getORStorage(old_surgery_day2).getOccupancy();
                newORUsedDayCapacity2 = getVs().getORStorage(new_surgery_day2).getOccupancy();
            }

            if (haveSurgery1 && haveSurgery2 && spec1 == spec2
                    && old_surgery_day1 == new_surgery_day1 && old_surgery_day2 ==
                    new_surgery_day2) {
                // D. Same admission day, only swap room To check
                // nothing changes
            } else if (haveSurgery1 && isElective1 && haveSurgery2 && isElective2
                    && new_surgery_day1 == new_surgery_day2 && spec1 == spec2) {
                if (oldORUsedSpecCapacity1 > oldORSpecCapacity1 + getVs().getORStorage
                        (old_surgery_day1)
                        .getSpecOR(spec1).getAdmittedOverTime()) {
                    ORU -= Math.min(oldORUsedSpecCapacity1 - oldORSpecCapacity1 - getVs()
                            .getORStorage(old_surgery_day1).getSpecOR(spec1).getAdmittedOverTime
                                    (), surgeryDuration1);
                }
                if (oldORUsedSpecCapacity2 > oldORSpecCapacity2 + getVs().getORStorage
                        (old_surgery_day2)
                        .getSpecOR(spec2).getAdmittedOverTime()) {
                    ORU -= Math.min(oldORUsedSpecCapacity2 - oldORSpecCapacity2 - getVs()
                            .getORStorage(old_surgery_day2).getSpecOR(spec2).getAdmittedOverTime
                                    (), surgeryDuration2);
                }

                if (newORUsedSpecCapacity1 + surgeryDuration1 + surgeryDuration2 >
                        newORSpecCapacity1
                                + getVs().getORStorage(new_surgery_day1).getSpecOR(spec1)
                                .getAdmittedOverTime()) {
                    ORU += Math.min(newORUsedSpecCapacity1 + surgeryDuration1 + surgeryDuration2
                            - newORSpecCapacity1 - getVs().getORStorage(new_surgery_day1)
                            .getSpecOR(spec1)
                            .getAdmittedOverTime(), surgeryDuration1 + surgeryDuration2);
                }
            } else if (haveSurgery1 && isElective1 && haveSurgery2 && isElective2
                    && old_surgery_day1 == old_surgery_day2 && spec1 == spec2) {
                if (oldORUsedSpecCapacity1 > oldORSpecCapacity1 + getVs().getORStorage
                        (old_surgery_day1)
                        .getSpecOR(spec1).getAdmittedOverTime()) {
                    ORU -= Math.min(oldORUsedSpecCapacity1 - oldORSpecCapacity1 - getVs()
                                    .getORStorage(old_surgery_day1).getSpecOR(spec1)
                                    .getAdmittedOverTime(),
                            surgeryDuration1 + surgeryDuration2);
                }
                if (newORUsedSpecCapacity1 + surgeryDuration1 > newORSpecCapacity1 + getVs()
                        .getORStorage(new_surgery_day1).getSpecOR(spec1).getAdmittedOverTime()) {
                    ORU += Math.min(newORUsedSpecCapacity1 + surgeryDuration1 -
                            newORSpecCapacity1 - getVs()
                            .getORStorage(new_surgery_day1).getSpecOR(spec1).getAdmittedOverTime
                                    (), surgeryDuration1);
                }
                if (newORUsedSpecCapacity2 + surgeryDuration2 > newORSpecCapacity2
                        + getVs().getORStorage(new_surgery_day2).getSpecOR(spec2)
                        .getAdmittedOverTime()) {
                    ORU += Math.min(newORUsedSpecCapacity2 + surgeryDuration2 - newORSpecCapacity2
                            - getVs().getORStorage(new_surgery_day2).getSpecOR(spec2)
                            .getAdmittedOverTime(), surgeryDuration2);
                }
            } else if (haveSurgery1 && isElective1 && haveSurgery2 && isElective2
                    && new_surgery_day1 == old_surgery_day2 && spec1 == spec2) {
                int old_surgery_day1_ass, old_surgery_day2_ass;

                old_surgery_day1_ass = oldORUsedSpecCapacity1;
                old_surgery_day2_ass = newORUsedSpecCapacity1;

                if (old_surgery_day1_ass > oldORSpecCapacity1 + getVs().getORStorage
                        (old_surgery_day1)
                        .getSpecOR(spec1).getAdmittedOverTime()) {
                    ORU -= old_surgery_day1_ass - oldORSpecCapacity1 - getVs().getORStorage
                            (old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime();
                }
                if (old_surgery_day2_ass > newORSpecCapacity1 + getVs().getORStorage
                        (old_surgery_day2)
                        .getSpecOR(spec1).getAdmittedOverTime()) {
                    ORU -= old_surgery_day2_ass - newORSpecCapacity1 - getVs()
                            .getORStorage(old_surgery_day2).getSpecOR(spec1).getAdmittedOverTime();
                }

                old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 + surgeryDuration2;
                old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2 + surgeryDuration1;

                if (old_surgery_day1_ass > oldORSpecCapacity1 + getVs().getORStorage
                        (old_surgery_day1).getSpecOR
                        (spec1).getAdmittedOverTime()) {
                    ORU += old_surgery_day1_ass - oldORSpecCapacity1 - getVs().getORStorage
                            (old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime();
                }
                if (old_surgery_day2_ass > newORSpecCapacity1 + getVs().getORStorage
                        (old_surgery_day2).getSpecOR
                        (spec1).getAdmittedOverTime()) {
                    ORU += old_surgery_day2_ass - newORSpecCapacity1 - getVs().getORStorage
                            (old_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime();
                }
            } else {
                if (haveSurgery1 && isElective1 && old_surgery_day1 != new_surgery_day1) {
                    if (oldORUsedSpecCapacity1 > oldORSpecCapacity1 + getVs().getORStorage
                            (old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime()) {
                        ORU -= Math.min(oldORUsedSpecCapacity1 - oldORSpecCapacity1 - getVs()
                                        .getORStorage
                                                (old_surgery_day1).getSpecOR(spec1)
                                        .getAdmittedOverTime(),
                                surgeryDuration1);
                    }
                    if (newORUsedSpecCapacity1 + surgeryDuration1 > newORSpecCapacity1 + getVs()
                            .getORStorage
                                    (new_surgery_day1).getSpecOR(spec1).getAdmittedOverTime()) {
                        ORU += Math.min(newORUsedSpecCapacity1 + surgeryDuration1 -
                                        newORSpecCapacity1 - getVs()
                                        .getORStorage(new_surgery_day1).getSpecOR(spec1)
                                        .getAdmittedOverTime(),
                                surgeryDuration1);
                    }
                }
                // cerr << cost << " ";
                if (haveSurgery2 && isElective2 && old_surgery_day2 != new_surgery_day2) {
                    if (oldORUsedSpecCapacity2 > oldORSpecCapacity2 + getVs().getORStorage
                            (old_surgery_day2)
                            .getSpecOR(spec2).getAdmittedOverTime()) {
                        ORU -= Math.min(oldORUsedSpecCapacity2 - oldORSpecCapacity2 - getVs()
                                        .getORStorage
                                                (old_surgery_day2).getSpecOR(spec2)
                                        .getAdmittedOverTime(),
                                surgeryDuration2);
                    }
                    if (newORUsedSpecCapacity2 + surgeryDuration2 > newORSpecCapacity2 + getVs()
                            .getORStorage
                                    (new_surgery_day2).getSpecOR(spec2).getAdmittedOverTime()) {
                        ORU += Math.min(newORUsedSpecCapacity2 + surgeryDuration2 -
                                        newORSpecCapacity2 - getVs()
                                        .getORStorage(new_surgery_day2).getSpecOR(spec2)
                                        .getAdmittedOverTime(),
                                surgeryDuration2);
                    }
                }
            }

            // cerr << cost << " " << endl;
            //Total daily capacity

            // Cases:
            //A. Both patients must be operated, they have the same new_surgery_day
            //B. Both patients must be operated, they have the same old_surgery_day
            //C. Both patients must be operated, they swap the surgery_day
            //D. Both patients must be operated, they have the same old_surgery_day and
            // new_surgery day
            //F. Only one patient must be operated

            int old_surgery_day1_ass, old_surgery_day2_ass, new_surgery_day1_ass,
                    new_surgery_day2_ass;

            old_surgery_day1_ass = oldORUsedDayCapacity1;
            old_surgery_day2_ass = oldORUsedDayCapacity2;
            new_surgery_day1_ass = newORUsedDayCapacity1;
            new_surgery_day2_ass = newORUsedDayCapacity2;

            if (haveSurgery1 && haveSurgery2) {
                // if(old_surgery_day1 == old_surgery_day2 && old_surgery_day1 ==
                // new_surgery_day1) // D. same
                // admission day, change only the room
                if (old_surgery_day1 == old_surgery_day2) {
                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day2_ass - newORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 -
                            surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day2_ass - newORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                } else if (new_surgery_day1 == new_surgery_day2) {
                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day2_ass - oldORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1 +
                            surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day2_ass - oldORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                } else if (new_surgery_day1 == old_surgery_day2) {
                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 +
                            surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1 -
                            surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                } else {
                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day2_ass - oldORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day2_ass - newORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day2_ass - oldORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day2_ass - newORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                }
            } else {
                if (haveSurgery1 && old_surgery_day1 != new_surgery_day1) {
                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;

                    if (old_surgery_day1_ass > oldORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day1_ass - oldORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day1_ass - newORDayCapacity1 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                }
                if (haveSurgery2 && old_surgery_day2 != new_surgery_day2) {
                    if (old_surgery_day2_ass > oldORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= old_surgery_day2_ass - oldORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU -= new_surgery_day2_ass - newORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }

                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day2_ass > oldORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += old_surgery_day2_ass - oldORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2 + Params.ADMITTED_TOTAL_OVERTIME) {
                        ORTU += new_surgery_day2_ass - newORDayCapacity2 - Params
                                .ADMITTED_TOTAL_OVERTIME;
                    }
                }
            }
            getObjCosts().setORUCostD(ORU);
            getObjCosts().setORTUCostD(ORTU);
        }
    }

    private void SwA_DeltaOvertime() {
        boolean haveSurgery1 = move.getsPatient1().needSurgery();
        boolean haveSurgery2 = move.getsPatient2().needSurgery();

        if (!haveSurgery1 && !haveSurgery2) {

        } else if (move.getStartDay1() == move.getStartDay2()) {

        } else {
            int spec1 = getInstance().getSpecialism(move.getsPatient1().getNumber()), spec2 =
                    getInstance()
                            .getSpecialism(move.getsPatient2().getNumber());
            int old_surgery_day1 = 0, new_surgery_day1 = 0, old_surgery_day2 = 0,
                    new_surgery_day2 = 0;
            int ORO = 0;
            int ORTO = 0;

            boolean isElective1 = move.getsPatient1().isElective();
            boolean isElective2 = move.getsPatient2().isElective();
            int surgeryDuration1 = 0, oldORSpecCapacity1 = 0, oldORDayCapacity1 = 0,
                    newORSpecCapacity1 = 0,
                    newORDayCapacity1 = 0;
            int surgeryDuration2 = 0, oldORSpecCapacity2 = 0, oldORDayCapacity2 = 0,
                    newORSpecCapacity2 = 0,
                    newORDayCapacity2 = 0;
            int oldORUsedSpecCapacity1 = 0, newORUsedSpecCapacity1 = 0, newORUsedDayCapacity1 = 0,
                    oldORUsedDayCapacity1 = 0;
            int oldORUsedSpecCapacity2 = 0, newORUsedSpecCapacity2 = 0, newORUsedDayCapacity2 = 0,
                    oldORUsedDayCapacity2 = 0;


            if (haveSurgery1) {
                old_surgery_day1 = move.getsPatient1().getSD();
                new_surgery_day1 = move.getStartDay2() + move.getsPatient1().getPreSurgeryDay();
                surgeryDuration1 = move.getsPatient1().getSurDur();
                oldORSpecCapacity1 = getInstance().getORSpecCapacity(old_surgery_day1, spec1);
                newORSpecCapacity1 = getInstance().getORSpecCapacity(new_surgery_day1, spec1);
                oldORDayCapacity1 = getInstance().getORDayCapacity(old_surgery_day1);
                newORDayCapacity1 = getInstance().getORDayCapacity(new_surgery_day1);
                oldORUsedSpecCapacity1 = getVs().getORStorage(old_surgery_day1).getSpecOR(spec1)
                        .getOccupancy();
                newORUsedSpecCapacity1 = getVs().getORStorage(new_surgery_day1).getSpecOR(spec1)
                        .getOccupancy();
                oldORUsedDayCapacity1 = getVs().getORStorage(old_surgery_day1).getOccupancy();
                newORUsedDayCapacity1 = getVs().getORStorage(new_surgery_day1).getOccupancy();
            }

            if (haveSurgery2) {
                old_surgery_day2 = move.getsPatient2().getSD();
                new_surgery_day2 = move.getStartDay1() + move.getsPatient2().getPreSurgeryDay();
                surgeryDuration2 = move.getsPatient2().getSurDur();
                oldORSpecCapacity2 = getInstance().getORSpecCapacity(old_surgery_day2, spec2);
                newORSpecCapacity2 = getInstance().getORSpecCapacity(new_surgery_day2, spec2);
                oldORDayCapacity2 = getInstance().getORDayCapacity(old_surgery_day2);
                newORDayCapacity2 = getInstance().getORDayCapacity(new_surgery_day2);
                oldORUsedSpecCapacity2 = getVs().getORStorage(old_surgery_day2).getSpecOR(spec2)
                        .getOccupancy();
                newORUsedSpecCapacity2 = getVs().getORStorage(new_surgery_day2).getSpecOR(spec2)
                        .getOccupancy();
                oldORUsedDayCapacity2 = getVs().getORStorage(old_surgery_day2).getOccupancy();
                newORUsedDayCapacity2 = getVs().getORStorage(new_surgery_day2).getOccupancy();
            }


            //Special cases:
            //A. Both patients must be operated, they have the same specialism and the same
            // new_surgery_day
            //B. Both patients must be operated, they have the same specialism and the same
            // old_surgery_day
            //C. Both patients must be operated, they have the same specialism and the swap the
            // surgery_day
            //D. Both patients must be operated, they have the same specialism and the same the
            // surgery_day (only
            // change room)

            if (haveSurgery1 && haveSurgery2 && spec1 == spec2
                    && old_surgery_day1 == new_surgery_day1 && old_surgery_day2 ==
                    new_surgery_day2) {
                // D. Same admission day, only swap room To check
                // nothing changes
            } else if (haveSurgery1 && isElective1 && haveSurgery2 && isElective2
                    && new_surgery_day1 == new_surgery_day2 && spec1 == spec2) {
                // simulate the move
                int old_surgery_day1_ass, old_surgery_day2_ass, new_surgery_day1_ass;

                old_surgery_day1_ass = oldORUsedSpecCapacity1;
                old_surgery_day2_ass = oldORUsedSpecCapacity2;
                new_surgery_day1_ass = newORUsedSpecCapacity1;

                if (old_surgery_day1_ass > oldORSpecCapacity1) {
                    ORO -= Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                            .getORStorage(old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                if (old_surgery_day2_ass > oldORSpecCapacity2) {
                    ORO -= Math.min(old_surgery_day2_ass - oldORSpecCapacity2, getVs()
                            .getORStorage(old_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                if (new_surgery_day1_ass > newORSpecCapacity1) {
                    ORO -= Math.min(new_surgery_day1_ass - newORSpecCapacity1, getVs()
                            .getORStorage(new_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1 + surgeryDuration2;

                if (old_surgery_day1_ass > oldORSpecCapacity1) {
                    ORO += Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                            .getORStorage(old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
                if (old_surgery_day2_ass > oldORSpecCapacity2) {
                    ORO += Math.min(old_surgery_day2_ass - oldORSpecCapacity2, getVs()
                            .getORStorage(old_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
                if (new_surgery_day1_ass > newORSpecCapacity1) {
                    ORO += Math.min(new_surgery_day1_ass -
                            newORSpecCapacity1, getVs().getORStorage(new_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
            } else if (haveSurgery1 && isElective1 && haveSurgery2 && isElective2
                    && old_surgery_day1 == old_surgery_day2 && spec1 == spec2) {
                // simulate the move
                int old_surgery_day1_ass, new_surgery_day2_ass, new_surgery_day1_ass;

                old_surgery_day1_ass = oldORUsedSpecCapacity1;
                new_surgery_day1_ass = newORUsedSpecCapacity1;
                new_surgery_day2_ass = getVs().getORStorage(new_surgery_day2).getSpecOR(spec1)
                        .getOccupancy();

                if (old_surgery_day1_ass > oldORSpecCapacity1) {
                    ORO -= Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                            .getORStorage(old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                if (new_surgery_day1_ass > newORSpecCapacity1) {
                    ORO -= Math.min(new_surgery_day1_ass - newORSpecCapacity1, getVs()
                            .getORStorage(new_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                if (new_surgery_day2_ass > newORSpecCapacity2) {
                    ORO -= Math.min(new_surgery_day2_ass - newORSpecCapacity2, getVs()
                            .getORStorage(new_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 - surgeryDuration2;
                new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;
                new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                if (old_surgery_day1_ass > oldORSpecCapacity1) {
                    ORO += Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                            .getORStorage(old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
                if (new_surgery_day1_ass > newORSpecCapacity1) {
                    ORO += Math.min(new_surgery_day1_ass - newORSpecCapacity1, getVs()
                            .getORStorage(new_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
                if (new_surgery_day2_ass > newORSpecCapacity2) {
                    ORO += Math.min(new_surgery_day2_ass - newORSpecCapacity2, getVs()
                            .getORStorage(new_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
            } else if (haveSurgery1 && isElective1 && haveSurgery2 && isElective2 &&
                    new_surgery_day1 == old_surgery_day2 && //redundant new_surgery_day2 ==
                    // old_surgery_day1
                    spec1 == spec2) {
                // simulate the move
                int old_surgery_day1_ass, old_surgery_day2_ass;

                old_surgery_day1_ass = oldORUsedSpecCapacity1;
                old_surgery_day2_ass = oldORUsedSpecCapacity2;

                if (old_surgery_day1_ass > oldORSpecCapacity1) {
                    ORO -= Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                            .getORStorage(old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                if (old_surgery_day2_ass > oldORSpecCapacity2) {
                    ORO -= Math.min(old_surgery_day2_ass - oldORSpecCapacity2, getVs()
                            .getORStorage(old_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 + surgeryDuration2;
                old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2 + surgeryDuration1;

                if (old_surgery_day1_ass > oldORSpecCapacity1) {
                    ORO += Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                            .getORStorage(old_surgery_day1)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }

                if (old_surgery_day2_ass > oldORSpecCapacity2) {
                    ORO += Math.min(old_surgery_day2_ass - oldORSpecCapacity2, getVs()
                            .getORStorage(old_surgery_day2)
                            .getSpecOR(spec1).getAdmittedOverTime());
                }
            } else {
                if (haveSurgery1 && isElective1 && old_surgery_day1 != new_surgery_day1) {
                    int old_surgery_day1_ass, new_surgery_day1_ass;

                    old_surgery_day1_ass = oldORUsedSpecCapacity1;
                    new_surgery_day1_ass = newORUsedSpecCapacity1;

                    if (old_surgery_day1_ass > oldORSpecCapacity1) {
                        ORO -= Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                                .getORStorage
                                        (old_surgery_day1).getSpecOR(spec1).getAdmittedOverTime());
                    }
                    if (new_surgery_day1_ass > newORSpecCapacity1) {
                        ORO -= Math.min(new_surgery_day1_ass - newORSpecCapacity1, getVs()
                                .getORStorage
                                        (new_surgery_day1).getSpecOR(spec1).getAdmittedOverTime());
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;

                    if (old_surgery_day1_ass > oldORSpecCapacity1) {
                        ORO += Math.min(old_surgery_day1_ass - oldORSpecCapacity1, getVs()
                                .getORStorage
                                        (old_surgery_day1).getSpecOR(spec1).getAdmittedOverTime());
                    }
                    if (new_surgery_day1_ass > newORSpecCapacity1) {
                        ORO += Math.min(new_surgery_day1_ass - newORSpecCapacity1, getVs()
                                .getORStorage
                                        (new_surgery_day1).getSpecOR(spec1).getAdmittedOverTime());
                    }
                }

                if (haveSurgery2 && isElective2 && old_surgery_day2 != new_surgery_day2) {
                    int old_surgery_day2_ass, new_surgery_day2_ass;

                    old_surgery_day2_ass = oldORUsedSpecCapacity2;
                    new_surgery_day2_ass = newORUsedSpecCapacity2;

                    if (old_surgery_day2_ass > oldORSpecCapacity2) {
                        ORO -= Math.min(old_surgery_day2_ass - oldORSpecCapacity2, getVs()
                                .getORStorage
                                        (old_surgery_day2).getSpecOR(spec2).getAdmittedOverTime());
                    }
                    if (new_surgery_day2_ass > newORSpecCapacity2) {
                        ORO -= Math.min(new_surgery_day2_ass - newORSpecCapacity2, getVs()
                                .getORStorage
                                        (new_surgery_day2).getSpecOR(spec2).getAdmittedOverTime());
                    }

                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day2_ass > oldORSpecCapacity2) {
                        ORO += Math.min(old_surgery_day2_ass - oldORSpecCapacity2, getVs()
                                .getORStorage
                                        (old_surgery_day2).getSpecOR(spec2).getAdmittedOverTime());
                    }
                    if (new_surgery_day2_ass > newORSpecCapacity2) {
                        ORO += Math.min(new_surgery_day2_ass - newORSpecCapacity2, getVs()
                                .getORStorage
                                        (new_surgery_day2).getSpecOR(spec2).getAdmittedOverTime());
                    }
                }
            }

            //Total daily overtime

            //Cases:
            // A. Both patients must be operated, they have the same new_surgery_day
            // B. Both patients must be operated, they have the same old_surgery_day
            // C. Both patients must be operated, they swap the surgery_day
            //D. Both patients must be operated, they have the same old_surgery_day and
            // new_surgery day
            //F. Only one patient must be operated


            int old_surgery_day1_ass, old_surgery_day2_ass, new_surgery_day1_ass,
                    new_surgery_day2_ass;

            old_surgery_day1_ass = oldORUsedDayCapacity1;
            old_surgery_day2_ass = oldORUsedDayCapacity2;
            new_surgery_day1_ass = newORUsedDayCapacity1;
            new_surgery_day2_ass = newORUsedDayCapacity2;

            if (haveSurgery1 && haveSurgery2) {
                if (old_surgery_day1 == new_surgery_day1 && old_surgery_day2 == new_surgery_day2) {
                    // nothing changes
                } else if (old_surgery_day1 == old_surgery_day2) {
                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO -= Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO -= Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2) {
                        ORTO -= Math.min(new_surgery_day2_ass - newORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 -
                            surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO += Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO += Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2) {
                        ORTO += Math.min(new_surgery_day2_ass - newORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                } else if (new_surgery_day1 == new_surgery_day2) {
                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO -= Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2) {
                        ORTO -= Math.min(old_surgery_day2_ass - oldORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO -= Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1 +
                            surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO += Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2) {
                        ORTO += Math.min(old_surgery_day2_ass - oldORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO += Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                } else if (new_surgery_day1 == old_surgery_day2) {
                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO -= Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO -= Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 +
                            surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1 -
                            surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO += Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO += Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                } else {
                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO -= Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2) {
                        ORTO -= Math.min(old_surgery_day2_ass - oldORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO -= Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2) {
                        ORTO -= Math.min(new_surgery_day2_ass - newORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO += Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (old_surgery_day2_ass > oldORDayCapacity2) {
                        ORTO += Math.min(old_surgery_day2_ass - oldORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO += Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2) {
                        ORTO += Math.min(new_surgery_day2_ass - newORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                }
            } else {
                if (haveSurgery1 && old_surgery_day1 != new_surgery_day1) {
                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO -= Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO -= Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }

                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1;
                    new_surgery_day1_ass = new_surgery_day1_ass + surgeryDuration1;

                    if (old_surgery_day1_ass > oldORDayCapacity1) {
                        ORTO += Math.min(old_surgery_day1_ass - oldORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day1_ass > newORDayCapacity1) {
                        ORTO += Math.min(new_surgery_day1_ass - newORDayCapacity1, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                }
                if (haveSurgery2 && old_surgery_day2 != new_surgery_day2) {
                    if (old_surgery_day2_ass > oldORDayCapacity2) {
                        ORTO -= Math.min(old_surgery_day2_ass - oldORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2) {
                        ORTO -= Math.min(new_surgery_day2_ass - newORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }

                    old_surgery_day2_ass = old_surgery_day2_ass - surgeryDuration2;
                    new_surgery_day2_ass = new_surgery_day2_ass + surgeryDuration2;

                    if (old_surgery_day2_ass > oldORDayCapacity2) {
                        ORTO += Math.min(old_surgery_day2_ass - oldORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                    if (new_surgery_day2_ass > newORDayCapacity2) {
                        ORTO += Math.min(new_surgery_day2_ass - newORDayCapacity2, Params
                                .ADMITTED_TOTAL_OVERTIME);
                    }
                }
            }

            getObjCosts().setOROCostD(ORO);
            getObjCosts().setORTOCostD(ORTO);
        }
    }

    private void SwA_DeltaIdleOR() {
        boolean haveSurgery1 = move.getsPatient1().needSurgery();
        boolean haveSurgery2 = move.getsPatient2().needSurgery();
        if (!haveSurgery1 && !haveSurgery2) {

        } else if (move.getStartDay1() == move.getStartDay2()) {

        } else {
            int surgeryDuration1 = 0, oldORDayCapacity1 = 0, newORDayCapacity1 = 0;
            int surgeryDuration2 = 0, oldORDayCapacity2 = 0, newORDayCapacity2 = 0;
            int newORUsedDayCapacity1 = 0, oldORUsedDayCapacity1 = 0;
            int newORUsedDayCapacity2 = 0, oldUsedORDayCapacity2 = 0;
            int old_surgery_day1 = 0, new_surgery_day1 = 0, old_surgery_day2 = 0,
                    new_surgery_day2 = 0;
            int cost = 0;

            if (haveSurgery1) {
                old_surgery_day1 = move.getsPatient1().getSD();
                new_surgery_day1 = move.getStartDay2() + move.getsPatient1().getPreSurgeryDay();
                surgeryDuration1 = move.getsPatient1().getSurDur();
                oldORDayCapacity1 = getInstance().getORDayCapacity(old_surgery_day1);
                newORDayCapacity1 = getInstance().getORDayCapacity(new_surgery_day1);
                oldORUsedDayCapacity1 = getVs().getORStorage(old_surgery_day1).getOccupancy();
                newORUsedDayCapacity1 = getVs().getORStorage(new_surgery_day1).getOccupancy();
            }

            if (haveSurgery2) {
                old_surgery_day2 = move.getsPatient2().getSD();
                new_surgery_day2 = move.getStartDay1() + move.getsPatient2().getPreSurgeryDay();
                surgeryDuration2 = move.getsPatient2().getSurDur();
                oldORDayCapacity2 = getInstance().getORDayCapacity(old_surgery_day2);
                newORDayCapacity2 = getInstance().getORDayCapacity(new_surgery_day2);
                oldUsedORDayCapacity2 = getVs().getORStorage(old_surgery_day2).getOccupancy();
                newORUsedDayCapacity2 = getVs().getORStorage(new_surgery_day2).getOccupancy();
            }

            //  Cases:
            // A. Both patients must be operated, they have the same new_surgery_day
            // B. Both patients must be operated, they have the same old_surgery_day
            // C. Both patients must be operated, they swap the surgery_day
            // D. Both patients must be operated, they have the same specialism and the same the
            // surgery_day (SAP = CR)
            // F. Only one patient must be operated


            if (haveSurgery1 && haveSurgery2 && old_surgery_day1 == new_surgery_day1 &&
                    old_surgery_day2 ==
                            new_surgery_day2) {
                // D. Same admission day, only swap room To check
                // nothing changes
                cost = 0;
            } else if (haveSurgery1 && haveSurgery2 && new_surgery_day1 == new_surgery_day2) {
                if (new_surgery_day1 < getInstance().getPlanningHorizon()) {
                    if (newORUsedDayCapacity1 < newORDayCapacity1) {
                        cost -= Math.min(newORDayCapacity1 - newORUsedDayCapacity1,
                                surgeryDuration1 +
                                        surgeryDuration2);
                    }
                }
                if (old_surgery_day1 < getInstance().getPlanningHorizon()) {
                    if (oldORUsedDayCapacity1 - surgeryDuration1 < oldORDayCapacity1) {
                        cost += Math.min(oldORDayCapacity1 - oldORUsedDayCapacity1 +
                                        surgeryDuration1,
                                surgeryDuration1);
                    }
                }
                if (old_surgery_day2 < getInstance().getPlanningHorizon()) {
                    if (oldUsedORDayCapacity2 - surgeryDuration2 < oldORDayCapacity2) {
                        cost += Math.min(oldORDayCapacity2 - oldUsedORDayCapacity2 +
                                        surgeryDuration2,
                                surgeryDuration2);
                    }
                }
            } else if (haveSurgery1 && haveSurgery2 && old_surgery_day1 == old_surgery_day2) {
                if (old_surgery_day1 < getInstance().getPlanningHorizon()) {
                    if (oldORUsedDayCapacity1 - surgeryDuration1 - surgeryDuration2 <
                            oldORDayCapacity1) {
                        cost += Math.min(oldORDayCapacity1 - oldORUsedDayCapacity1 +
                                surgeryDuration1 +
                                surgeryDuration2, surgeryDuration1 + surgeryDuration2);
                    }
                }

                if (new_surgery_day1 < getInstance().getPlanningHorizon()) {
                    if (newORUsedDayCapacity1 < newORDayCapacity1) {
                        cost -= Math.min(newORDayCapacity1 - newORUsedDayCapacity1,
                                surgeryDuration1);
                    }
                }
                if (new_surgery_day2 < getInstance().getPlanningHorizon()) {
                    if (newORUsedDayCapacity2 < newORDayCapacity2) {
                        cost -= Math.min(newORDayCapacity2 - newORUsedDayCapacity2,
                                surgeryDuration2);
                    }
                }
            } else if (haveSurgery1 && haveSurgery2 &&
                    new_surgery_day1 == old_surgery_day2 // redundant new_surgery_day2 ==
                // old_surgery_day1
                    ) {
                if (old_surgery_day1 < getInstance().getPlanningHorizon()) {
                    int old_surgery_day1_ass = oldORUsedDayCapacity1;
                    if (old_surgery_day1_ass < oldORDayCapacity1) {
                        cost -= oldORDayCapacity1 - old_surgery_day1_ass;
                    }
                    old_surgery_day1_ass = old_surgery_day1_ass - surgeryDuration1 +
                            surgeryDuration2;
                    if (old_surgery_day1_ass < oldORDayCapacity1) {
                        cost += oldORDayCapacity1 - old_surgery_day1_ass;
                    }
                }
                if (new_surgery_day1 < getInstance().getPlanningHorizon()) {
                    int old_surgery_day2_ass = oldUsedORDayCapacity2;
                    if (old_surgery_day2_ass < oldORDayCapacity2) {
                        cost -= oldORDayCapacity2 - old_surgery_day2_ass;
                    }
                    old_surgery_day2_ass = old_surgery_day2_ass + surgeryDuration1 -
                            surgeryDuration2;
                    if (old_surgery_day2_ass < oldORDayCapacity2) {
                        cost += oldORDayCapacity2 - old_surgery_day2_ass;
                    }
                }
            } else {
                if (haveSurgery1 && old_surgery_day1 != new_surgery_day1) {
                    if (old_surgery_day1 < getInstance().getPlanningHorizon() &&
                            oldORUsedDayCapacity1 -
                                    surgeryDuration1 < oldORDayCapacity1) {
                        cost += Math.min(oldORDayCapacity1 - oldORUsedDayCapacity1 +
                                        surgeryDuration1,
                                surgeryDuration1);
                    }

                    if (new_surgery_day1 < getInstance().getPlanningHorizon() &&
                            newORUsedDayCapacity1 <
                                    newORDayCapacity1) {
                        cost -= Math.min(newORDayCapacity1 - newORUsedDayCapacity1,
                                surgeryDuration1);
                    }
                }

                if (haveSurgery2 && old_surgery_day2 != new_surgery_day2) {
                    if (old_surgery_day2 < getInstance().getPlanningHorizon() &&
                            oldUsedORDayCapacity2 -
                                    surgeryDuration2 < oldORDayCapacity2) {
                        cost += Math.min(oldORDayCapacity2 - oldUsedORDayCapacity2 +
                                        surgeryDuration2,
                                surgeryDuration2);
                    }

                    if (new_surgery_day2 < getInstance().getPlanningHorizon() &&
                            newORUsedDayCapacity2 <
                                    newORDayCapacity2) {
                        cost -= Math.min(newORDayCapacity2 - newORUsedDayCapacity2,
                                surgeryDuration2);
                    }
                }
            }

            getObjCosts().setIORCostD(cost);
        }
    }

}
