/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_l;

import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.MoveList.SwAMove;
import dpas.algoritm.ls.Ceschia_2014.ObjCosts;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.algoritm.ls.EvaOperations;
import dpas.schedule.Solution;


/**
 * Created by zhuyi on 2/10/2017.
 */
public class SwAEvaluate_l extends ObjCosts_l implements Evaluation<QuadMoves> {
    private SwAMove move;

    public SwAEvaluate_l(EvaOperations evaOpera) {
        super(evaOpera);
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (SwAMove) move;
        getObjCosts().initialize();
    }

    public int evaluation() {
        int TrCostTemp = 0;
        int roomCostTemp = 0;
        int DeCostTemp = 0;
        int[] IR_OR_RG_Ri = new int[4];
        int[] ORCosts = new int[5];

        if (move.getStartDay1() != move.getStartDay2()) {
            DeCostTemp -= move.getsPatient1().getDelayPriority();
            DeCostTemp += move.getsPatient1().getDelayPriority(move.getStartDay2());

            DeCostTemp -= move.getsPatient2().getDelayPriority();
            DeCostTemp += move.getsPatient2().getDelayPriority(move.getStartDay1());

            getObjCosts().setDeCostD(DeCostTemp);

            if (move.getsPatient1().needSurgery()) {
                getEvaOpera().addInvolvedOR(true, move.getsPatient1(), move.getsPatient1().getSD
                        (), ORCosts);// remove the data in OR
                getEvaOpera().addInvolvedOR(false, move.getsPatient1(), move.getStartDay2() +
                        move.getsPatient1().getPreSurgeryDay(), ORCosts);// add the data in new OR
            }

            if (move.getsPatient2().needSurgery()) {
                getEvaOpera().addInvolvedOR(true, move.getsPatient2(), move.getsPatient2().getSD
                        (), ORCosts);// remove the data in OR
                getEvaOpera().addInvolvedOR(false, move.getsPatient2(), move.getStartDay1() +
                        move.getsPatient2().getPreSurgeryDay(), ORCosts);// add the data in new OR
            }
            getEvaOpera().ORValuesEvaluation(ORCosts);
            setORRCosts(ORCosts);
        }


        if (move.getRoom1() != move.getRoom2()) {
            TrCostTemp -= move.getsPatient1().transfer();
            TrCostTemp += move.getsPatient1().transfer(move.getRoom2());

            TrCostTemp -= move.getsPatient2().transfer();
            TrCostTemp += move.getsPatient2().transfer(move.getRoom1());

            roomCostTemp -= move.getsPatient1().getRoomCosts();
            roomCostTemp += move.getsPatient1().getRoomCosts(move.getRoom2());

            roomCostTemp -= move.getsPatient2().getRoomCosts();
            roomCostTemp += move.getsPatient2().getRoomCosts(move.getRoom1());

            getObjCosts().setTrCostD(TrCostTemp);
            getObjCosts().setRoomCostD(roomCostTemp);
        }


        int oldStartDay1 = move.getStartDay1();
        int newStartDay1 = move.getStartDay2();
        for (int day = 0; day < move.getsPatient1().getRestLOS(); day++, oldStartDay1++,
                newStartDay1++) {
            getEvaOpera().addInvolvedRoom(true, move.getsPatient1(), move.getRoom1(),
                    oldStartDay1, IR_OR_RG_Ri);
            getEvaOpera().addInvolvedRoom(false, move.getsPatient1(), move.getRoom2(),
                    newStartDay1, IR_OR_RG_Ri);
        }

        int oldStartDay2 = move.getStartDay2();
        int newStartDay2 = move.getStartDay1();
        for (int day = 0; day < move.getsPatient2().getRestLOS(); day++, oldStartDay2++,
                newStartDay2++) {
            getEvaOpera().addInvolvedRoom(true, move.getsPatient2(), move.getRoom2(),
                    oldStartDay2, IR_OR_RG_Ri);
            getEvaOpera().addInvolvedRoom(false, move.getsPatient2(), move.getRoom1(),
                    newStartDay2, IR_OR_RG_Ri);
        }

        int oldEndDay1 = move.getsPatient1().getDischargeDay();
        int newEndDay1 = move.getStartDay2() + move.getsPatient1().getRestLOS();
        assert oldEndDay1 == oldStartDay1 && newEndDay1 == newStartDay1;
        if (move.getsPatient1().getVariablity() > 0) {
            getEvaOpera().addVariabilityDay(true, move.getRoom1(), oldEndDay1, IR_OR_RG_Ri);
            getEvaOpera().addVariabilityDay(false, move.getRoom2(), newEndDay1, IR_OR_RG_Ri);
        }

        int oldEndDay2 = move.getsPatient2().getDischargeDay();
        int newEndDay2 = move.getStartDay1() + move.getsPatient2().getRestLOS();
        assert oldEndDay2 == oldStartDay2 && newEndDay2 == newStartDay2;
        if (move.getsPatient2().getVariablity() > 0) {
            getEvaOpera().addVariabilityDay(true, move.getRoom2(), oldEndDay2, IR_OR_RG_Ri);
            getEvaOpera().addVariabilityDay(false, move.getRoom1(), newEndDay2, IR_OR_RG_Ri);
        }
        getEvaOpera().calRoomValues(IR_OR_RG_Ri);
        setRoomRCosts(IR_OR_RG_Ri);

        return getTotalCostsDelta();
    }

    @Override
    public int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() + getObjCosts().getTotalORCostsDelta() +
                getObjCosts().getDeCostD() + getObjCosts().getTrCostD() + getObjCosts()
                .getRoomCostD();
    }

    public void makeMove(Solution solution) {
        move.acceptMove(solution);

        if (move.getRoom1() != move.getRoom2()) {
            getObjCosts().updateSolTrCost(solution);
            getObjCosts().updateSolRoomCost(solution);
        }
        if (move.getStartDay1() != move.getStartDay2()) {
            getObjCosts().updateSolDeCost(solution);
            updateSolORRCosts(solution);
        }
        updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);
    }

    public void check(ObjCosts objCosts) {
        getObjCosts().checkCostsDelta(objCosts);
    }
}
