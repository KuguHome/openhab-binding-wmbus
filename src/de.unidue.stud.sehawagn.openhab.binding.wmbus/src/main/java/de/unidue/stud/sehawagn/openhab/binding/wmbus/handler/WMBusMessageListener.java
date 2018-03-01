package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

public interface WMBusMessageListener {

    /**
     *
     * @param device The message which was received.
     */
    public void onNewWMBusDevice(WMBusDevice device);

    /**
     *
     * @param device The message which was received.
     */
    public void onChangedWMBusDevice(WMBusDevice device);

}
