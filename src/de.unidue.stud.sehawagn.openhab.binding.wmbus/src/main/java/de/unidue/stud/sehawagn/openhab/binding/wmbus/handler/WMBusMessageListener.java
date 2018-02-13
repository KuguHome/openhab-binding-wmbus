package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import org.openmuc.jmbus.wireless.WMBusMessage;

public interface WMBusMessageListener {

    /**
     *
     * @param wmBusDevice The message which was received.
     */
    public void onNewWMBusDevice(WMBusMessage wmBusDevice);

    /**
     *
     * @param wmBusDevice The message which was received.
     */
    public void onChangedWMBusDevice(WMBusMessage wmBusDevice);

}
