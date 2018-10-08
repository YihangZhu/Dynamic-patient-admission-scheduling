/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.algoritm.mp.cg;

import gurobi.GRB;
import gurobi.GRBException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import dpas.schedule.SPatient;
import util.Util;
import util.XML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by yihang on 6/12/17.
 */

@SuppressWarnings("unused")
class ColumnXML {
    public static void recordData(String path, LinkedList<ColumnPatient>[] columnSet) {

        try {

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element docPattern = document.createElement("Columns_for_patterns");
            document.appendChild(docPattern);

            for (int p = 0; p < columnSet.length; p++) {
                Element ePattern = document.createElement("pattern" + p);
                docPattern.appendChild(ePattern);

                for (ColumnPatient column : columnSet[p]) {
                    if (column.getVar().get(GRB.DoubleAttr.X) > Util.EPS) {
                        Element col = document.createElement("column");
                        ePattern.appendChild(col);

                        Attr cost = document.createAttribute("cost");
                        cost.setValue(Double.toString(column.getCost()));
                        col.setAttributeNode(cost);

                        Attr delay = document.createAttribute("delay");
                        delay.setValue(Integer.toString(column.getDelay()));
                        col.setAttributeNode(delay);

                        int[] rooms = column.getRooms();
                        for (int d = 0; d < rooms.length; d++) {
                            Attr room = document.createAttribute("room" + d);
                            room.setValue(Integer.toString(rooms[d]));
                            col.setAttributeNode(room);
                        }
                    }
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(path));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);

        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        } catch (GRBException e) {
            e.getErrorCode();
        }
    }

    @SuppressWarnings("unchecked")
    public static void readXMLFile(String path, ArrayList<SPatient> patientSet, MasterPatient master, boolean
                                    add) {
        XMLEvent event;
        StartElement startElement;
        Iterator<Attribute> attributes;
        Attribute attribute;
        EndElement endElement;

        int ID = 0;
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
            XMLEventReader eventReader = xmlInputFactory.createXMLEventReader(new FileInputStream
                    (new File(path)));
            while (eventReader.hasNext()) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, "pattern" + ID)) {
                        SPatient patient = patientSet.get(ID);
                        while (eventReader.hasNext()) {
                            event = eventReader.nextEvent();
                            if (event.isStartElement()) {
                                startElement = event.asStartElement();
                                if (XML.equal(startElement, "column")) {
                                    ColumnPatient column = new ColumnPatient(ID);
                                    int[] rooms = new int[patient.getRestLOS()];
                                    attributes = startElement.getAttributes();

                                    while (attributes.hasNext()) {
                                        attribute = attributes.next();
                                        if (XML.equal(attribute, "cost")) {
                                            column.setCost(Double.parseDouble(attribute.getValue
                                                    ()));
                                        } else if (XML.equal(attribute, "delay")) {
                                            column.setDelay(Integer.parseInt(attribute.getValue()));
                                        } else {
                                            String name = attribute.getName().getLocalPart();
                                            rooms[Integer.parseInt(name.substring(4))] =
                                                    Integer.parseInt(attribute.getValue());
                                        }
                                    }
                                    column.setRooms(rooms);
                                    if (add) {
                                        master.addColumn(column);
                                    }
                                }
                            } else if (event.isEndElement()) {
                                endElement = event.asEndElement();
                                if (XML.equal(endElement, "pattern" + ID)) {
                                    ID++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
