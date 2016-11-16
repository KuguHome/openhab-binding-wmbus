/*
 * Copyright 2010-16 Fraunhofer ISE
 *
 * This file is part of jMBus.
 * For more information visit http://www.openmuc.org
 *
 * jMBus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jMBus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jMBus.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.unidue.stud.sehawagn.openhab.binding.wmbus.internal;

import java.io.IOException;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.TechemHKVMessage;
import org.openmuc.jmbus.WMBusListener;
import org.openmuc.jmbus.WMBusMessage;
import org.openmuc.jmbus.WMBusMode;
import org.openmuc.jmbus.WMBusSap;
import org.openmuc.jmbus.WMBusSapAmber;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusBridgeHandler;

/**
 *
 * @author
 *
 */
public class TechemReceiver implements WMBusListener {
    int[] filterIDs = new int[] {};

    private WMBusBridgeHandler wmBusBridgeHandler;

    public static String VENDOR_TECHEM = "TCH";

    public TechemReceiver(WMBusBridgeHandler wmBusBridgeHandler) {
        this.wmBusBridgeHandler = wmBusBridgeHandler;
    }

    public int[] getFilterIDs() {
        return filterIDs;
    }

    public void setFilterIDs(int[] filterIDs) {
        this.filterIDs = filterIDs;
    }

    public void init(String serialPortName) {

        WMBusMode mode = null;

        mode = WMBusMode.T;

        final WMBusSap wMBusSap = new WMBusSapAmber(serialPortName, mode, this);

        try {
            wMBusSap.open();
        } catch (IOException e2) {
            System.err.println("Failed to open serial port: " + e2.getMessage());
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (wMBusSap != null) {
                    wMBusSap.close();
                }
            }
        });

    }

    boolean filterMatch(int inQuestion) {
        if (filterIDs.length == 0) {
            return true;
        }
        for (int i = 0; i < filterIDs.length; i++) {
            if (filterIDs[i] == inQuestion) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void newMessage(WMBusMessage message) {
        try {
            message.decodeDeep();
            if (filterMatch(message.getSecondaryAddress().getDeviceId().intValue())) {
                System.out.println(message.toString());
                wmBusBridgeHandler.processMessage(message);
            }
        } catch (DecodingException e) {
            byte[] messageBytes = message.asBytes();
            if (messageBytes.length == 51 && (messageBytes[10] & 0xff) == 0xa0
                    && message.getSecondaryAddress().getManufacturerId().equals(VENDOR_TECHEM)) {
                newMessage(new TechemHKVMessage(message));
            } else {
                System.out.println("Unable to fully decode received message: " + e.getMessage());
                System.out.println(message.toString());
            }
        }
    }

    @Override
    public void discardedBytes(byte[] bytes) {
        // System.out.println("Bytes discarded: " + HexConverter.toShortHexString(bytes));
    }

    @Override
    public void stoppedListening(IOException e) {
        // System.out.println("Stopped listening for new messages because: " + e.getMessage());
    }
}