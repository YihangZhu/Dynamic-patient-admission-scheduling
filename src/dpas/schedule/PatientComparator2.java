/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.instance.Status;

import java.util.Comparator;

/**
 * Created by zhuyi on 2/18/2017.
 */
public class PatientComparator2 implements Comparator<SPatient> {
    public int compare(SPatient o1, SPatient o2) {
        if (o1.getStatus().equals(Status.Admitted) && o2.getStatus().equals(Status.Registered)) {
            return -1;
        } else if (o1.getStatus().equals(Status.Registered) && o2.getStatus().equals(Status
                .Admitted)) {
            return 1;
        } else {
            return Integer.compare(o1.getMaxDelay(), o2.getMaxDelay());
        }
    }
}

