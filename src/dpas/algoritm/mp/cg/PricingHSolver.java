/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import algorithm.columnGeneration.PricingHAbs;
import gurobi.GRBEnv;
import gurobi.GRBException;
import dpas.schedule.Problem;
import util.Util;

public class PricingHSolver extends PricingHAbs<MasterPatient,ColumnPatient,PricingNSolver> {
    PricingHSolver(MasterPatient master, GRBEnv env, Problem problem, int ID) throws GRBException {
//        es = new PricingESolver(master, env, problem, ID);
        es = new PricingNSolver(master, problem, ID);
        maxIteration = 3 * es.getInstance().getPatientNum();
    }

    @Override
    public boolean solve() throws GRBException {
        return es.solve();
    }

    @Override
    public ColumnPatient initSolution() {
        int[] rooms = new int[es.getPatient().getRestLOS()];
        int r = es.getPatient().getFeasibleRooms().
                get(Util.rand.nextInt(es.getPatient().getFeasibleRooms().size()));

        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = r;
        }
        int delay = Util.rand.nextInt(es.getPatient().getMaxDelay() + 1);
        return new ColumnPatient(es.getID(), delay, rooms);
    }

    @Override
    public ColumnPatient searchColumn() {
        double rand = Util.rand.nextDouble();
        if (rand < 0.3 && es.getPatient().getMaxDelay() > 0) {
            return shiftDelay(bestColumn);
        } else if (rand < 0.5 && es.getPatient().getFeasibleRooms().size() > 1) {
            return changeOneRoom(bestColumn);
        } else if (es.getPatient().getFeasibleRooms().size() > 1) {
            return changeAllRoom(bestColumn);
        } else {
            return null;
        }
    }

    private ColumnPatient shiftDelay(ColumnPatient column) {
        do {
            int delay = Util.rand.nextInt(es.getPatient().getMaxDelay() + 1);
            if (delay != column.getDelay()) {
                return new ColumnPatient(es.getID(), delay, column.getRooms().clone());
            }
        } while (true);
    }

    private ColumnPatient changeOneRoom(ColumnPatient column) {
        do {
            int[] rooms = column.getRooms().clone();
            int d = Util.rand.nextInt(es.getPatient().getRestLOS());
            int r = es.getPatient().getFeasibleRooms().get(Util.rand.nextInt(es.getPatient()
                    .getFeasibleRooms().size()));
            if (rooms[d] != r) {
                return new ColumnPatient(es.getID(), column.getDelay(), rooms);
            }
        } while (true);
    }

    private ColumnPatient changeAllRoom(ColumnPatient column) {
        do {
            int[] rooms = new int[column.getRooms().length];
            int r = es.getPatient().getFeasibleRooms().
                    get(Util.rand.nextInt(es.getPatient().getFeasibleRooms().size()));

            if (rooms[0] != r) {
                for (int i = 0; i < rooms.length; i++) {
                    rooms[i] = r;
                }
                return new ColumnPatient(es.getID(), column.getDelay(), rooms);
            }
        } while (true);
    }
}
