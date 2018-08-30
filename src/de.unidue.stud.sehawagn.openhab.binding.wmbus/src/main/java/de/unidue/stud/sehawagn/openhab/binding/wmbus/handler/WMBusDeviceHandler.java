package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.PROPERTY_DEVICE_ID;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

public abstract class WMBusDeviceHandler extends BaseThingHandler implements WMBusMessageListener {
    private final Logger logger = LoggerFactory.getLogger(WMBusDeviceHandler.class);
    protected String deviceId;
    private WMBusBridgeHandler bridgeHandler;
    protected WMBusDevice wmbusDevice;

    public WMBusDeviceHandler(Thing thing) {
        super(thing);
        logger.debug("new() for Thing" + thing.toString());
    }

    // entry point - gets device here
    @Override
    public void onNewWMBusDevice(WMBusDevice wmBusDevice) {
        logger.trace("onNewWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceId().equals(deviceId)) {
            logger.trace("onNewWMBusDevice(): yes it's me");
            logger.trace("onNewWMBusDevice(): updating status to online");
            updateStatus(ThingStatus.ONLINE);
            logger.trace("onNewWMBusDevice(): calling onChangedWMBusDevice()");
            onChangedWMBusDevice(wmBusDevice);
        }
        logger.trace("onNewWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusDevice receivedDevice) {
        logger.trace("onChangedWMBusDevice(): is it me?");
        if (receivedDevice.getDeviceId().equals(deviceId)) {
            logger.trace("onChangedWMBusDevice(): yes");
            // in between the good messages, there are messages with invalid values -> filter these out
            if (!checkMessage(receivedDevice)) {
                logger.trace("onChangedWMBusDevice(): this is a malformed message, ignoring this message");
            } else {
                wmbusDevice = receivedDevice;
                logger.trace("onChangedWMBusDevice(): inform all channels to refresh");
                triggerRefresh();
            }
        } else {
            logger.trace("onChangedWMBusDevice(): no");
        }
        logger.trace("onChangedWMBusDevice(): return");
    }

    protected void triggerRefresh() {
        for (Channel curChan : getThing().getChannels()) {
            handleCommand(curChan.getUID(), RefreshType.REFRESH);
        }
    }

    protected DateTimeType convertDate(Object input) {
        if (input instanceof Date) {
            Date date = (Date) input;
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            zonedDateTime.truncatedTo(ChronoUnit.SECONDS); // throw away millisecond value to avoid, eg. _previous_date
                                                           // changed from 2018-02-28T00:00:00.353+0100 to
                                                           // 2018-02-28T00:00:00.159+0100
            return new DateTimeType(zonedDateTime);
        }
        return null;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler.");
        Configuration config = getConfig();
        deviceId = (String) config.getProperties().get(PROPERTY_DEVICE_ID);
        wmbusDevice = getDevice();
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler.");
    }

    protected synchronized WMBusBridgeHandler getBridgeHandler() {
        logger.trace("getBridgeHandler() begin");
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof WMBusBridgeHandler) {
                bridgeHandler = (WMBusBridgeHandler) handler;
                bridgeHandler.registerWMBusMessageListener(this);
            } else {
                return null;
            }
        }
        logger.trace("getBridgeHandler() returning bridgehandler");
        return bridgeHandler;
    }

    protected WMBusDevice getDevice() {
        logger.trace("getDevice() begin");
        WMBusBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("thinghandler: getDevice() end: returning null");
            return null;
        }
        logger.trace("getDevice() end: returning devicebyid");
        return bridgeHandler.getDeviceById(deviceId);
    }

    boolean checkMessage(WMBusDevice receivedDevice) {
        return true;
    }
}
