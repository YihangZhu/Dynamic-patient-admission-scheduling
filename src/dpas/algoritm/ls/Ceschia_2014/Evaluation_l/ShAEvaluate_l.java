/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_l;

import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.MoveList.ShAMove;
import dpas.algoritm.ls.Ceschia_2014.ObjCosts;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.algoritm.ls.EvaOperations;
import dpas.schedule.Solution;

/**
 * Created by zhuyi on 2/10/2017.
 */
public class ShAEvaluate_l extends ObjCosts_l implements Evaluation<QuadMoves> {
    private ShAMove move;

    public ShAEvaluate_l(EvaOperations evaOpera) {
        super(evaOpera);
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (ShAMove) move;
        getObjCosts().initialize();
    }

    @Override
    public int evaluation() {
        int DeCostTemp = 0;
        int[] ORCosts = new int[5];
        int[] IR_OR_RG_Ri = new int[4];

        DeCostTemp -= move.getsPatient().getDelayPriority();
        DeCostTemp += move.getsPatient().getDelayPriority(move.getNewStartDay());
        getObjCosts().setDeCostD(DeCostTemp);

        if (move.getsPatient().needSurgery()) {
            getEvaOpera().addInvolvedOR(true, move.getsPatient(), move.getsPatient().getSD(),
                    ORCosts);// remove the data in OR
            getEvaOpera().addInvolvedOR(false, move.getsPatient(), move.getNewStartDay() + move
                    .getsPatient().getPreSurgeryDay(), ORCosts);// add the data in new OR
            getEvaOpera().ORValuesEvaluation(ORCosts);
            setORRCosts(ORCosts);
        }

        int room = move.getsPatient().getRoomIndexTemp();
        int oldStartDay = move.getOldStartDay();
        int newStartDay = move.getNewStartDay();
        for (int day = 0; day < move.getsPatient().getTotalLOS(); day++, oldStartDay++,
                newStartDay++) {
            getEvaOpera().addInvolvedRoom(true, move.getsPatient(), room, oldStartDay, IR_OR_RG_Ri);
            getEvaOpera().addInvolvedRoom(false, move.getsPatient(), room, newStartDay,
                    IR_OR_RG_Ri);
        }

        assert oldStartDay == move.getOldEndDay();
        assert newStartDay == move.getNewEndDay();
        if (move.getsPatient().getVariablity() > 0) {
            getEvaOpera().addVariabilityDay(true, room, move.getOldEndDay(), IR_OR_RG_Ri);
            getEvaOpera().addVariabilityDay(false, room, move.getNewEndDay(), IR_OR_RG_Ri);
        }
        getEvaOpera().calRoomValues(IR_OR_RG_Ri);
        setRoomRCosts(IR_OR_RG_Ri);
        return getTotalCostsDelta();

    }

    public int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() + getObjCosts().getTotalORCostsDelta() +
                getObjCosts().getDeCostD();
    }

    @Override
    public void makeMove(Solution solution) {
        move.acceptMove(solution);
        getObjCosts().updateSolDeCost(solution);
        if (move.getsPatient().needSurgery()) {
            updateSolORRCosts(solution);
        }
        updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);

    }

    public void check(ObjCosts objCosts) {
        getObjCosts().checkCostsDelta(objCosts);
    }
}
