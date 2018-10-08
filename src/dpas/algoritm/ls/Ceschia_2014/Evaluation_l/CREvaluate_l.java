/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_l;


import dpas.algoritm.ls.Ceschia_2014.Evaluation;
import dpas.algoritm.ls.Ceschia_2014.MoveList.CRMove;
import dpas.algoritm.ls.Ceschia_2014.ObjCosts;
import dpas.algoritm.ls.Ceschia_2014.QuadMoves;
import dpas.algoritm.ls.EvaOperations;
import dpas.schedule.Solution;


/**
 * Created by zhuyi on 2/10/2017.
 */
public class CREvaluate_l extends ObjCosts_l implements Evaluation<QuadMoves> {
    private CRMove move;

    public CREvaluate_l(EvaOperations evaOpera) {
        super(evaOpera);
    }

    @Override
    public void setMove(QuadMoves move) {
        this.move = (CRMove) move;
        getObjCosts().initialize();
    }

    @Override
    public int evaluation() {
        int TrCostTemp = 0;
        int roomCostTemp = 0;
        int startDay = move.getsPatient().getAdmissionDay(), endDay = move.getsPatient()
                .getDischargeDay();

        TrCostTemp -= move.getsPatient().transfer();
        TrCostTemp += move.getsPatient().transfer(move.getNewRoom());

        roomCostTemp -= move.getsPatient().getRoomCosts();
        roomCostTemp += move.getsPatient().getRoomCosts(move.getNewRoom());

        getObjCosts().setTrCostD(TrCostTemp);
        getObjCosts().setRoomCostD(roomCostTemp);


        int[] IR_OR_RG_Ri = new int[4];
        for (int day = startDay; day < endDay; day++) {
            getEvaOpera().addInvolvedRoom(true, move.getsPatient(), move.getOldRoom(), day,
                    IR_OR_RG_Ri);
            getEvaOpera().addInvolvedRoom(false, move.getsPatient(), move.getNewRoom(), day,
                    IR_OR_RG_Ri);
        }
        if (move.getsPatient().getVariablity() > 0) {
            getEvaOpera().addVariabilityDay(true, move.getOldRoom(), endDay, IR_OR_RG_Ri);
            getEvaOpera().addVariabilityDay(false, move.getNewRoom(), endDay, IR_OR_RG_Ri);
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

    //@Override
    public void makeMove(Solution solution) {
        move.acceptMove(solution);
        getObjCosts().updateSolTrCost(solution);
        getObjCosts().updateSolRoomCost(solution);
        updateSolRoomRCosts(solution);
        solution.updateObjectiveValue(false);
    }

    public void check(ObjCosts objCosts) {
        getObjCosts().checkCostsDelta(objCosts);
    }

}
