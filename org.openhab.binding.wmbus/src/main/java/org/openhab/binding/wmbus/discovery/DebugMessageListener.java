package org.openhab.binding.wmbus.discovery;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.EncryptionMode;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class DebugMessageListener implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(DebugMessageListener.class);

    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        log(device);
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice device) {
        log(device);
    }

    private void log(WMBusDevice device) {
        SecondaryAddress secondaryAddress = device.getOriginalMessage().getSecondaryAddress();

        if (!"TCH".equals(secondaryAddress.getManufacturerId())) {

            try {
                VariableDataStructure vdr = device.getOriginalMessage().getVariableDataResponse();

                if (vdr.getEncryptionMode() == EncryptionMode.NONE) {
                    vdr.decode();

                    logger.debug(
                            "Received telegram ({}): access number: {}, status: {}, encryption mode: {}, number of encrypted blocks: {}",
                            secondaryAddress, vdr.getAccessNumber(), vdr.getStatus(), vdr.getEncryptionMode(),
                            vdr.getNumberOfEncryptedBlocks());
                    logger.debug("Message in hex: {}", HexUtils.bytesToHex(device.getOriginalMessage().asBlob()));

                    for (DataRecord record : vdr.getDataRecords()) {
                        logger.debug("> record: {}", record.toString());
                    }
                } else {
                    logger.debug("Encrypted block {}", vdr);
                }
            } catch (DecodingException e) {
                logger.debug("Could not decode frame ({})", secondaryAddress, e);
            }
        }

    }

}
