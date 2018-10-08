/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas;

import dpas.algoritm.FinalComputing;
import dpas.instance.Instance;
import util.Util;


/**
 * Created by zhuyi on 9/26/2016.
 */
public class Validator {
    private void validation(String[] args) {
        String instancePath = null;
        String solutionPath = null;
        Params.initialize();
        Util.initRandomGenerator();
        if (args.length == 0) {
            instancePath = Params.instanceFilePath;
            solutionPath = Params.resultsFilePath;
        } else if (args.length == 2) {
            instancePath = args[0];
            solutionPath = args[1];
        } else {
            System.err.println("<instance file> <solution file>");
        }

        Instance instance = Util.readInstance(instancePath);
        FinalComputing fc = new FinalComputing(Util.readSolution(instance, solutionPath), instance);
        System.out.print(solutionPath + "\t");
        Util.printResults(fc.start(true));
    }

    public static void main(String[] args) {
        Validator validator = new Validator();
        validator.validation(args);
    }

}
