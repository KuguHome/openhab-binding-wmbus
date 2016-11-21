package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openmuc.jmbus.TechemHKVMessage;
import org.openmuc.jmbus.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class WMBusTechemHKVHandler extends ConfigStatusThingHandler {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_WMBUS_TECHEM_HKV);
    private final Logger logger = LoggerFactory.getLogger(WMBusTechemHKVHandler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public WMBusTechemHKVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        return null;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing TechemHKV handler.");

        Configuration config = getThing().getConfiguration();
        deviceId = (String) config.get(PROPERTY_HKV_ID);
        WMBusMessage deviceMessage = getDevice();
        System.out.print(deviceId);
        System.out.print(deviceMessage);
        if (deviceMessage instanceof TechemHKVMessage) {
            TechemHKVMessage techemDeviceMessage = (TechemHKVMessage) deviceMessage;

            updateState(CHANNEL_ROOMTEMPERATURE, new DecimalType(techemDeviceMessage.getT1()));
            updateState(CHANNEL_RADIATORTEMPERATURE, new DecimalType(techemDeviceMessage.getT2()));
            updateState(CHANNEL_CURRENTREADING, new DecimalType(techemDeviceMessage.getCurVal()));
            updateState(CHANNEL_LASTREADING, new DecimalType(techemDeviceMessage.getLastVal()));
            updateState(CHANNEL_RECEPTION, new DecimalType(techemDeviceMessage.getRssi()));
            updateState(CHANNEL_LASTDATE, new StringType(dateFormat.format(techemDeviceMessage.getLastDate())));
            updateState(CHANNEL_CURRENTDATE, new StringType(dateFormat.format(techemDeviceMessage.getCurDate())));
            updateState(CHANNEL_ALMANAC, new StringType(techemDeviceMessage.getHistory()));
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TechemHKV handler.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands possible
    }

    private synchronized WMBusBridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof WMBusBridgeHandler) {
                this.bridgeHandler = (WMBusBridgeHandler) handler;
                // this.bridgeHandler.registerWMBusMessageListener(this); // what for?
            } else {
                return null;
            }
        }
        return this.bridgeHandler;
    }

    private WMBusMessage getDevice() {
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            return bridgeHandler.getDeviceById(deviceId);
        }
        return null;
    }
}
