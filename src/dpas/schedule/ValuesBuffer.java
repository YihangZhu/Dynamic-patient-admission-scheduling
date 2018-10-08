/*
 * Copyright (c) 1/22/18 4:19 PM
 * Author: Yi-Hang Zhu
 */

package dpas.schedule;


import dpas.instance.Instance;
import dpas.schedule.buffer.BufferPRoom;

import java.util.ArrayList;

/**
 * Created by zhuyi on 10/19/2016.
 * this class is used to store the new date get by the new move.
 */

public class ValuesBuffer implements Storage<SRoomValues> {
    private ValuesStorage valuesStorage;

    private ArrayList<SOR> involvedOR;
    private SOR[] bufferOR;

    private ArrayList<SRoomValues> involvedRooms;
    private ArrayList<SRoomValues> involvedPotentialRooms;
    private SRoomValues[][] bufferRooms;

    public ValuesBuffer(Instance instance, ValuesStorage valuesStorage) {
        this.valuesStorage = valuesStorage;
        involvedOR = new ArrayList<>();
        bufferOR = new SOR[instance.getNumDays()];
        involvedRooms = new ArrayList<>();
        involvedPotentialRooms = new ArrayList<>();
        bufferRooms = new SRoomValues[instance.getRoomNum()][instance.getNumDays()];//dummy 5
        // days. but do not check
    }

    public void initializeBuffers() {
        for (SOR buf : involvedOR) {
            bufferOR[buf.getDay()] = null;
        }
        for (SRoomValues buf : involvedRooms) {
            this.bufferRooms[buf.getRoomNum()][buf.getDay()] = null;
        }
        for (SRoomValues buf : involvedPotentialRooms) {
            this.bufferRooms[buf.getRoomNum()][buf.getDay()] = null;
        }
        involvedOR = new ArrayList<>();
        involvedRooms = new ArrayList<>();
        involvedPotentialRooms = new ArrayList<>();
    }


    public void updateRoomStorageValues() {
        for (SRoomValues buf : involvedRooms) {
            valuesStorage.getRoomStorage(buf.getRoomNum(), buf.getDay()).updateOccupancy(buf);
        }

        for (SRoomValues buf : involvedPotentialRooms) {
            valuesStorage.getRoomStorage(buf.getRoomNum(), buf.getDay()).updatePOccupancy(buf
                    .getPOccupancy());
        }
    }

    public void updateORStorageValues() {
        for (SOR buf : involvedOR) {
            valuesStorage.getORStorage(buf.getDay()).updateOR(buf);
        }
    }

    public ArrayList<SOR> getInvolvedOR() {
        return involvedOR;
    }

    public void addInvolvedOR(SOR OR) {
        involvedOR.add(OR);
        bufferOR[OR.getDay()] = OR;
    }

    public void addInvolvedRoom(SRoom sRoom) {
        bufferRooms[sRoom.getRoomNum()][sRoom.getDay()] = sRoom;
        involvedRooms.add(sRoom);
    }

    public void addInvolvedPRoom(BufferPRoom PRoom) {
        bufferRooms[PRoom.getRoomNum()][PRoom.getDay()] = PRoom;
        involvedPotentialRooms.add(PRoom);
    }

    public SOR getORStorage(int day) {
        return bufferOR[day];
    }

    public ArrayList<SRoomValues> getInvolvedRooms() {
        return involvedRooms;
    }

    public ArrayList<SRoomValues> getInvolvedPotentialRooms() {
        return involvedPotentialRooms;
    }

    public SRoomValues getRoomStorage(int roomIndex, int day) {
        return bufferRooms[roomIndex][day];
    }

}
