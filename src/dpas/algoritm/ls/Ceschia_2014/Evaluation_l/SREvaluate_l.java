/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_l;

import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.MoveList.SRMove;
import dpas.algoritm.ls.Ceschia_2014.ObjCosts;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.algoritm.ls.EvaOperations;
import dpas.schedule.Solution;

/**
 * Created by zhuyi on 2/10/2017.
 */
public class SREvaluate_l extends ObjCosts_l implements Evaluation<QuadMoves> {
    private SRMove move;

    public SREvaluate_l(EvaOperations evaOpera) {
        super(evaOpera);
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (SRMove) move;
        getObjCosts().initialize();
    }

    @Override
    public int evaluation() {
        int TrCostTemp = 0;
        int roomCostTemp = 0;
        int[] IR_OR_RG_Ri = new int[4];

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

        int startDay1 = move.getsPatient1().getAdmissionDay(), startDay2 = move.getsPatient2()
                .getAdmissionDay();
        int endDay1 = move.getsPatient1().getDischargeDay(), endDay2 = move.getsPatient2()
                .getDischargeDay();

        for (int day = startDay1; day < endDay1; day++) {
            getEvaOpera().addInvolvedRoom(true, move.getsPatient1(), move.getRoom1(), day,
                    IR_OR_RG_Ri);
            getEvaOpera().addInvolvedRoom(false, move.getsPatient1(), move.getRoom2(), day,
                    IR_OR_RG_Ri);
        }

        for (int day = startDay2; day < endDay2; day++) {
            getEvaOpera().addInvolvedRoom(true, move.getsPatient2(), move.getRoom2(), day,
                    IR_OR_RG_Ri);
            getEvaOpera().addInvolvedRoom(false, move.getsPatient2(), move.getRoom1(), day,
                    IR_OR_RG_Ri);
        }

        if (move.getsPatient1().getVariablity() > 0) {
            getEvaOpera().addVariabilityDay(true, move.getRoom1(), endDay1, IR_OR_RG_Ri);
            getEvaOpera().addVariabilityDay(false, move.getRoom2(), endDay1, IR_OR_RG_Ri);
        }
        if (move.getsPatient2().getVariablity() > 0) {
            getEvaOpera().addVariabilityDay(true, move.getRoom2(), endDay2, IR_OR_RG_Ri);
            getEvaOpera().addVariabilityDay(false, move.getRoom1(), endDay2, IR_OR_RG_Ri);
        }
        getEvaOpera().calRoomValues(IR_OR_RG_Ri);
        setRoomRCosts(IR_OR_RG_Ri);
        return getTotalCostsDelta();
    }

    @Override
    public int getTotalCostsDelta() {
        return getObjCosts().getTotalRoomCostsDelta() + getObjCosts().getTrCostD() + getObjCosts
                ().getRoomCostD();
    }

    @Override
    public void makeMove(Solution solution) {
        move.acceptMove(solution);
        updateSolRoomRCosts(solution);
        getObjCosts().updateSolTrCost(solution);
        getObjCosts().updateSolRoomCost(solution);

        solution.updateObjectiveValue(false);
    }


    public void check(ObjCosts objCosts) {
        getObjCosts().checkCostsDelta(objCosts);
    }
}
