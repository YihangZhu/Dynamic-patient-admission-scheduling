/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import dpas.PAS;
import dpas.Params;
import dpas.schedule.Solution;

/**
 * Created by yihang on 5/12/17.
 * EXECUTE MIP MODEL
 */
public class RunMIP {
    public static void main(String[] args) {

//        if (args.length > 2) {
//            Params.timeLimit = Double.parseDouble(args[3]);
//            if (args[4].equals("1")) {
//                System.out.println("ISTS is enabled.");
//                Params.dynStrategy = true;
//            }else {
//                Params.dynStrategy = false;
//            }
//        }

        Params.dynStrategy = true;
        Params.STATIC = false;
        Params.transfer = false;

        PAS pas = new PAS(args);
        try {
            GRBEnv env = new GRBEnv();
//            env.set(GRB.IntParam.Seed,Params.SEED);
            env.set(GRB.IntParam.OutputFlag, Params.display ? 1 : 0);
            env.set(GRB.DoubleParam.TimeLimit, Params.timeLimit);
            env.set(GRB.DoubleParam.MIPGap, Params.mipGap);
            env.set(GRB.IntParam.Threads, 1);
//            Sara_LS ls = new Sara_LS(dpas.getProblem());
            PASMIP mip;
            if (Params.STATIC && Params.transfer) {
                mip = PASMIP.trMIPBuilder(pas.getProblem(), env);
            } else {
                mip = PASMIP.noTrMIPBuilder(pas.getProblem(), env);
            }
            while (pas.continuE()) {
                mip.mipModel();
                if (Params.importSol) {
                    mip.importUB();
//                    mip.importSol();
                }
//                ((MipNoTr)mip).drawPatientDecompose();
//                ((MipNoTr)mip).drawRoomDecompose();
                Solution solution = mip.solve();
                if (solution == null) {
                    throw new GRBException("Solution is null!");
                }
//                ls.localSearchLaunch();
//                Util.checkSolution(dpas.getProblem(),solution);
                pas.storeData(solution);
            }
            env.dispose();
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }
}
