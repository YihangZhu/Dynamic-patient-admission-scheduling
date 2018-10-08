/*
 * Copyright (c) 3/16/18 3:59 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import algorithm.ShortestPath;
import algorithm.columnGeneration.PricingEAbs;
import gurobi.*;
import dpas.Params;
import dpas.instance.Instance;
import dpas.schedule.Problem;
import dpas.schedule.SPatient;
import util.Graph;
import util.Util;

import java.util.Collections;
import java.util.LinkedList;

public class PricingNSolver extends PricingEAbs<MasterPatient,ColumnPatient> {

    private Instance instance;
    private SPatient patient;
    private Graph[] graphsTemp;
    private Graph[] graphs;
    private int V;
    private int numRoom;
    private LinkedList<ColumnPatient> columns;
    PricingNSolver(MasterPatient master, Problem problem, int ID) {
        super(ID, master);
        instance = problem.getInstance();
        patient = problem.getPatientSet().get(ID);
        createModel(null);
    }

    @Override
    public boolean solve() throws GRBException {
        double runtime = (double) System.currentTimeMillis();
        boolean columnFound = false;
        updateObj();
        columns.clear();
        for (int d = 0; d < graphs.length; d++) {
            ShortestPath.BellmanFord(graphs[d], 0);

            int[] rooms = new int[patient.getRestLOS()];
            int u = V-1;

            int los = patient.getRestLOS()-1;
            while (los>=0){
                int v = ShortestPath.getPred()[u];
                int index = v - 1 - los*numRoom;
                rooms[los] = patient.getFeasibleRooms().get(index);
                los--;
                u = v;
            }

            if (ShortestPath.getPred()[patient.getFeasibleRooms().indexOf(rooms[0])+1] != 0){
                throw new IllegalMonitorStateException("The shortest path is not correct!");
            }

            double objVal = ShortestPath.getDist()[V - 1];

            if (objVal < -Util.EPS) {
                newColumn = new ColumnPatient(ID, d, rooms);
                newColumn.setReducedCost(objVal);
                computeColCost(newColumn);
                columns.add(newColumn);
            }
        }
        Collections.sort(columns);
        int cNum = Math.min(master.getAddColumnNum(),columns.size());
        for (int c = 0; c < cNum; c++) {
            if (master.addColumn(columns.get(c))) {
                if (master.printColumn()){
                    System.out.println(columns.get(c));
                }
                columnFound = true;
            }
        }
        master.countEPricingTime(System.currentTimeMillis() - runtime);
        return columnFound;
    }

    public void createModel(GRBEnv env) {
        columns = new LinkedList<>();
        numRoom = patient.getFeasibleRooms().size();
        graphsTemp = new Graph[patient.getMaxDelay()+1]; // 0, 1, ..., MaxDelay,
        // graphsTemp store basic weight for each graph
        // Each time graph weights are updated by copying basic weight from
        // graphsTemp and latest dual values.
        V = 2+patient.getRestLOS()*numRoom; //a sink a source, restLOS layers.
        int E = 2*numRoom + (int)(Math.pow(numRoom,2))*(patient.getRestLOS()-1);
        for (int d = 0; d < graphsTemp.length; d++) {
            graphsTemp[d] = new Graph(V, E);
            int e = 0;
            //from source to first layer //
            for (int i = 0; i < numRoom; i++, e++) {
                int weight = d*Params.DELAY +
                        patient.getRoomCost(patient.getFeasibleRooms().get(i));
                if (!patient.isRegistered()){
                    weight += Params.TRANSFER*patient.transfer(patient.getFeasibleRooms().get(i));
                }
                int dest = i+1;
                graphsTemp[d].getEdge(e).setSrcDestWeight(0, dest, weight);
            }
            //the next los-1 layer
            for (int dd = 1; dd < patient.getRestLOS(); dd++) {
                for (int i = 0; i < numRoom; i++) {
                    int src = i + (dd-1)*numRoom + 1; // 1 is for the source
                    for (int j = 0; j < numRoom; j++, e++) {
                        int dest = j + dd*numRoom + 1;
                        int weight = patient.getRoomCost(patient.getFeasibleRooms().get(j));
                        if (j != i){
                            weight += Params.TRANSFER;
                        }
                        graphsTemp[d].getEdge(e).setSrcDestWeight(src,dest,weight);
                    }
                }
            }

            //the last layer
            for (int i = 0; i < numRoom; i++, e++) {
                int src = i + (patient.getRestLOS()-1)*numRoom + 1;
                graphsTemp[d].getEdge(e).setSrcDestWeight(src, V-1,0);
            }
            if (graphsTemp[d].getEdge(e-1).getSrc()!=V-2){
                throw new IllegalMonitorStateException("The last vertex is not correct!\t"
                        + graphsTemp[d].getEdge(e-1).getSrc());
            }
            if (e!=E){
                throw new IllegalMonitorStateException("The edge number is not correct! \t"
                        + e);
            }
        }

        graphs = new Graph[patient.getMaxDelay()+1];
        for (int d = 0; d < graphs.length; d++) {
            graphs[d] = new Graph(graphsTemp[d]);
        }
    }


    public void updateObj() {
        master.getConstraints().updateNetwork(this);
    }

    public double getColumn() {
        return computeObj(newColumn);
    }

    @Override
    public double computeObj(ColumnPatient column) {
        computeColCost(column);
        return master.getConstraints().validateReducedCost(patient, column) +
                column.getCost();
    }

    private void computeColCost(ColumnPatient that){
        int roomCost = 0, trCost = 0, deCost = 0;

        for (int d = 0; d < patient.getRestLOS(); d++) {
            roomCost += patient.getRoomCost(that.getRooms()[d]);
        }
        if (patient.isRegistered()) {
            deCost += that.getDelay() * Params.DELAY;
        } else {
            trCost += patient.transfer(that.getRooms()[0]) * Params.TRANSFER;
        }
        for (int d = 1; d < patient.getRestLOS(); d++) {
            if (that.getRooms()[d - 1] != that.getRooms()[d]) {
                trCost += Params.TRANSFER;
            }
        }

        that.setCost(trCost + roomCost + deCost);
    }

    @Override
    public void dispose() {}

    public Instance getInstance() {
        return instance;
    }

    public SPatient getPatient() {
        return patient;
    }

    Graph[] getGraphsTemp() {
        return graphsTemp;
    }

    Graph[] getGraphs() {
        return graphs;
    }
}
