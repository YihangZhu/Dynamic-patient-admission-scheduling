/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package algorithm;

import util.Util;

/**
 * Created by yi hang on 9/1/2016.
 *
 */
public class SimulatedAnnealing {
    private double To;
    private double Tmin;
    private double delta;
    private int iterationMax;
    private double T;
    private int iteration;
    private boolean ctn;


    public SimulatedAnnealing(double To, double Tmin, double delta, int iterationMax){
        this.To = To;
        this.Tmin = Tmin;
        this.delta = delta;
        this.iterationMax = iterationMax;
        getReady();
    }

    public void getReady(){
        T = To;
        iteration = 0;
        ctn = true;
    }
    private void updateT(){
        T = delta*T;
    }

    public boolean stopCriteria(){
        if (T >= Tmin){
            if (iteration < iterationMax && ctn){
                iteration++;
            }else {
                iteration = 0;
                updateT();
                ctn = true;
            }
            return false;
        }else {
            return true;
        }
    }

    public boolean acceptMove(int delta){
        double prob;
        if (delta<=0){
            prob = 1;
        }else{
            prob = Math.exp(-(delta/T));
        }
        return Util.rand.nextDouble()< prob ;
    }

    public double getT() {
        return T;
    }

    public int getIteration() {
        return iteration;
    }

    public void nextTemp() {
        this.ctn = false;
    }
}

