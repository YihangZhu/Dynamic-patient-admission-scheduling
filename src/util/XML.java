/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package util;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Created by yihang on 5/17/17.
 * the common methods used for XML file.
 */
public class XML {
    public static boolean equal(XMLEvent event, String name, String startOrEnd){
        switch (startOrEnd) {
            case "start":
                if (event.isStartElement()) {
                    return event.asStartElement().getName().getLocalPart().equals(name);
                }
                break;
            case "end":
                if (event.isEndElement()) {
                    return event.asEndElement().getName().getLocalPart().equals(name);
                }
                break;
        }
        return false;
    }

    public static boolean equal(StartElement startElement, String name){
        return startElement.getName().getLocalPart().equals(name);
    }

    public static boolean equal(EndElement startElement, String name){
        return startElement.getName().getLocalPart().equals(name);
    }

    public static boolean equal(Attribute attribute, String name){
        return attribute.getName().getLocalPart().equals(name);
    }

    public static int getInt(XMLEvent event){
        return Integer.parseInt(event.asCharacters().getData());
    }

    public static int getInt(Attribute attribute){
        return Integer.parseInt(attribute.getValue());
    }

    public static double getDouble(XMLEvent event){
        return Double.parseDouble(event.asCharacters().getData());
    }

    public static String getStr(XMLEvent event){
        return event.asCharacters().getData();
    }

//    public static String getStr(XMLAttribute attribute) {
//        return attribute.getValue();
//    }

    public static LocalTime getLocalTime(XMLEvent event){
        return LocalTime.parse(event.asCharacters().getData().substring(0,8));
    }

    public static LocalDate getLocalDate(XMLEvent event){
        return LocalDate.parse(event.asCharacters().getData());
    }
}
