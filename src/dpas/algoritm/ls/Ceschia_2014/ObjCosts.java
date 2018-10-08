/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014;

import dpas.Params;
import dpas.schedule.Solution;

/**
 * Created by zhuyi on 2/9/2017.
 */
public class ObjCosts {
    private int TrCostD;
    private int roomCostD;
    private int DeCostD;

    private int idleRCCostD;
    private int overRCCostD;
    private int RGCostD;
    private int POverRCCostD;

    private int OROCostD;
    private int ORUCostD;
    private int ORTOCostD;
    private int ORTUCostD;
    private int IORCostD;


    public void setTrCostD(int TrCostD) {
        this.TrCostD = TrCostD * Params.TRANSFER;
    }

    public void setRoomCostD(int roomCostD) {
        this.roomCostD = roomCostD;
    }

    public void setDeCostD(int DeCostTemp) {
        DeCostD = DeCostTemp * Params.DELAY;
    }

    public void setIdleRCCostD(int idleRCCostD) {
        this.idleRCCostD = idleRCCostD * Params.IDLE_ROOM_CAPACITY;
    }

    public void setOverRCCostD(int overRCCostD) {
        this.overRCCostD = overRCCostD * Params.HARD_CONSTRAINTS;
    }

    public void setRGCostD(int RGCostD) {
        this.RGCostD = RGCostD * Params.GENDER_POLICY;
    }

    public void setPOverRCCostD(int POverRCCostD) {
        this.POverRCCostD = POverRCCostD * Params.OVERCROWD_RISK;
    }

    public void setOROCostD(int OROCostD) {
        this.OROCostD = OROCostD * Params.OVERTIME;
    }

    public void setORUCostD(int ORUCostD) {
        this.ORUCostD = ORUCostD * Params.HARD_CONSTRAINTS;
    }

    public void setORTOCostD(int ORTOCostD) {
        this.ORTOCostD = ORTOCostD * Params.OVERTIME;
    }

    public void setORTUCostD(int ORTUCostD) {
        this.ORTUCostD = ORTUCostD * Params.HARD_CONSTRAINTS;
    }

    public void setIORCostD(int IORCostD) {
        this.IORCostD = IORCostD * Params.IDLE_OPERATING_ROOM;
    }

    public int getTotalRoomCostsDelta() {
        return idleRCCostD + overRCCostD + RGCostD + POverRCCostD;
    }

    public int getTotalORCostsDelta() {
        return OROCostD + ORUCostD + ORTOCostD + ORTUCostD + IORCostD;
    }


    public int getTrCostD() {
        return TrCostD;
    }

    public int getRoomCostD() {
        return roomCostD;
    }

    public int getDeCostD() {
        return DeCostD;
    }

    /*
    public int getIdleRCCostD() {
        return idleRCCostD;
    }

    public int getOverRCCostD() {
        return overRCCostD;
    }

    public int getRGCostD() {
        return RGCostD;
    }

    public int getPOverRCCostD() {
        return POverRCCostD;
    }

    public int getOROCostD() {
        return OROCostD;
    }

    public int getORUCostD() {
        return ORUCostD;
    }

    public int getORTOCostD() {
        return ORTOCostD;
    }

    public int getORTUCostD() {
        return ORTUCostD;
    }

    public int getIORCostD() {
        return IORCostD;
    }
    */

    public void initialize() {
        TrCostD = 0;
        roomCostD = 0;
        DeCostD = 0;

        idleRCCostD = 0;
        overRCCostD = 0;
        RGCostD = 0;
        POverRCCostD = 0;

        OROCostD = 0;
        ORUCostD = 0;
        ORTOCostD = 0;
        ORTUCostD = 0;
        IORCostD = 0;
    }

    public void updateSolRoomRCosts(Solution solution) {
        solution.updateIRCost(idleRCCostD);
        solution.updateOverRoomCost(overRCCostD);
        solution.updateRGCost(RGCostD);
        solution.updateRiCost(POverRCCostD);
    }

    public void updateSolORRCosts(Solution solution) {
        solution.updateOROORTO(OROCostD, ORTOCostD);
        solution.updateORUORTU(ORUCostD, ORTUCostD);
        solution.updateORIdleTime(IORCostD);
    }

    public void updateSolTrCost(Solution solution) {
        solution.updateTrCost(TrCostD);
    }

    public void updateSolRoomCost(Solution solution) {
        solution.updateRoomCost(roomCostD);
    }

    public void updateSolDeCost(Solution solution) {
        solution.updateDeCost(DeCostD);
    }

    public void checkCostsDelta(ObjCosts objCosts) {
        if (TrCostD != objCosts.TrCostD) {
            throw new IllegalMonitorStateException("Tr cost is not correct!");
        }

        if (roomCostD != objCosts.roomCostD) {
            throw new IllegalMonitorStateException("room cost is not correct!");
        }

        if (DeCostD != objCosts.DeCostD) {
            throw new IllegalMonitorStateException("delay cost is not correct");
        }
        if (idleRCCostD != objCosts.idleRCCostD) {
            throw new IllegalMonitorStateException("idle room capacity is not correct!");
        }

        if (overRCCostD != objCosts.overRCCostD) {
            throw new IllegalMonitorStateException("over room capacity is not correct!");
        }
        if (RGCostD != objCosts.RGCostD) {
            throw new IllegalMonitorStateException("Room gender is not correct!");
        }

        if (POverRCCostD != objCosts.POverRCCostD) {
            throw new IllegalMonitorStateException("room over crowd cost is not correct!");
        }

        if (OROCostD != objCosts.OROCostD) {
            throw new IllegalMonitorStateException("specialism over time cost is not correct!");
        }

        if (ORUCostD != objCosts.ORUCostD) {
            throw new IllegalMonitorStateException("specialism capacity over time is not correct!");
        }

        if (ORTOCostD != objCosts.ORTOCostD) {
            throw new IllegalMonitorStateException("daily over time cost is not correct!");
        }

        if (ORTUCostD != objCosts.ORTUCostD) {
            throw new IllegalMonitorStateException("daily operating room capacity cost is not " +
                    "correct!");
        }

        if (IORCostD != objCosts.IORCostD) {
            throw new IllegalMonitorStateException("idle daily operating room capacity cost is " +
                    "not correct!");
        }

    }

}
