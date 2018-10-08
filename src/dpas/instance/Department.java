/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.instance;


import java.util.ArrayList;

public class Department {
    private String name;
    private boolean[] mainSpecialisms;
    private boolean[] minorSpecialisms;
//    private int capacity;
//    private ArrayList<Integer> nurseResource = new ArrayList<>();

    Department(int specNUm) {
        mainSpecialisms = new boolean[specNUm];
        minorSpecialisms = new boolean[specNUm];
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addMainSpecialism(int specialism) {
        mainSpecialisms[specialism] = true;
    }

    public boolean getMainSpecialisms(int spec) {
        return mainSpecialisms[spec];
    }

    public boolean getMinorSpecialisms(int spec) {
        return minorSpecialisms[spec];
    }

    public void addMinorSpecialism(int specialism) {
        minorSpecialisms[specialism] = true;
    }

//    public int getCapacity() {
//        return capacity;
//    }

//    public void setCapacity(int capacity) {
//        this.capacity = capacity;
//    }
}
