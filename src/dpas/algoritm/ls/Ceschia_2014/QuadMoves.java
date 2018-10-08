/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014;

import dpas.schedule.SPatient;
import dpas.schedule.Solution;
import util.Util;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zhuyi on 11/3/2016.
 */
public abstract class QuadMoves {

    protected Random random = Util.rand;
    private ArrayList<SPatient> sPatientsList;

    protected QuadMoves(ArrayList<SPatient> sPatientsList) {
        this.sPatientsList = sPatientsList;
    }

    protected ArrayList<SPatient> getsPatientsList() {
        return sPatientsList;
    }

    public abstract void search();

    public abstract void acceptMove(Solution solution);
}
