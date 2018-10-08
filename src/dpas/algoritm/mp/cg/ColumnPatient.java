/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import algorithm.columnGeneration.Column;
import gurobi.GRBVar;

import java.util.Arrays;

public class ColumnPatient extends Column {
    private int delay;
    private int[] rooms;

    ColumnPatient(int ID) {
        super(ID);
    }

    ColumnPatient(int ID, int delay, int[] rooms) {
        super(ID);
        this.delay = delay;
        this.rooms = rooms;
    }

    @Override
    public void setVar(GRBVar var) {
        super.setVar(var);
    }

    @Override
    public int getID() {
        return super.getID();
    }

    @Override
    public GRBVar getVar() {
        return super.getVar();
    }

    @Override
    public void setCost(double cost) {
        super.setCost(cost);
    }

    @Override
    public double getCost() {
        return super.getCost();
    }

    public int getDelay() {
        return delay;
    }

    public int[] getRooms() {
        return rooms;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void setRooms(int[] rooms) {
        this.rooms = rooms;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ColumnPatient that = (ColumnPatient) o;

        return getID() == that.getID() &&
                delay == that.delay &&
                Arrays.equals(rooms, that.rooms);
    }

    @Override
    public int hashCode() {
        int result = getID();
        result = 31 * result + delay;
        result = 31 * result + Arrays.hashCode(rooms);
        return result;
    }

    @Override
    public String toString() {
        return "ColumnPatient{" +
                "ID=" + getID() +
                ", ReducedCost=" + getReducedCost() +
                ", delay=" + delay +
                ", rooms=" + Arrays.toString(rooms) +
                '}';
    }
}
