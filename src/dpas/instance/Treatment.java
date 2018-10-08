/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.instance;

public class Treatment {
    private String name;
    private boolean requireSurgery = false;
    private int specialism;
    private int durationSurgery;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    void setSurgeryRequirement(int surgeryRequirement) {
        if (surgeryRequirement == 1) {
            requireSurgery = true;
        }
    }

    @SuppressWarnings("unused")
    public boolean getSurgeryRequirement() {
        return this.requireSurgery;
    }

    public void setSpecialism(int specialism) {
        this.specialism = specialism;
    }

    public int getSpecialism() {
        return this.specialism;
    }

    void setDurationSurgery(int durationSurgery) {
        this.durationSurgery = durationSurgery;
    }

    public int getDurationSurgery() {
        return durationSurgery;
    }
}
