/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.text.MessageFormat;
import java.util.Map;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;

/**
 * Represents a wireless M-Bus link layer message without the CRC checksum.
 * 
 * {@link WMBusMessage} is structured as follows:
 * <ul>
 * <li>Length (1 byte) - the length (number of bytes) of the complete message without the length byte and the CRC bytes.
 * </li>
 * <li>Control field (1 byte) - defines the frame type. 0x44 signifies an SND-NR (send no request) message that is sent
 * by meters in S1 mode.</li>
 * <li>Secondary address (8 bytes) - the secondary address consists of:
 * <ul>
 * <li>Manufacturer ID (2 bytes) -</li>
 * <li>Address (6 bytes) - consists of
 * <ul>
 * <li>Device ID (4 bytes) -</li>
 * <li>Version (1 byte) -</li>
 * <li>Device type (1 byte) -</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 */
public class WMBusMessage {

    private final Integer signalStrengthInDBm;

    private final byte[] buffer;
    private final int controlField;
    private final SecondaryAddress secondaryAddress;
    private final VariableDataStructure vdr;

    private WMBusMessage(Integer signalStrengthInDBm, byte[] buffer, int controlField,
            SecondaryAddress secondaryAddress, VariableDataStructure vdr) {
        this.signalStrengthInDBm = signalStrengthInDBm;
        this.buffer = buffer;
        this.controlField = controlField;
        this.secondaryAddress = secondaryAddress;
        this.vdr = vdr;
    }

    /*
     * Only decodes the wireless M-Bus message itself.
     */
    static WMBusMessage decode(byte[] buffer, Integer signalStrengthInDBm, Map<SecondaryAddress, byte[]> keyMap)
            throws DecodingException {
        int length = buffer[0] & 0xff;

        if (length > (buffer.length - 1)) {
            String msg = MessageFormat.format(
                    "Byte buffer has only a length of {0} while the specified length field is {1}.", buffer.length,
                    length);
            throw new DecodingException(msg);
        }

        int controlField = buffer[1] & 0xff;
        SecondaryAddress secondaryAddress = SecondaryAddress.newFromWMBusLlHeader(buffer, 2);
        VariableDataStructure vdr = new VariableDataStructure(buffer, 10, length - 9, secondaryAddress, keyMap);

        return new WMBusMessage(signalStrengthInDBm, buffer, controlField, secondaryAddress, vdr);
    }

    /**
     * Get the message as binary large object (byte array).
     * 
     * @return the byte array representation of the message.
     */
    public byte[] asBlob() {
        return buffer;
    }

    public int getControlField() {
        return controlField;
    }

    /**
     * Get the secondary address.
     * 
     * @return the secondary address.
     */
    public SecondaryAddress getSecondaryAddress() {
        return secondaryAddress;
    }

    /**
     * Get the variable data structure of the message.
     * 
     * @return the variable data structure.
     */
    public VariableDataStructure getVariableDataResponse() {
        return vdr;
    }

    /**
     * Returns the received signal string indication (RSSI) in dBm.
     * 
     * @return the RSSI.
     */
    public Integer getRssi() {
        return signalStrengthInDBm;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (signalStrengthInDBm != null) {
            builder.append("Message was received with signal strength: ").append(signalStrengthInDBm).append("dBm\n");
        }

        return builder.append("control field: ").append(String.format("0x%02X", controlField))
                .append("\nSecondary Address -> ").append(secondaryAddress).append("\nVariable Data Response:\n")
                .append(vdr).toString();
    }
}
