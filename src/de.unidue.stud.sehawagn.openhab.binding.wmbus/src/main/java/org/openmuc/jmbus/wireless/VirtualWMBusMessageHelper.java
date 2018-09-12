package org.openmuc.jmbus.wireless;

import java.util.Map;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.SecondaryAddress;

public class VirtualWMBusMessageHelper {

    public static WMBusMessage decode(byte[] buffer, Integer signalStrengthInDBm, Map<SecondaryAddress, byte[]> keyMap)
            throws DecodingException {
        return WMBusMessage.decode(buffer, signalStrengthInDBm, keyMap);

    }
}
