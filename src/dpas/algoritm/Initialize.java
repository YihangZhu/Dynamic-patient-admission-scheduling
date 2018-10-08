/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm;

import dpas.instance.Instance;
import dpas.instance.Status;
import dpas.schedule.SPatient;
import dpas.schedule.Solution;
import util.Util;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by zhuyi on 2/8/2017.
 */
public class Initialize {
    private ArrayList<SPatient> unadmittedPatients;
    private ArrayList<SPatient> registeredPatients;
    private Instance instance;
    private Random random = Util.rand;

    public Initialize(Instance instance, ArrayList<SPatient> unadmittedPatients,
                      ArrayList<SPatient> registeredPatients) {
        this.instance = instance;
        this.unadmittedPatients = unadmittedPatients;
        this.registeredPatients = registeredPatients;
    }

    public Solution zeroInitialize() {
        int[] admissionDelays = new int[unadmittedPatients.size()];
        int[] roomAssignments = new int[registeredPatients.size()];
        for (int i = 0; i < registeredPatients.size(); i++) {
            SPatient sPatient = registeredPatients.get(i);
            if (i < unadmittedPatients.size()) {
                admissionDelays[i] = 0;
            }
            int roomIndex;

            if (sPatient.getPreRoomIndex() != -1) {
                roomIndex = sPatient.getPreRoomIndex();
                //System.out.println(sPatient.getRoomCost(roomIndex));
            } else {
                assert sPatient.getStatus().equals(Status.Registered);
                do {
                    roomIndex = random.nextInt(instance.getRoomNum());
                } while (!sPatient.roomAvailable(roomIndex));

            }
            roomAssignments[i] = roomIndex;
        }

        return new Solution(admissionDelays, roomAssignments);
    }

//    public Solution randomInitialize(Solution solution){
//        int[] admissionDelays = new int[unadmittedPatients.size()];
//        int[] roomAssignments = new int[registeredPatients.size()];
//        solution.setAdmissionDelays(admissionDelays);
//        solution.setRoomAssignments(roomAssignments);
//        for (int i =0; i < registeredPatients.size(); i++) {
//            SPatient sPatient = registeredPatients.get(i);
//            if (i<unadmittedPatients.size()) {
//                admissionDelays[i] = random.nextInt(sPatient.getMaxDelay()+1);
//            }
//            int  roomIndex = sPatient.getFeasibleRooms().get(random.nextInt(sPatient
// .getFeasibleRooms().size()));
//            roomAssignments[i] = roomIndex;
//        }
//        //solutionCalculation.assign(solution);
//        //solutionCalculation.objectiveValueCalculation(solution);
//        return solution;
//    }

//    public Solution greedyWay(ValuesStorage storage, EvaOperations evaOpera){
//        int[] admissionDelays = new int[unadmittedPatients.size()];
//        int[] roomAssignments = new int[registeredPatients.size()];
//        ArrayList<SPatient> sPatientsTemp = new ArrayList<>(registeredPatients);
//        sPatientsTemp.sort(new PatientComparator1());
//        for (SPatient patient : sPatientsTemp){
//            Collections.shuffle(patient.getFeasibleRooms(),random);
//            int[] temp = evaOpera.roomAssignment(patient,patient.getFeasibleRooms(),storage);
//            roomAssignments[registeredPatients.indexOf(patient)] = temp[0];
//            patient.setRoomIndexTemp(temp[0]);
//            if (patient.getStatus().equals(Status.Registered)) {
//                int delay = temp[1] - patient.getEarliestAD();
//                admissionDelays[registeredPatients.indexOf(patient)] = delay;
//                patient.setDelayTemp(delay);
//            }
//        }
//        return new Solution(admissionDelays,roomAssignments);
//    }
}
