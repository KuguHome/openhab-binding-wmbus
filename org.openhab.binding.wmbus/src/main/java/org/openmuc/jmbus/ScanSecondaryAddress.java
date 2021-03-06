/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jmbus.MBusMessage.MessageType;

class ScanSecondaryAddress {

    private static final int MAX_LENGTH = 16;
    private static int pos = 0;
    private static byte[] value = new byte[MAX_LENGTH];

    public static List<SecondaryAddress> scan(MBusConnection mBusConnection, String wildcardMask,
            SecondaryAddressListener secondaryAddressListener) throws IOException {

        List<SecondaryAddress> secondaryAddresses = new LinkedList<>();

        boolean stop = false;
        boolean collision = false;

        wildcardMask = flipString(wildcardMask);
        wildcardMask += "ffffffff";

        for (int i = 0; i < MAX_LENGTH; ++i) {
            value[i] = Byte.parseByte(wildcardMask.substring(i, i + 1), 16);
        }

        pos = wildcardMask.indexOf('f');
        if (pos == 8 || pos < 0) {
            pos = 7;
        }
        value[pos] = 0;

        while (!stop) {
            String msg = MessageFormat.format("scan with wildcard: {0}", printHexBinary(toSendByteArray(value)));
            notifyScanMsg(secondaryAddressListener, msg);

            SecondaryAddress secondaryAddessesWildCard = SecondaryAddress.newFromLongHeader(toSendByteArray(value), 0);
            SecondaryAddress readSecondaryAddress = null;

            if (scanSelection(mBusConnection, secondaryAddessesWildCard)) {

                try {
                    readSecondaryAddress = mBusConnection.read(0xfd).getSecondaryAddress();

                } catch (InterruptedIOException e) {
                    notifyScanMsg(secondaryAddressListener, "Read (REQ_UD2) TimeoutException");
                    collision = false;
                } catch (IOException e) {
                    notifyScanMsg(secondaryAddressListener, "Read (REQ_UD2) IOException / Collision");
                    collision = true;
                }

                if (collision) {
                    if (pos < 7) {
                        ++pos;
                        value[pos] = 0;
                    } else {
                        stop = handler();
                    }
                    collision = false;
                } else {
                    if (readSecondaryAddress != null) {
                        String message = "Detected Device:\n" + readSecondaryAddress.toString();
                        notifyScanMsg(secondaryAddressListener, message);
                        secondaryAddresses.add(readSecondaryAddress);
                        if (secondaryAddressListener != null) {
                            secondaryAddressListener.newDeviceFound(readSecondaryAddress);
                        }
                        stop = handler();
                    } else {

                        notifyScanMsg(secondaryAddressListener,
                                "Problem to decode secondary address. Perhaps a collision.");
                        if (pos < 7) {
                            ++pos;
                            value[pos] = 0;
                        } else {
                            stop = handler();
                        }
                        collision = false;
                    }
                }
            } else {
                stop = handler();
            }
        }
        if (mBusConnection != null) {
            mBusConnection.close();
        }
        return secondaryAddresses;
    }

    /**
     * Scans if any device response to the given wildcard.
     * 
     * @param mBusConnection
     *            object to the open mbus connection
     * 
     * @param wildcard
     *            secondary address wildcard e.g. f1ffffffffffffff
     * @return true if any device responsed else false
     * @throws IOException
     */
    private static boolean scanSelection(MBusConnection mBusConnection, SecondaryAddress wildcard) throws IOException {
        ByteBuffer bf = ByteBuffer.allocate(8);
        byte[] ba = new byte[8];

        bf.order(ByteOrder.LITTLE_ENDIAN);

        bf.put(wildcard.asByteArray());

        bf.position(0);
        bf.get(ba, 0, 8);

        mBusConnection.sendLongMessage(0xfd, 0x53, 0x52, 8, ba);

        try {
            MBusMessage mBusMessage = mBusConnection.receiveMessage();

            return mBusMessage.getMessageType() == MessageType.SINGLE_CHARACTER;
        } catch (InterruptedIOException e) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private static void notifyScanMsg(SecondaryAddressListener secondaryAddressListener, String message) {
        if (secondaryAddressListener != null) {
            secondaryAddressListener.newScanMessage(message);
        }
    }

    private static boolean handler() {
        boolean stop;

        ++value[pos];

        if (value[pos] < 10) {
            stop = false;
        } else {
            if (pos > 0) {
                --pos;
                ++value[pos];
                setFValue();

                while (value[pos] > 10) {
                    --pos;
                    ++value[pos];
                    setFValue();
                }
                stop = false;
            } else {
                stop = true;
            }
        }

        return stop;
    }

    private static void setFValue() {
        for (int i = pos + 1; i < 8; ++i) {
            value[i] = 0xf;
        }
    }

    private static byte[] toSendByteArray(byte[] value) {
        byte[] sendByteArray = new byte[8];

        for (int i = 0; i < MAX_LENGTH; ++i) {

            if (i % 2 > 0) {
                sendByteArray[i / 2] |= value[i] << 4;
            } else {
                sendByteArray[i / 2] |= value[i];
            }
        }
        return sendByteArray;
    }

    /**
     * Flips character pairs. <br>
     * from 01253fffffffffff to 1052f3ffffffffff
     * 
     * @param value
     *            a string value like 01253fffffffffff
     * @return a fliped string value.
     */
    private static String flipString(String value) {
        StringBuilder flipped = new StringBuilder();

        for (int i = 0; i < value.length(); i += 2) {
            flipped.append(value.charAt(i + 1));
            flipped.append(value.charAt(i));
        }
        return flipped.toString();
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private ScanSecondaryAddress() {
    }
}
