/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas;


import dpas.algoritm.FinalComputing;
import dpas.instance.Instance;
import dpas.instance.InstanceXMLReader;
import dpas.schedule.Problem;
import dpas.schedule.ScheduleResult;
import dpas.schedule.Solution;
import dpas.schedule.XMLWriter;
import util.Util;


public class PAS {
    private int currentDay = 0;
    private Instance instance;
    private Problem problem;

    public PAS(String[] args) {
        if (args.length > 0) {
            Params.instanceName = args[0];
            Params.result = args[1];
            Params.display = args[2].equals("1");
//            Params.STATIC = args[2].equals("1");
//            Params.transfer = args[3].equals("1");
        }
        management();
    }

    private void management() {
        Params.initialize();
        Util.initRandomGenerator();
        InstanceXMLReader instanceXmlReader = new InstanceXMLReader();
        instance = instanceXmlReader.readData(Params.instanceFilePath);
        ScheduleResult scheduleResult = new ScheduleResult(instance);
        problem = new Problem(instance, scheduleResult);
        problem.addPatients();
    }

    //only for dynamic version
    public boolean continuE() {
        if (Params.STATIC) return true;
        if (currentDay < (instance.getPlanningHorizon())) {
            if (Params.display) {
                System.out.print(String.format("Day\t%d\t", currentDay));
            }
            Params.dayStateTime = System.currentTimeMillis();
            return problem.updatePatientList(currentDay);

        } else {
            problem.completeSchedule(currentDay);
            FinalComputing fc = new FinalComputing(problem);
            XMLWriter.recordData(Params.resultsFilePath, fc.start(false), instance);
            return false;
        }
    }

    public void storeData(Solution solution) {
        if (Params.STATIC) {
            problem.storeStaticResult(solution);
            FinalComputing fc = new FinalComputing(problem);
            XMLWriter.recordData(Params.resultsFilePath, fc.start(false), instance);
            System.exit(0);
        } else {
            problem.storeResult(solution);
        }
        currentDay++;
    }

    public Problem getProblem() {
        return problem;
    }

}