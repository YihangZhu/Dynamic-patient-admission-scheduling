/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014.Evaluation_ll;

import dpas.algoritm.ls.Ceschia_2014.ObjCosts;
import dpas.instance.Instance;
import dpas.schedule.ValuesStorage;

/**
 * Created by zhuyi on 2/10/2017.
 */
abstract class ObjCosts_ll {
    private Instance instance;
    private ValuesStorage vs;
    private ObjCosts objCosts = new ObjCosts();

    ObjCosts_ll(Instance instance, ValuesStorage vs) {
        this.instance = instance;
        this.vs = vs;
    }

    public ValuesStorage getVs() {
        return vs;
    }

    public Instance getInstance() {
        return instance;
    }

    public ObjCosts getObjCosts() {
        return objCosts;
    }

    abstract int getTotalCostsDelta();
}
