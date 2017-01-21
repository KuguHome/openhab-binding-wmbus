package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openmuc.jmbus.TechemHKVMessage;
import org.openmuc.jmbus.WMBusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class WMBusTechemHKVHandler extends BaseThingHandler implements WMBusMessageListener {

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TECHEM_HKV);
    private final Logger logger = LoggerFactory.getLogger(WMBusTechemHKVHandler.class);
    private String deviceId;
    private WMBusBridgeHandler bridgeHandler;

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private TechemHKVMessage techemDeviceMessage;

    public WMBusTechemHKVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WMBusTechemHKVHandler handler.");
        Configuration config = getConfig();
        deviceId = (String) config.getProperties().get(PROPERTY_HKV_ID);
        WMBusMessage deviceMessage = getDevice();
        if (deviceMessage instanceof TechemHKVMessage) {
            techemDeviceMessage = (TechemHKVMessage) deviceMessage;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing TechemHKV handler.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            State newState = null;
            if (techemDeviceMessage != null) {
                switch (channelUID.getId()) {
                    case CHANNEL_ROOMTEMPERATURE: {
                        newState = new DecimalType(techemDeviceMessage.getT1());
                        break;
                    }
                    case CHANNEL_RADIATORTEMPERATURE: {
                        newState = new DecimalType(techemDeviceMessage.getT2());
                        break;
                    }
                    case CHANNEL_CURRENTREADING: {
                        newState = new DecimalType(techemDeviceMessage.getCurVal());
                        break;
                    }
                    case CHANNEL_LASTREADING: {
                        newState = new DecimalType(techemDeviceMessage.getLastVal());
                        break;
                    }
                    case CHANNEL_RECEPTION: {
                        newState = new DecimalType(techemDeviceMessage.getRssi());
                        break;
                    }
                    case CHANNEL_LASTDATE: {
                        newState = new DateTimeType(techemDeviceMessage.getLastDate());
                        // newState = new StringType(dateFormat.format(techemDeviceMessage.getLastDate().getTime()));
                        break;
                    }
                    case CHANNEL_CURRENTDATE: {
                        newState = new DateTimeType(techemDeviceMessage.getCurDate());
                        // newState = new StringType(dateFormat.format(techemDeviceMessage.getCurDate().getTime()));
                        break;
                    }
                    case CHANNEL_ALMANAC: {
                        newState = new StringType(techemDeviceMessage.getHistory());
                        break;
                    }
                }
                updateState(channelUID.getId(), newState);
            }
        }
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
                this.bridgeHandler.registerWMBusMessageListener(this);
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

    @Override
    public void onNewWMBusDevice(WMBusMessage wmBusDevice) {
        if (wmBusDevice.getSecondaryAddress().getDeviceId().toString().equals(deviceId)) {
            updateStatus(ThingStatus.ONLINE);
            onChangedWMBusDevice(wmBusDevice);
        }

    }

    @Override
    public void onChangedWMBusDevice(WMBusMessage wmBusDevice) {
        if (wmBusDevice.getSecondaryAddress().getDeviceId().toString().equals(deviceId)) {
            techemDeviceMessage = (TechemHKVMessage) wmBusDevice;
            for (Channel curChan : getThing().getChannels()) {
                handleCommand(curChan.getUID(), RefreshType.REFRESH);
            }
        }

    }
}
