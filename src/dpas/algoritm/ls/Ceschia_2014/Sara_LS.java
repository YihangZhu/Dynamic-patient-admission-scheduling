/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.ls.Ceschia_2014;

import algorithm.SimulatedAnnealing;
import dpas.PAS;
import dpas.Params;
import dpas.algoritm.Initialize;
import dpas.algoritm.SolCalculation;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_ll.CREvaluate_ll;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_ll.SREvaluate_ll;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_ll.ShAEvaluate_ll;
import dpas.algoritm.ls.Ceschia_2014.Evaluation_ll.SwAEvaluate_ll;
import dpas.algoritm.ls.Ceschia_2014.MoveList.CRMove;
import dpas.algoritm.ls.Ceschia_2014.MoveList.SRMove;
import dpas.algoritm.ls.Ceschia_2014.MoveList.ShAMove;
import dpas.algoritm.ls.Ceschia_2014.MoveList.SwAMove;
import dpas.algoritm.ls.EvaOperations;
import dpas.instance.Instance;
import dpas.schedule.Problem;
import dpas.schedule.Solution;
import dpas.schedule.ValuesBuffer;
import dpas.schedule.ValuesStorage;
import util.Util;

public class Sara_LS {
    private Problem problem;
    private Instance instance;
    private ValuesStorage vs;
    private ValuesBuffer vb;
    private SolCalculation solCal;
    private EvaOperations evaOpera;
//    private CREvaluate_l crEvaluate_l;
//    private SREvaluate_l srEvaluate_l;
//    private SwAEvaluate_l swAEvaluate_l;
//    private ShAEvaluate_l shAEvaluate_l;

    private CREvaluate_ll crEvaluate_ll;
    private SREvaluate_ll srEvaluate_ll;
    private SwAEvaluate_ll swAEvaluate_ll;
    private ShAEvaluate_ll shAEvaluate_ll;
    private boolean getFeasible = false;

    private Sara_LS(Problem problem) {
        this.instance = problem.getInstance();
        this.problem = problem;
        vs = new ValuesStorage(instance);
        vb = new ValuesBuffer(instance, vs);
        solCal = new SolCalculation(problem, vs);

        evaOpera = new EvaOperations(vs, vb, instance);
//        crEvaluate_l = new CREvaluate_l(evaOpera);
//        srEvaluate_l = new SREvaluate_l(evaOpera);
//        swAEvaluate_l = new SwAEvaluate_l(evaOpera);
//        shAEvaluate_l = new ShAEvaluate_l(evaOpera);

        crEvaluate_ll = new CREvaluate_ll(instance, vs);
        srEvaluate_ll = new SREvaluate_ll(instance, vs);
        swAEvaluate_ll = new SwAEvaluate_ll(instance, vs);
        shAEvaluate_ll = new ShAEvaluate_ll(instance, vs);

    }

    @SuppressWarnings("unused")
    public void setGetFeasible() {
        this.getFeasible = true;
    }

    private int count1 = 0;

    private Solution localSearchLaunch() {
        int currentDay = problem.getCurrent();
        vs.initialize(currentDay);
        SimulatedAnnealing SA = new SimulatedAnnealing(to, tmin, delta, iterationMax);

        CRMove changeRoom = new CRMove(instance, problem.getPatientSet());
        SRMove swapRoom = new SRMove(problem.getPatientSet());
        SwAMove swapAdmission = new SwAMove(problem.getPatientSet());
        ShAMove shiftDelay = new ShAMove(problem.getUnadmittedPatients());

        Initialize solInit = new Initialize(instance, problem.getUnadmittedPatients(), problem
                .getPatientSet());
        Solution solution = solInit.zeroInitialize();
        solCal.assign(solution);
        solCal.objectiveValueCalculation(solution, false);

        SA.getReady();
        QuadMoves move;
        Evaluation<QuadMoves> evaluation_Mine;
        Evaluation<QuadMoves> evaluation_Sara;
        Solution bestSolution = new Solution(solution);
        int unImprove = 0;
        while (!SA.stopCriteria()) {
            vb.initializeBuffers();
            double r = Util.rand.nextDouble();
            double pCR = 0.49;
            double pSR = 0.35;
            double pShA = 0.01;
            if (r < pCR) {        // change room
                move = changeRoom;
//                evaluation_Mine = crEvaluate_l;
                evaluation_Sara = crEvaluate_ll;
            } else if (r < pCR + pSR) {  // swap room
                move = swapRoom;
//                evaluation_Mine = srEvaluate_l;
                evaluation_Sara = srEvaluate_ll;
            } else if (r < pCR + pSR + pShA) { // shift admission delay.
                move = shiftDelay;
//                evaluation_Mine = shAEvaluate_l;
                evaluation_Sara = shAEvaluate_ll;
            } else {  // swap admission delay.
                move = swapAdmission;
//                evaluation_Mine = swAEvaluate_l;
                evaluation_Sara = swAEvaluate_ll;
            }
            move.search();
//            evaluation_Mine.setMove(move);
            evaluation_Sara.setMove(move);

//            int delta_Mine = evaluation_Mine.evaluation();
            int delta_Sara = evaluation_Sara.evaluation();

            if (SA.acceptMove(delta_Sara)) {
//                evaluation_Mine.makeMove(solution);
                evaluation_Sara.makeMove(solution);
//                solCal.objectiveValueCalculation(solution,true);
                if (evaOpera.compareBestSolution(SA, solution, bestSolution, false)) {
                    bestSolution = new Solution(solution);
                    unImprove = 0;
                } else {
                    unImprove++;
                }
            }

            if (unImprove >= 5000) {
                SA.nextTemp();
                unImprove = 0;
            }

            if (getFeasible) {
                if (getFeasibleSol(bestSolution)) {
                    break;
                }
            }
            count1++;

        }

//        if (!(bestSolution.getORU()==0 && bestSolution.getORTU()==0 && bestSolution
// .getOverRoomCost()==0)) {
//            throw new IllegalArgumentException("Hard constraints are violated!");
//        }
        //System.out.print(System.currentTimeMillis() - start + "\t" + count + "\t" + "objective
        // value:" + bestSolution.getObjectiveValue()+"\n");
        if (Params.display) {
            evaOpera.showResult(SA, bestSolution);
        }
        return bestSolution;
    }

    private boolean getFeasibleSol(Solution solution) {
        return (solution.getORU() == 0 && solution.getORTU() == 0 && solution.getOverRoomCost()
                == 0);
    }

    public final static double to = 154.88;
    public final static double tmin = 1.54;
    public final static double delta = 0.999;
    public final static int iterationMax = 1086;//6long // 429;//4long //504;//2long //   430;
    // 6short // 480;//4short //

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args.length != 5) {
                throw new IllegalMonitorStateException("Parameter number is:\t" + args.length);
            } else {
                Params.SEED = Integer.parseInt(args[4]);
            }
        }

        PAS pas = new PAS(args);
        Sara_LS ls = new Sara_LS(pas.getProblem());
        while (pas.continuE()) {
            pas.storeData(ls.localSearchLaunch());
        }
//        }else {
//            System.out.println("Please input parameters!");
//        }
    }
}
