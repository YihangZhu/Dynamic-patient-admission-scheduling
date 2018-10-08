/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.Params;

import java.util.ArrayList;

/**
 * Created by zhuyi on 2/6/2017.
 */
public class SOR {
    private int day;
    private int capacity;
    private SSpecOR[] specOR;
    private ArrayList<Integer> selectedSpec;
    private int occupancy;

    SOR(int day, int capacity, int[] specCap, int[] specSlotNum) {
        this.day = day;
        this.capacity = capacity;
        specOR = new SSpecOR[specCap.length];
        for (int s = 0; s < specOR.length; s++) {
            specOR[s] = new SSpecOR(s, specCap[s], specSlotNum[s]);
        }
        selectedSpec = new ArrayList<>();
    }

    public SOR(SOR OR) {
        day = OR.getDay();
        capacity = OR.getCapacity();
        specOR = new SSpecOR[OR.getSpecNum()];
        selectedSpec = new ArrayList<>();
        occupancy = OR.getOccupancy();
    }

    public void initialize() {
        for (SSpecOR sSpecOR : specOR) {
            sSpecOR.initialize();
        }
        occupancy = 0;
        selectedSpec.clear();
    }

    public int getDay() {
        return day;
    }

    public int getCapacity() {
        return capacity;
    }

    private ArrayList<Integer> getSelectedSpec() {
        return selectedSpec;
    }

    public SSpecOR getSpecOR(int spec) {
        return specOR[spec];
    }

    public void addSpecOR(SSpecOR specOR) {
        int spec = specOR.getSpec();
        this.specOR[spec] = specOR;
        selectedSpec.add(spec);
    }

    private int getSpecNum() {
        return specOR.length;
    }

    public int getOverORT() {
        return Integer.max(0, Integer.min(Params.ADMITTED_TOTAL_OVERTIME, occupancy - capacity));
    }

    public int getOverORTU() {
        return Integer.max(0, occupancy - Params.ADMITTED_TOTAL_OVERTIME - capacity);
    }

    public int getIOS() {
        return Integer.max(0, capacity - occupancy);
    }

    public int getOccupancy() {
        return occupancy;
    }

    public void incORDayOccupancy(int surgeryDuration) {
        occupancy += surgeryDuration;
    }

    public void decORDayOccupancy(int surgeryDuration) {
        occupancy -= surgeryDuration;
        assert occupancy >= 0;
    }

    void updateOR(SOR buf) {
        occupancy = buf.getOccupancy();
        for (int s : buf.getSelectedSpec()) {
            specOR[s].updateSpecOR(buf.getSpecOR(s));
        }
    }

    public int getORO() {
        int ORO = 0;
        for (int s : selectedSpec) {
            ORO += specOR[s].getOverOR();
        }
        return ORO;
    }

    public int getORU() {
        int ORU = 0;
        for (int s : selectedSpec) {
            ORU += specOR[s].getOverORU();
        }
        return ORU;
    }
}
