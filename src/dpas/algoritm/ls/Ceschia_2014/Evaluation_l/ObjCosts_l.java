/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_l;

import dpas.algoritm.ls.Ceschia_2014.ObjCosts;
import dpas.algoritm.ls.EvaOperations;
import dpas.schedule.Solution;

/**
 * Created by zhuyi on 2/10/2017.
 */
public abstract class ObjCosts_l {
    private EvaOperations evaOpera;
    private ObjCosts objCosts = new ObjCosts();

    public ObjCosts_l(EvaOperations evaOpera) {
        this.evaOpera = evaOpera;
    }

    protected void setRoomRCosts(int[] values) {
        objCosts.setIdleRCCostD(values[0]);
        objCosts.setOverRCCostD(values[1]);
        objCosts.setRGCostD(values[2]);
        objCosts.setPOverRCCostD(values[3]);
    }

    protected void updateSolRoomRCosts(Solution solution) {
        objCosts.updateSolRoomRCosts(solution);
        evaOpera.updateRoomValues();
    }

    protected void setORRCosts(int[] ORCosts) {
        objCosts.setOROCostD(ORCosts[0]);
        objCosts.setORUCostD(ORCosts[1]);
        objCosts.setORTOCostD(ORCosts[2]);
        objCosts.setORTUCostD(ORCosts[3]);
        objCosts.setIORCostD(ORCosts[4]);
    }

    protected void updateSolORRCosts(Solution solution) {
        objCosts.updateSolORRCosts(solution);
        evaOpera.updateORValues();
    }

    public ObjCosts getObjCosts() {
        return objCosts;
    }

    protected EvaOperations getEvaOpera() {
        return evaOpera;
    }

    public abstract int getTotalCostsDelta();
}

