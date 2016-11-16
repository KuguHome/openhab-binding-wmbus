package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import org.openmuc.jmbus.WMBusMessage;

public interface WMBusMessageListener {

    /**
     *
     * @param wmBusDevice The message which was received.
     */
    public void onWMBusMessageReceived(WMBusMessage wmBusDevice);

}
