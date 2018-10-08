///*
// * Copyright (c) $today.date.4.2017
// * Author: Yihang Zhu
// */
//
//package dpas.algoritm.mp;
//
//import gurobi.GRB;
//import gurobi.GRBException;
//import dpas.Params;
//import dpas.algoritm.mp.cg.patientBased.cps.Patterns;
//import dpas.algoritm.mp.cg.roomBased.cps.Pattern;
//import dpas.instance.Instance;
//import dpas.schedule.Problem;
//import dpas.schedule.SPatient;
//import util.Util;
//
//public class CheckSol {
//    private double[][] roomAssign;
//    private double[][] roomPAssign;
//    private double[][] roomMAssign;
//    private double[][] roomFAssign;
//    private double[][] orAssign;
//    private double[] orDAssign;
//    private Problem problem;
//    private Instance instance;
//    private int currentDay;
//    private double ptCost=0;
//    private double roomCosts = 0;
//    private double trCosts = 0;
//    private double deCosts = 0;
//    private double roomIdle = 0;
//    private double roomGender = 0;
//    private double roomRi = 0;
//    private double orSpecOver = 0;
//    private double orDayOver = 0;
//    private double orIdle = 0;
//
//    private double[][][] x;
//    private double[][][] y;
//
//    @SuppressWarnings("unused")
//    public CheckSol(Problem problem, Pattern[] patterns, double objVal)throws GRBException{
//        initialize(problem);
//        importResult(patterns);
//        validate();
//        validateOBJR(objVal);
//    }
//    @SuppressWarnings("unused")
//    public CheckSol(Problem problem, Patterns[] patterns, double objVal)throws GRBException{
//        initialize(problem);
//        importResult(patterns);
//        validate();
//        validateOBJP(objVal);
//    }
//
//    @SuppressWarnings("unused")
//    CheckSol(Problem problem, int[] de, int[][] rooms, double objVal)throws GRBException{
//        initialize(problem);
//        importResults(de,rooms);
//        validate();
//        validateOBJ(objVal);
//    }
//    @SuppressWarnings("unused")
//    CheckSol(Problem problem, int[] de, int[] rooms, double objVal) throws GRBException{
//        initialize(problem);
//        importResults(de, rooms);
//        validate();
//        validateOBJ(objVal);
//    }
//
//    private void initialize(Problem problem){
//        this.problem = problem;
//        currentDay = problem.getCurrent();
//        instance = problem.getInstance();
//        roomAssign = new double[instance.getRoomNum()][instance.getNumDays()];
//        roomPAssign = new double[instance.getRoomNum()][instance.getNumDays()];
//        roomMAssign = new double[instance.getRoomNum()][instance.getNumDays()];
//        roomFAssign = new double[instance.getRoomNum()][instance.getNumDays()];
//        orAssign = new double[instance.getNumDays()][instance.getSpecNum()];
//        orDAssign = new double[instance.getNumDays()];
//    }
//
//    private void importResult(Patterns[] patterns)throws GRBException{
//        x = new double[patterns.length][instance.getRoomNum()][instance.getNumDays()];
//        y = new double[patterns.length][instance.getRoomNum()][instance.getNumDays()];
//        for (Patterns pattern : patterns) {
//            double totVal = 0;
//            for (int c = 0; c < pattern.getColumnNum(); c++) {
//                double val = pattern.getColumn(c).getVar().get(GRB.DoubleAttr.X);
//                if (val > Util.EPS) {
//                    totVal += val;
//                    SPatient patient = problem.getPatientSet().get(pattern.getID());
//                    ptCost += val * pattern.getColumn(c).getCost();
//
//                    int[] rooms = pattern.getColumn(c).getRooms();
//                    int ad = pattern.getColumn(c).getDelay() + patient.getEarliestAD();
//                    assignRoom(rooms,ad,val,patient);
//
//                    for (int d = 0; d < patient.getRestLOS(); d++) {
//                        x[pattern.getID()][rooms[d]][ad+d] += val;
//                    }
//                    if (patient.getVariablity()>0 && ad + patient.getRestLOS()<instance
// .getNumDays()){
//                        y[pattern.getID()][rooms[patient.getRestLOS()-1]][ad + patient
// .getRestLOS()] += val;
//                    }
//
//                    if (patient.needSurgery()) {
//                        int sd = pattern.getColumn(c).getDelay() + patient.getEarliestSD();
//                        assignOR(sd,val,patient);
//                    }
//                }
//            }
//            if (totVal + Util.EPS < 1){
//                for (int c = 0; c< pattern.getColumnNum();c++){
//                    System.out.println(String.format("Col: %d, %f",c,pattern.getColumn(c)
// .getVar().get(GRB.DoubleAttr.X)));
//                }
//                throw new GRBException("Convexity constraint problem:\t"+totVal);
//            }
//        }
//    }
//
//    private void importResult(Pattern[] patterns)throws GRBException{
//        double[] patientVal = new double[problem.getPatientSet().size()];
//        ptCost -= Params.IDLE_ROOM_CAPACITY*instance.redundantRoomCap();
//        for (Pattern pattern : patterns) {
//            double totVal = 0;
//            for (int c = 0; c < pattern.getColumnNum(); c++) {
//                double val = pattern.getColumn(c).getVar().get(GRB.DoubleAttr.X);
//                if (val > Util.EPS) {
//                    totVal += val;
//                    for (int p = 0; p < patientVal.length; p++) {
//                        SPatient patient = problem.getPatientSet().get(p);
//                        if (pattern.getColumn(c).getDe()[p] != -1){
//                            patientVal[p] += val;
//                            int room = pattern.getColumn(c).getID();
//                            int ad = pattern.getColumn(c).getDe()[p] + patient.getEarliestAD();
//                            assignRoom(room,ad,val,patient);
//                            if (patient.needSurgery()) {
//                                int sd = pattern.getColumn(c).getDe()[p] + patient
// .getEarliestSD();
//                                assignOR(sd,val,patient);
//                            }
//                        }
//                    }
//                    ptCost += val * pattern.getColumn(c).getCost();
//                }
//            }
//            if (totVal + Util.EPS < 1){
//                for (int c = 0; c< pattern.getColumnNum();c++){
//                    System.out.println(String.format("Col: %d, %f",c,pattern.getColumn(c)
// .getVar().get(GRB.DoubleAttr.X)));
//                }
//                throw new GRBException("Convexity constraint problem:\t"+totVal);
//            }
//        }
//        for (double aPatientVal : patientVal) {
//            if (aPatientVal + Util.EPS < 1) {
//                throw new GRBException("one room constraint violated!");
//            }
//        }
//    }
//
//    private void importResults(int[] de, int[][] rooms){
//        x = new double[de.length][instance.getRoomNum()][instance.getNumDays()];
//        for (int p = 0; p < de.length; p++) {
//            SPatient patient = problem.getPatientSet().get(p);
//            if (rooms[p][0] != patient.getPreRoomIndex() && patient.getPreRoomIndex() != -1) {
//                trCosts ++;
//            }
//            for (int i = 1; i < patient.getRestLOS(); i++) {
//                if (rooms[p][i]!=rooms[p][i-1]){
//                    trCosts ++;
//                }
//            }
//            deCosts += de[p] * patient.getPriority();
//            for (int i = 0; i < patient.getRestLOS(); i++) {
//                roomCosts += patient.getRoomCost(rooms[p][i]);
//            }
//            int ad = de[p] + patient.getEarliestAD();
//            assignRoom(rooms[p],ad,1,patient);
//            if (patient.needSurgery()){
//                int sd = de[p] + patient.getEarliestSD();
//                assignOR(sd,1,patient);
//            }
//        }
//        trCosts *= Params.TRANSFER;
//        deCosts *= Params.DELAY;
//    }
//
//    private void importResults(int[] de, int[] rooms){
//        for (int p = 0; p < de.length; p++) {
//            SPatient patient = problem.getPatientSet().get(p);
//            if (rooms[p] != patient.getPreRoomIndex() && patient.getPreRoomIndex() != -1) {
//                trCosts ++;
//            }
//            deCosts += de[p] * patient.getPriority();
//            roomCosts += patient.getRoomCosts(rooms[p]);
//            int ad = de[p] + patient.getEarliestAD();
//            assignRoom(rooms[p],ad,1,patient);
//            if (patient.needSurgery()){
//                int sd = de[p] + patient.getEarliestSD();
//                assignOR(sd,1,patient);
//            }
//        }
//        trCosts *= Params.TRANSFER;
//        deCosts *= Params.DELAY;
//    }
//
//    private void assignRoom(int[]rooms, int ad, double val, SPatient patient){
//        for (int i = 0; i < patient.getRestLOS(); i++) {
//            int d = i + ad;
//            assignRoomDay(rooms[i],d,val,patient);
//        }
//        int dd = ad + patient.getRestLOS();
//        if (patient.getVariablity() > 0 && dd < roomAssign[0].length) {
//            roomPAssign[rooms[patient.getRestLOS()-1]][dd] += val;
//        }
//    }
//
//    private void assignRoom(int room, int ad, double val, SPatient patient){
//        for (int i = 0; i < patient.getRestLOS(); i++) {
//            int d = i + ad;
//            assignRoomDay(room,d,val, patient);
//        }
//        int dd = ad + patient.getRestLOS();
//        if (patient.getVariablity() > 0 && dd < roomAssign[0].length) {
//            roomPAssign[room][dd] += val;
//        }
//    }
//
//    private void assignRoomDay(int roomID, int day, double val, SPatient patient){
//        roomAssign[roomID][day] += val;
//        roomPAssign[roomID][day] += val;
//        if (instance.roomForSG(roomID)){
//            if (patient.isMale()){
//                roomMAssign[roomID][day] += val;
//            }else {
//                roomFAssign[roomID][day] += val;
//            }
//        }
//    }
//
//    private void assignOR(int sd, double val, SPatient patient){
//        if (patient.isElective()) {
//            orAssign[sd][patient.getSpec()] += val * patient.getSurDur();
//        }
//        orDAssign[sd] += val * patient.getSurDur();
//    }
//
//    private void validate()throws GRBException{
//        for (int r = 0; r<roomAssign.length;r++){
//            int capacity = instance.getRoomCapacity(r);
//            for (int d = currentDay; d<roomAssign[r].length;d++){
//                if (roomAssign[r][d] - capacity - Util.EPS>0){
//                    throw new GRBException("Room capacity is violated!");
//                }
//
//                roomRi += Math.max(0, roomPAssign[r][d] - capacity);
//            }
//            if (instance.roomForSG(r)){
//                for (int d = currentDay;d<roomMAssign[r].length;d++){
////                    roomGender += Math.max(0,roomMAssign[r][d]/(double) capacity +
// roomFAssign[r][d]/(double) capacity - 1);
////                    roomGender += Math.min(roomMAssign[r][d],roomFAssign[r][d])>0? 1:0;
//                    double f = 0, m  = 0;
//                    for (int p = 0; p <problem.getPatientSet().size() ; p++) {
//                        if (problem.getPatientSet().get(p).isMale()) {
//                            m = Math.max(m, x[p][r][d]);
//                        }else {
//                            f = Math.max(f, x[p][r][d]);
//                        }
//                    }
//                    roomGender += Math.max(0,m+f-1);
//                }
//            }
//        }
//
//        roomRi *= Params.OVERCROWD_RISK;
//        roomGender *= Params.GENDER_POLICY;
//
//        for (int d = currentDay;d<orAssign.length;d++){
//            for (int s = 0; s<orAssign[d].length;s++){
//
//                if(orAssign[d][s] - instance.getTotORSpecCap(d,s) - Util.EPS>0){
//                    throw new GRBException("The or capacity is violated!");
//                }
//                orSpecOver += Math.max(0, orAssign[d][s] - instance.getORSpecCapacity(d,s));
//            }
//
//            if (orDAssign[d] - instance.getTotORDayCap(d) - Util.EPS>0){
//                throw new GRBException("The or day capacity is violated!");
//            }
//            orDayOver += Math.max(0,orDAssign[d] - instance.getORDayCapacity(d));
//        }
//
//        orSpecOver *= Params.OVERTIME;
//        orDayOver *= Params.OVERTIME;
//
//        if (Params.STATIC) {
//            getIdleRoom(instance.maximumRoomUsage());
//            getIdleOR(instance.maximumORUsage());
//        }else {
//            getIdleRoom(instance.getTotalRoomCapacity(currentDay));
//            getIdleOR(instance.getTotalORCapacity(currentDay));
//        }
//    }
//
//    private void getIdleRoom(int initialRoomCap){
//        roomIdle = initialRoomCap;
//        double totalRoomOccupancy = 0;
//        for (double[] vars : roomAssign) {
//            for (int d = currentDay; d < instance.getHorizon(); d++) {
//                totalRoomOccupancy += vars[d];
//            }
//        }
//        roomIdle -= totalRoomOccupancy;
//
//        roomIdle *= Params.IDLE_ROOM_CAPACITY;
//    }
//
//    private void getIdleOR(int initialORCap){
//        orIdle =  initialORCap;
//        double totalOROccupancy = 0;
//        for (int d = currentDay; d < instance.getHorizon(); d++) {
//            for (int s = 0; s < orAssign[d].length; s++) {
//                totalOROccupancy += orAssign[d][s];
//                orIdle += Math.max(0, orAssign[d][s] - instance.getORSpecCapacity(d,s));
//            }
//        }
//        orIdle -= totalOROccupancy;
//
//        orIdle *= Params.IDLE_OPERATING_ROOM;
//    }
//
//    private void validateOBJR(double objVal)throws GRBException{
//        double obj = ptCost + orIdle + orSpecOver + orDayOver;
//        System.out.println("ObjVal:\t"+ obj);
//        System.out.println("PatternCost:\t" + ptCost
//                + "\tIdleOR: " + orIdle
//                + "\tovertime: " + orSpecOver
//                + "\tovertime: " + orDayOver);
//        Util.isUnequal(objVal,obj);
//    }
//
//    private void validateOBJP(double objVal)throws GRBException{
//        double obj = ptCost + roomGender + roomRi + roomIdle +
//                orIdle + orSpecOver + orDayOver;
//        System.out.println("ObjVal:\t"+ obj);
//        System.out.println("PatternCost:\t" + ptCost
//                + "\tGenderPolicy: "+ roomGender
//                + "\tOverCrowdRisk: "+ roomRi
//                + "\tIdleRoom: "+roomIdle
//                + "\tIdleOR: " + orIdle
//                + "\tovertime: " + orSpecOver
//                + "\tovertime: " + orDayOver);
//        Util.isUnequal(objVal,obj);
//    }
//
//    private void validateOBJ(double objVal)throws GRBException{
//        double obj = roomCosts + deCosts + trCosts + roomGender +
//                roomRi + roomIdle +
//                orIdle + orSpecOver + orDayOver;
//        System.out.println("ObjVal:\t"+ obj);
//        System.out.println("\troomCost:\t" + roomCosts
//                + "\ttransferCost:\t" + trCosts
//                + "\tdelayCost:\t" + deCosts
//                + "\tGenderPolicy: "+ roomGender
//                + "\tOverCrowdRisk: "+ roomRi
//                + "\tIdleRoom: "+roomIdle
//                + "\tIdleOR: " + orIdle
//                + "\tovertime: " + orSpecOver
//                + "\tovertime: " + orDayOver);
//        Util.isUnequal(objVal,obj);
//    }
//
////    @SuppressWarnings("unused")
////    private double getDynamicIdleOR(){
////        orIdle = instance.getTotalORCapacity(currentDay);
////        double totalOROccupancy = 0;
////        for (int d = currentDay; d < instance.getHorizon(); d++) {
////            totalOROccupancy += orDAssign[d];
////            orIdle += Math.max(0, orDAssign[d] - instance.getORDayCapacity(d));
////        }
////        orIdle -= totalOROccupancy;
////        return orIdle;
////    }
//
//
//    public double[][][] getX() {
//        return x;
//    }
//
//    public double[][][] getY() {
//        return y;
//    }
//}
