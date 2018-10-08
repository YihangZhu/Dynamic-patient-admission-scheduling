/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

/**
 * Created by zhuyi on 2/15/2017.
 */
public interface Storage<A> {
    SOR getORStorage(int day);

    A getRoomStorage(int roomIndex, int day);
}
