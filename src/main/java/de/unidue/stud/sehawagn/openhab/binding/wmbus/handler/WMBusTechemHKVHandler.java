package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import java.util.Collection;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants;

public class WMBusTechemHKVHandler extends ConfigStatusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WMBusTechemHKVHandler.class);

    public WMBusTechemHKVHandler(Thing thing) {
        super(thing);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBus handler.");

        Configuration config = getThing().getConfiguration();
        config.get(WMBusBindingConstants.CONFKEY_INTERFACE_NAME);
        getThing().getProperties();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing WMBus handler.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        WMBusBridgeHandler wmbusBridgeHandler = (WMBusBridgeHandler) getBridge().getHandler();

        if (command instanceof RefreshType) {

            switch (channelUID.getId()) {
                case WMBusBindingConstants.CHANNEL_ROOMTEMPERATURE:
                    updateState(channelUID, getRoomTemperature());
                    break;
                case WMBusBindingConstants.CHANNEL_RADIATORTEMPERATURE:
                    updateState(channelUID, getRadiatorTemperature());
                    break;
                case WMBusBindingConstants.CHANNEL_COSTCOUNTER:
                    updateState(channelUID, getCoscounter());
                    break;
                default:
                    logger.debug("WMBus: Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        } else {
            logger.debug("WMBus: Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    private State getRadiatorTemperature() {
        // TODO Auto-generated method stub
        return null;
    }

    private State getCoscounter() {
        // TODO Auto-generated method stub
        return null;
    }

    private State getRoomTemperature() {
        // TODO Auto-generated method stub
        return null;
    }

}
