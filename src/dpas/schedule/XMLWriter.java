/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import dpas.Params;
import dpas.instance.Instance;
import util.Util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.time.LocalDate;

public class XMLWriter {

    public static void recordData(String path, Solution solution, Instance instance) {
        ScheduleResult scheduleResult = solution.getScheduleResult();
        double te = (double) (System.currentTimeMillis() - Params.stateTime) / 1000;
        int epn = Params.ePatientNum / scheduleResult.getCurrentDate();
        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element OrPasu = document.createElement("OrPasu_main_out");
            document.appendChild(OrPasu);

            Attr instanceAttr = document.createAttribute("instance");
            instanceAttr.setValue(scheduleResult.getName());
            OrPasu.setAttributeNode(instanceAttr);

            Element planningHorizon = document.createElement("planning_horizon");
            OrPasu.appendChild(planningHorizon);

            Element startDay = document.createElement("start_day");
            startDay.appendChild(document.createTextNode(scheduleResult.getStartDate().toString()));
            planningHorizon.appendChild(startDay);

            Element numDays = document.createElement("num_days");
            numDays.appendChild(document.createTextNode(Integer.toString(scheduleResult
                    .getExtendedHorizonDays())));
            planningHorizon.appendChild(numDays);

            Element currentDay = document.createElement("current_day");
            currentDay.appendChild(document.createTextNode(scheduleResult.getStartDate().plusDays
                    (scheduleResult.getCurrentDate()).toString()));
            planningHorizon.appendChild(currentDay);

            Element eValues = document.createElement("Values");
            OrPasu.appendChild(eValues);

            Element eRoomCosts = document.createElement("Room_Costs");
            eValues.appendChild(eRoomCosts);

            for (int i = 0; i < Params.getRoomCostNames().length; i++) {
                Attr eCost = document.createAttribute(Params.getRoomCostNames()[i].toString());
                eCost.setValue(Integer.toString(solution.getRoomCostContainer()[i]));
                eRoomCosts.setAttributeNode(eCost);
            }

            for (int i = 0; i < Params.getCostNames().length - 2; i++) {
                Element eCost = document.createElement(Params.getCostNames()[i].toString());
                eCost.appendChild(document.createTextNode(Integer.toString(solution
                        .getCostContainer()[i])));
                eValues.appendChild(eCost);
            }
            Element eViolation = document.createElement("Violation");
            eViolation.appendChild(document.createTextNode(Integer.toString(solution
                    .getViolations())));
            eValues.appendChild(eViolation);

            Element eObjectiveValue = document.createElement("Objective_Value");
            eObjectiveValue.appendChild(document.createTextNode(Integer.toString(solution
                    .getObjectiveValue())));
            eValues.appendChild(eObjectiveValue);

            Element eRunTime = document.createElement("Runtime");
            eRunTime.appendChild(document.createTextNode(String.format("%f", te)));
            eValues.appendChild(eRunTime);

            Element ePatientScheduling = document.createElement("patients_scheduling");
            OrPasu.appendChild(ePatientScheduling);

            for (int p = 0; p < instance.getPatientNum(); p++) {
                SPatient sPatient = scheduleResult.getSPatients(p);
                if (sPatient != null) {
                    Element epatient = document.createElement("patient");
                    ePatientScheduling.appendChild(epatient);

                    Attr name = document.createAttribute("name");
                    name.setValue(sPatient.getName());
                    epatient.setAttributeNode(name);
/*
                    Attr status = document.createAttribute("status");
                    status.setValue(sPatient.getStatus().toString());
                    epatient.setAttributeNode(status);
                    if (sPatient.getAdDelay() != 0) {
                        Attr delay = document.createAttribute("delay");
                        delay.setValue(Integer.toString(sPatient.getAdDelay()));
                        epatient.setAttributeNode(delay);
                    }
*/
                    LocalDate date = scheduleResult.getStartDate().plusDays(sPatient
                            .getAdmissionDay());
                    int LOS = sPatient.getDischargeDay() - sPatient.getAdmissionDay();
                    for (int day = 0; day < LOS; day++) {
                        int roomIndex = sPatient.getRooms(day);
                        Element stay = document.createElement("stay");
                        epatient.appendChild(stay);

                        Attr room = document.createAttribute("room");
                        room.setValue(instance.getRoomNames().get(roomIndex));
                        stay.setAttributeNode(room);

                        Attr dayAttr = document.createAttribute("day");
                        dayAttr.setValue(date.plusDays(day).toString());
                        stay.setAttributeNode(dayAttr);
                    }
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(3));

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(path));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

            if (Params.display) {
                Util.printResults(solution);
            }
            if (Params.STATIC) {
                System.out.print("The static");
            } else {
                System.out.print("The dynamic");
            }
            System.out.println(" result in\t" + path
                    + "\tviolations:\t" + solution.getViolations()
                    + "\ttotal ObjVal:\t" + solution.getObjectiveValue()
                    + "\truntime:\t" + te
                    + "\taverage_patient_number:\t" + epn);

        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
    }
}
