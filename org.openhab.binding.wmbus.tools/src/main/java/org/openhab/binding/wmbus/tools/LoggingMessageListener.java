package org.openhab.binding.wmbus.tools;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class LoggingMessageListener implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(LoggingMessageListener.class);

    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        log(device);
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        log(device);
    }

    private void log(WMBusDevice device) {
        logger.debug(HexUtils.bytesToHex(device.getOriginalMessage().asBlob()));
    }

}
