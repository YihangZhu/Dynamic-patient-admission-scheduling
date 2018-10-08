/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import dpas.instance.Instance;
import dpas.instance.Patient;
import dpas.instance.Status;
import dpas.schedule.SPatient;
import dpas.schedule.ScheduleResult;
import dpas.schedule.Solution;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

/**
 * Created by zhuyi on 9/26/2016.
 *
 */
class SolutionXMLReader {
    @SuppressWarnings("unchecked")
    static Solution readXMLFile(String filePath, ScheduleResult scheduleResult, Instance instance){
        Solution solution = new Solution(scheduleResult);
        XMLEvent event;
        StartElement startElement;
        Iterator<Attribute> attributes;
        Attribute attribute;
        EndElement endElement;
        SPatient sPatient = null;
        Patient patient;
        boolean key = false;
        int count = 0;
        try{
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(
                    new FileInputStream(new File(filePath)));
            while (eventReader.hasNext()){
                event = eventReader.nextEvent();
                if (event.isStartElement()){
                    startElement = event.asStartElement();
                    if (XML.equal(startElement,"Values")){
                        while (eventReader.hasNext()){
                            event = eventReader.nextEvent();
                            if (event.isStartElement()){
                                startElement = event.asStartElement();
                                if (XML.equal(startElement,"RoomCost")){
                                    solution.setRoomCost(XML.getInt(eventReader.nextEvent()));
                                }else if(XML.equal(startElement,"GenderPolicy")){
                                    solution.setRGCost(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"TransferCost")){
                                    solution.setTrCost(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"DelayCost")){
                                    solution.setDeCost(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"OvercrowdRisk")){
                                    solution.setRiCost(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"IdleRoom")){
                                    solution.setIRCost(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"IdleOR")){
                                    solution.setORIdleTime(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"OROORTO")){
                                    solution.setOROORTO(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"Violation")){
                                    solution.setViolations(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"Objective_Value")){
                                    solution.setObjectiveValue(XML.getInt(eventReader.nextEvent()));
                                }else if (XML.equal(startElement,"Runtime")){
                                    solution.setRuntime(XML.getDouble(eventReader.nextEvent()));
                                    break;
                                }
                            }
                        }
                    }else
                    if (XML.equal(startElement,"patients_scheduling")){
                        while (eventReader.hasNext()){
                            event = eventReader.nextEvent();
                            if (event.isStartElement()){
                                startElement = event.asStartElement();
                                if (XML.equal(startElement,"patient")) {
                                    key = true;
                                    attributes = startElement.getAttributes();
                                    while (attributes.hasNext()) {
                                        attribute = attributes.next();
                                        if (XML.equal(attribute,"name")) {
                                            patient = instance.getPatients()[instance.getPatientNames().indexOf(attribute.getValue())];
                                            sPatient = new SPatient(patient);
                                        }
                                        else if (XML.equal(attribute,"status")) {
                                            assert sPatient!=null;
                                            switch (attribute.getValue()){
                                                case "discharged": sPatient.setStatus(Status.Discharged);
                                                case "registered": sPatient.setStatus(Status.Registered);
                                                case "admitted":    sPatient.setStatus(Status.Admitted);
                                            }
                                        }
//                                        else if (XML.equal(attribute,DELAY)) {
//                                            sPatient.setAdmissionDelay(Integer.parseInt(attribute.getValue()));
//                                        }
                                    }
                                    count = 0;
                                }else if (XML.equal(startElement,"stay")){
                                    assert sPatient!=null;
                                    attributes = startElement.getAttributes();
                                    int roomIndex = -1;
                                    int day = -1;
                                    while (attributes.hasNext()){
                                        attribute = attributes.next();
                                        if (XML.equal(attribute,"room")) {
                                            roomIndex = instance.getRoomNames().indexOf(attribute.getValue());
                                            count++;
                                            // check room suitability constraint
                                            assert sPatient.getFeasibleRooms().contains(roomIndex);
                                        }
                                        else if (XML.equal(attribute,"day"))
                                            day = (int) ChronoUnit.DAYS.between(instance.getDateStart(),LocalDate.parse(attribute.getValue()));
                                    }
                                    if (key){
                                        key = false;
                                        sPatient.setFinalAdmissionDay(day);
                                        //check max admission day constraint
                                        if (sPatient.getAdmissionDay() > sPatient.getMaxAD() || sPatient.getAdmissionDay() < sPatient.getEarliestAD()){
//                                            throw new IllegalArgumentException("Admission day is not feasible for patient "+sPatient.getName());
                                            System.out.println("Admission day is not feasible for patient "+sPatient.getName());
                                        }
                                    }
                                    assert roomIndex != -1;
                                    sPatient.addRoom(roomIndex);
                                    if (!sPatient.roomAvailable(roomIndex)){
                                        throw new IllegalArgumentException("The room is not feasible for patient "+sPatient.getName());
                                    }
                                }
                            }else if (event.isEndElement()){
                                assert sPatient !=null;
                                endElement = event.asEndElement();
                                if (endElement.getName().getLocalPart().equals("patient"))
                                    // check LOS constraint.
                                    if (count != sPatient.getTotalLOS()){
                                        throw new IllegalArgumentException("The length of stay is not meet for patient "+sPatient.getName());
                                    }
                                    scheduleResult.addSPatient(sPatient);
                            }
                        }
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return solution;
    }
}
