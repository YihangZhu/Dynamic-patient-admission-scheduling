/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014;

import dpas.schedule.Solution;

/**
 * Created by zhuyi on 2/11/2017.
 */
public interface Evaluation<T> {
    void setMove(T move);

    int evaluation();

    void makeMove(Solution solution);
}
