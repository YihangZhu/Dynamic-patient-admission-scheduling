/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.instance;


public class Room {
    private String name;
    private int number;
    private GenderPolicy genderPolicy;   // enum
    private Department department;
    private int capacity;
    private boolean[] features;

    //private ArrayList<String> features = new ArrayList<>();
    public Room(int featureNUm) {
        features = new boolean[featureNUm];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    void setDepartment(Department department) {
        this.department = department;
    }

    Department getDepartment() {
        return department;
    }

    void setGenderPolicy(GenderPolicy genderPolicy) {
        this.genderPolicy = genderPolicy;
    }

    public GenderPolicy getGenderPolicy() {
        return genderPolicy;
    }

    void setCapacity(int capacity) {
        this.capacity = capacity;
//        this.capacity = capacity > 2 ? capacity - 2 : 1;
//        this.capacity = capacity > 1 ? capacity - 1 : capacity;
//        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    void addFeature(int feature) {
        features[feature] = true;
    }

    boolean[] getFeatures() {
        return features;
    }

}

