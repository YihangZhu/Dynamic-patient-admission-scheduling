/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.instance;

import util.XML;

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
 * Created by zhuyi on 8/30/2016.
 */
@SuppressWarnings("unchecked")
public class InstanceXMLReader {
    private final String DESCRIPTOR = "descriptor";
    private final String TREATMENTS = "treatments";
    private final String OR_SLOTS = "or_slots";
    private final String NAME = "name";
    private final Instance instance = new Instance();

    private XMLEvent event;
    private StartElement startElement;
    private Iterator<Attribute> attributes;
    private Attribute attribute;
    private EndElement endElement;

    public Instance readData(String str) {

        final String ORPASU_INSTANCE = "OrPasu_instance";
        final String PATIENTS = "patients";
        final String ROOMS = "rooms";
        final String DEPARTMENTS = "departments";
        final String FEATURES = "features";
        final String SPECIALISMS = "specialisms";
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory
                    .createXMLEventReader(new FileInputStream(new File(str)));
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, ORPASU_INSTANCE)) {
                        attributes = startElement.getAttributes();
                        attribute = attributes.next();
                        if (XML.equal(attribute, NAME)) {
                            instance.setName(attribute.getValue());
                        }
                    } else if (XML.equal(startElement, DESCRIPTOR)) {
                        readDescriptor(eventReader);
                    } else if (XML.equal(startElement, SPECIALISMS)) {
                        readSpecialisms(eventReader);
                    } else if (XML.equal(startElement, FEATURES)) {
                        readFeatures(eventReader);
                    } else if (XML.equal(startElement, DEPARTMENTS)) {
                        readDepartments(eventReader);
                    } else if (XML.equal(startElement, ROOMS)) {
                        readRooms(eventReader);
                    } else if (XML.equal(startElement, TREATMENTS)) {
                        readTreatments(eventReader);
                    } else if (XML.equal(startElement, OR_SLOTS)) {
                        readOperatingRooms(eventReader);
                    } else if (XML.equal(startElement, PATIENTS)) {
                        readPatients(eventReader);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance.preparation();
        return instance;
    }

    private void readDescriptor(XMLEventReader eventReader) {
        final String DEPARTMENTS = "Departments";
        final String ROOMS = "Rooms";
        final String FEATURES = "Features";
        final String PATIENTS = "Patients";
        final String SPECIALISMS = "Specialisms";
        final String HORIZON = "Horizon";
        final String NUM_DAYS = "num_days";
        final String START_DAY = "start_day";
        final String TREATMENTS = "Treatments";
        try {
            while (eventReader.hasNext()) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, DEPARTMENTS)) {
                        instance.setDepNum(XML.getInt(eventReader.nextEvent()));
                    } else if (XML.equal(startElement, ROOMS)) {
                        instance.setRoomNum(XML.getInt(eventReader.nextEvent()));
                    } else if (XML.equal(startElement, FEATURES)) {
                        instance.setFeatureNum(XML.getInt(eventReader.nextEvent()));
                    } else if (XML.equal(startElement, PATIENTS)) {
                        instance.setPatientNum(XML.getInt(eventReader.nextEvent()));
                    } else if (XML.equal(startElement, SPECIALISMS)) {
                        instance.setSpecialismNum(XML.getInt(eventReader.nextEvent()));
                    } else if (XML.equal(startElement, HORIZON)) {
                        attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            attribute = attributes.next();
                            if (XML.equal(attribute, NUM_DAYS)) {
                                instance.setPlanningHorizon(XML.getInt(attribute));
                            } else if (XML.equal(attribute, START_DAY)) {
                                LocalDate date = LocalDate.parse(attribute.getValue());
                                instance.setDateStart(date);
                            }
                        }
                    } else if (XML.equal(startElement, TREATMENTS)) {
                        instance.setTreatmentNum(XML.getInt(eventReader.nextEvent()));
                    }
                } else if (event.isEndElement()) {
                    endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(DESCRIPTOR)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readSpecialisms(XMLEventReader eventReader) {
        final String SPECIALISM = "specialism";
        try {
            for (int i = 0; i < instance.getSpecNum(); ) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, SPECIALISM)) {
                        instance.addSpecialisms(XML.getStr(eventReader.nextEvent()));
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFeatures(XMLEventReader eventReader) {
        try {
            for (int i = 0; i < instance.getFeatureNum(); ) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, "feature")) {
                        instance.addFeatures(XML.getStr(eventReader.nextEvent()));
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressWarnings("unchecked")
    private void readDepartments(XMLEventReader eventReader) {
        final String DEPARTMENT = "department";
        final String MAIN_SPECIALISM = "main_specialism";
        final String AUX_SPECIALISM = "aux_specialism";

        try {
            Department department = null;
            for (int i = 0; i < instance.getDepartmentNum(); ) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, DEPARTMENT)) {
                        attributes = startElement.getAttributes();
                        attribute = attributes.next();
                        if (XML.equal(attribute, NAME)) {
                            department = new Department(instance.getSpecNum());
                            department.setName(attribute.getValue());
                            instance.addDepartments(department);
                        }
                    } else if (XML.equal(startElement, MAIN_SPECIALISM)) {
                        event = eventReader.nextEvent();
                        assert department != null;
                        department.addMainSpecialism(instance.getSpecialisms()
                                .indexOf(XML.getStr(event)));
                    } else if (XML.equal(startElement, AUX_SPECIALISM)) {
                        event = eventReader.nextEvent();
                        assert department != null;
                        department.addMinorSpecialism(instance.getSpecialisms()
                                .indexOf(XML.getStr(event)));
                    }
                } else if (event.isEndElement()) {
                    endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(DEPARTMENT)) {
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readRooms(XMLEventReader eventReader) {
        final String FEATURE = "feature";
        final String DEPARTMENT = "department";
        final String ROOM = "room";
        final String GENDER_POLICY = "gender_policy";
        final String CAPACITY = "capacity";
        try {
            Room room = null;
            for (int i = 0; i < instance.getRoomNum(); ) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, ROOM)) {
                        room = new Room(instance.getFeatureNum());
                        attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            attribute = attributes.next();
                            if (XML.equal(attribute, NAME)) {
                                room.setName(attribute.getValue());
                            } else if (XML.equal(attribute, GENDER_POLICY)) {
                                switch (attribute.getValue()) {
                                    case "MaleOnly":
                                        room.setGenderPolicy(GenderPolicy.MaleOnly);
                                        break;
                                    case "FemaleOnly":
                                        room.setGenderPolicy(GenderPolicy.FemaleOnly);
                                        break;
                                    case "SameGender":
                                        room.setGenderPolicy(GenderPolicy.SameGender);
                                        break;
                                    case "Any":
                                        room.setGenderPolicy(GenderPolicy.Any);
                                }
                            } else if (XML.equal(attribute, DEPARTMENT)) {
                                room.setDepartment(instance.getDepartments()[instance
                                        .getDepartmentNames().
                                                indexOf(attribute.getValue())]);
                            } else if (XML.equal(attribute, CAPACITY)) {
                                room.setCapacity(XML.getInt(attribute));
                            }
                        }
                    } else if (XML.equal(startElement, FEATURE)) {
                        event = eventReader.nextEvent();
                        assert room != null;
                        room.addFeature(instance.getFeature(XML.getStr(event)));
                    }
                } else if (event.isEndElement()) {
                    endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(ROOM)) {
                        instance.addRooms(room);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readTreatments(XMLEventReader eventReader) {
        final String SPECIALISM = "specialism";
        final String TREATMENT = "treatment";
        final String SURGERY = "surgery";
        final String LENGTH = "length";

        try {
            Treatment treatment;
            while (eventReader.hasNext()) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, TREATMENT)) {
                        treatment = new Treatment();

                        attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = attributes.next();
                            if (XML.equal(attribute, NAME)) {
                                treatment.setName(attribute.getValue());
                                instance.addTreatments(treatment);
                            } else if (XML.equal(attribute, SPECIALISM)) {
                                treatment.setSpecialism(instance.getSpecialisms().indexOf
                                        (attribute.getValue()));
                            } else if (XML.equal(attribute, LENGTH)) {
                                treatment.setDurationSurgery(XML.getInt(attribute));
                            } else if (XML.equal(attribute, SURGERY)) {
                                treatment.setSurgeryRequirement(XML.getInt(attribute));
                            }
                        }
                    }
                } else if (event.isEndElement()) {
                    endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(TREATMENTS)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readOperatingRooms(XMLEventReader eventReader) {
        final String SLOT_LENGTH = "slot_length";
        final String DAY = "day";
        final String SPECIALISM_ASSIGNMENT = "specialism_assignment";
        final String NUMBER_OR_SLOTS = "number_or_slots";
        try {
            int date = -1;
            attributes = startElement.getAttributes();
            attribute = attributes.next();
            if (XML.equal(attribute, SLOT_LENGTH)) {
                instance.setSlotLengthOR(XML.getInt(attribute));
            }
            while (eventReader.hasNext()) {
                event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, DAY)) {

                        attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            attribute = attributes.next();
                            if (XML.equal(attribute, NAME)) {
                                date = getRelativeDate(attribute);
                            }
                        }
                    }
                    while (eventReader.hasNext()) {
                        event = eventReader.nextEvent();
                        if (event.isStartElement()) {
                            startElement = event.asStartElement();
                            if (XML.equal(startElement, SPECIALISM_ASSIGNMENT)) {
                                attributes = startElement.getAttributes();
                                int specialismIndex = -1;
                                int slotNum = -1;
                                while (attributes.hasNext()) {
                                    attribute = attributes.next();
                                    if (XML.equal(attribute, NAME)) {
                                        specialismIndex = instance.getSpecialisms().indexOf
                                                (attribute.getValue());
                                    } else if (XML.equal(attribute, NUMBER_OR_SLOTS)) {
                                        slotNum = XML.getInt(attribute);
                                    }
                                }
                                assert date != -1 && specialismIndex != -1 && slotNum != -1;
                                instance.addOperatingRoom(date, specialismIndex, slotNum);
                            }
                        } else if (event.isEndElement()) {
                            endElement = event.asEndElement();
                            if (endElement.getName().getLocalPart().equals(DAY)) {
                                break;
                            }
                        }
                    }
                }
                if (event.isEndElement()) {
                    EndElement endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(OR_SLOTS)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance.extendOperatingRooms();
    }

    private void readPatients(XMLEventReader eventReader) {
        final String ROOM = "room";
        final String TREATMENT = "treatment";
        final String PATIENT = "patient";
        final String MAX_ADMISSION = "max_admission";
        final String VARIABILITY = "variability";
        final String SURGERY_DAY = "surgery_day";
        final String DISCHARGE = "discharge";
        final String ADMISSION = "admission";
        final String REGISTRATION = "registration";
        final String GENDER = "gender";
        final String AGE = "age";
        final String PREFERRED_CAPACITY = "preferred_capacity";
        final String ROOM_PROPERTY = "room_property";
        final String TYPE = "type";
        try {
            Patient patient = null;

            for (int i = 0; i < instance.getPatientNum(); ) {
                event = eventReader.nextEvent();

                if (event.isStartElement()) {
                    startElement = event.asStartElement();
                    if (XML.equal(startElement, PATIENT)) {
                        patient = new Patient();
                        attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            attribute = attributes.next();
                            if (XML.equal(attribute, NAME)) {
                                patient.setName(attribute.getValue());
                            } else if (XML.equal(attribute, PREFERRED_CAPACITY)) {
                                patient.setPreferredCapacity(XML.getInt(attribute));
                            } else if (XML.equal(attribute, VARIABILITY)) {
                                patient.setVariability(XML.getInt(attribute));
                            } else if (XML.equal(attribute, DISCHARGE)) {
                                int d = getRelativeDate(attribute);
                                assert d >= 0;
                                patient.setDateDischarge(d);
                            } else if (XML.equal(attribute, ADMISSION)) {
                                int d = getRelativeDate(attribute);
                                assert d >= 0;
                                patient.setDateAdmission(d);
                            } else if (XML.equal(attribute, REGISTRATION)) {
                                int d = getRelativeDate(attribute);
                                assert d >= 0;
                                patient.setDateRegister(d);
                            } else if (XML.equal(attribute, TREATMENT)) {
                                patient.setTreatment(instance.getTreatments().get(attribute
                                        .getValue()));
                            } else if (XML.equal(attribute, GENDER)) {
                                switch (attribute.getValue()) {
                                    case "Male":
                                        patient.setGender(Gender.Male);
                                        break;
                                    case "Female":
                                        patient.setGender(Gender.Female);
                                }
                            } else if (XML.equal(attribute, SURGERY_DAY)) {
                                int d = getRelativeDate(attribute);
                                assert d >= 0;
                                patient.setDateSurgery(d);
                            } else if (XML.equal(attribute, AGE)) {
                                patient.setAge(XML.getInt(attribute));
                            } else if (XML.equal(attribute, ROOM)) {
                                Room room = instance.getRooms(instance.getRoomNames().indexOf
                                        (attribute.getValue()));
                                patient.setRoom(room);
                            } else if (XML.equal(attribute, MAX_ADMISSION)) {
                                int d = getRelativeDate(attribute);
                                assert d >= 0;
                                patient.setMaxAdmission(d);
                            }
                        }
                    }

                    if (XML.equal(startElement, ROOM_PROPERTY)) {
                        attributes = startElement.getAttributes();
                        int feature = -1;
                        String type = null;
                        while (attributes.hasNext()) {
                            attribute = attributes.next();
                            if (XML.equal(attribute, NAME)) {
                                feature = instance.getFeature(attribute.getValue());
                            } else if (XML.equal(attribute, TYPE)) {
                                type = attribute.getValue();
                            }
                        }
                        assert patient != null;
                        patient.setFeatures(type, feature);
                    }
                } else if (event.isEndElement()) {
                    endElement = event.asEndElement();
                    if (endElement.getName().getLocalPart().equals(PATIENT)) {
                        if (patient == null
                                || patient.getDateAdmission() < patient.getDateRegister()
                                || patient.getDateDischarge() <= patient.getDateAdmission()) {
                            throw new IllegalMonitorStateException("admission day for patient is " +
                                    "not valid!");
                        }
                        if (patient.getDateSurgery() != -1) {
                            if (patient.getDateSurgery() < patient.getDateAdmission() || patient
                                    .getDateSurgery() > patient.getDateDischarge()) {
                                throw new IllegalMonitorStateException("surgery day for patient " +
                                        "is not valid!");
                            }
                        }
                        instance.addPatients(patient);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getRelativeDate(Attribute attribute) {
        return (int) ChronoUnit.DAYS.between(instance.getDateStart(), LocalDate.parse(attribute
                .getValue()));
    }
}
