/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas;

import dpas.schedule.CostNames;

/**
 * Created by zhuyi on 11/4/2016.
 * For parameters setting.
 */
public class Params {
    private static CostNames[] costNames;
    private static CostNames[] roomCostNames;

    public static void initialize() {
        costNames = new CostNames[10];
        roomCostNames = new CostNames[4];
        costNames[0] = CostNames.RoomCost;
        costNames[1] = CostNames.GenderPolicy;
        costNames[2] = CostNames.TransferCost;
        costNames[3] = CostNames.DelayCost;
        costNames[4] = CostNames.OvercrowdRisk;
        costNames[5] = CostNames.IdleRoom;
        costNames[6] = CostNames.IdleOR;
        costNames[7] = CostNames.OROORTO;
        costNames[8] = CostNames.OverRC;
        costNames[9] = CostNames.ORUORTU;
        roomCostNames[0] = CostNames.FeaturesCost;
        roomCostNames[1] = CostNames.PreferenceCost;
        roomCostNames[2] = CostNames.SpecialismCost;
        roomCostNames[3] = CostNames.FixedGenderCost;
        stateTime = System.currentTimeMillis();
        updatePath();
    }

    public static CostNames[] getCostNames() {
        return costNames;
    }

    public static CostNames[] getRoomCostNames() {
        return roomCostNames;
    }

    // cost weights
    public final static int ROOM_PROPERTY = 20;
    public final static int ROOM_PREFERENCE = 10;
    public final static int SPECIALISM = 20;
    public final static int GENDER_POLICY = 50;
    public final static int TRANSFER = 100;
    public static int DELAY = 5;       //(X day)
    public final static int OVERCROWD_RISK = 1;
    public final static int IDLE_OPERATING_ROOM = 10; // (X minute)
    public final static int IDLE_ROOM_CAPACITY = 20; //(X day)
    public final static int OVERTIME = 3;  //(X minute)
    public final static int HARD_CONSTRAINTS = 10000;
    public final static int ADMITTED_OVERTIME = 30;  // the admitted overtime per specialism each
    // day.
    public final static int ADMITTED_TOTAL_OVERTIME = 60; // the totally admitted overtime for
    // day OR.
    public final static int REPEATED_HORIZONTAL = 2;
    //statistic
    public static long stateTime;
    public static long dayStateTime;
    public static int ePatientNum = 0;
    //run
    public static int SEED = 0;

    public static String instanceFilePath;
    static String resultsFilePath;
    public static String importSolutionPath;

    public static String instanceName = "4_short03";
    public static String result =  "";
    private static void updatePath() {
        instanceFilePath = "./Instances/or_pas_dept" + instanceName + ".xml";
            resultsFilePath = "./solutions/" + result + "or_pas_dept" + instanceName + "_sol.xml";
        importSolutionPath = "./solutions/mipStatic/or_pas_dept" + instanceName + "_sol.xml";
    }

    public static boolean importSol = false;
    public static boolean display = false;
    public static boolean printColumn = false;
    public static boolean STATIC = false;
    public static boolean transfer = false;
    public static boolean suddenTr = true;

    public static boolean dynStrategy = true;

    public final static double penaltyDecay = 0.95;
    public final static double a = 4;
    public final static double b = 150;

    public static String genderPolicyConstr = "5"; // 4 5 6
    public static double timeLimit = 3600;
    public static double mipGap = 1e-4;
}



