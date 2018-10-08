/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import dpas.Params;

/**
 * Created by zhuyi on 2/7/2017.
 */
public class SSpecOR {
    private int spec;
    private int capacity;
    private int occupancy;
    private int admittedOverTime;

    SSpecOR(int spec, int capacity, int slotNum) {
        this.spec = spec;
        this.capacity = capacity;
        admittedOverTime = slotNum * Params.ADMITTED_OVERTIME;
    }

    public SSpecOR(SSpecOR specOR) {
        spec = specOR.getSpec();
        capacity = specOR.getCapacity();
        admittedOverTime = specOR.getAdmittedOverTime();
        occupancy = specOR.getOccupancy();
    }

    int getSpec() {
        return spec;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public int getAdmittedOverTime() {
        return admittedOverTime;
    }

    public void initialize() {
        occupancy = 0;
    }

    public int getOverOR() {
        return Integer.max(0, Integer.min(admittedOverTime, occupancy - capacity));
    }

    public int getOverORU() {
        return Integer.max(0, occupancy - capacity - admittedOverTime);
    }

    public void incOccupancy(int surgeryDuration) {
        occupancy += surgeryDuration;
    }

    public void decOccupancy(int surgeryDuration) {
        occupancy -= surgeryDuration;
        assert occupancy >= 0;
    }

    void updateSpecOR(SSpecOR buf) {
        occupancy = buf.getOccupancy();
    }
}
