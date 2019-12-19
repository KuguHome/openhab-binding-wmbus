/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.handler;

import static org.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.config.DateFieldMode;
import org.openhab.binding.wmbus.internal.WMBusException;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openhab.io.transport.mbus.wireless.MapKeyStorage;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WMBusDeviceHandler} class defines abstract WMBusDeviceHandler
 *
 * @author Hanno - Felix Wagner - Initial contribution
 * @author Łukasz Dywicki - Added possibility to customize message parsing.
 */

public abstract class WMBusDeviceHandler<T extends WMBusDevice> extends BaseThingHandler
        implements WMBusMessageListener {

    private final Logger logger = LoggerFactory.getLogger(WMBusDeviceHandler.class);
    private final KeyStorage keyStorage;

    protected String deviceAddress;
    private WMBusBridgeHandlerBase bridgeHandler;
    protected T wmbusDevice;
    protected Long lastUpdate;
    private Long frequencyOfUpdates = WMBusBindingConstants.DEFAULT_DEVICE_FREQUENCY_OF_UPDATES;
    private ThingStatus status;

    protected WMBusDeviceHandler(Thing thing) {
        this(thing, new MapKeyStorage());
    }

    public WMBusDeviceHandler(Thing thing, KeyStorage keyStorage) {
        super(thing);
        this.keyStorage = keyStorage;
        logger.debug("Created new handler for thing {}", thing.getUID());
    }

    // entry point - gets device here
    @Override
    public void onNewWMBusDevice(WMBusAdapter adapter, WMBusDevice wmBusDevice) {
        logger.trace("onNewWMBusDevice(): is it me?");
        if (wmBusDevice.getDeviceAddress().equals(deviceAddress)) {
            logger.trace("onNewWMBusDevice(): yes it's me");
            logger.trace("onNewWMBusDevice(): calling onChangedWMBusDevice()");
            onChangedWMBusDevice(adapter, wmBusDevice);
        }
        logger.trace("onNewWMBusDevice(): no");
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter adapter, WMBusDevice receivedDevice) {
        logger.trace("onChangedWMBusDevice(): is it me?");
        if (receivedDevice.getDeviceAddress().equals(deviceAddress)) {
            logger.trace("onChangedWMBusDevice(): yes");
            // in between the good messages, there are messages with invalid values -> filter these out
            if (!checkMessage(receivedDevice)) {
                logger.trace("onChangedWMBusDevice(): this is a malformed message, ignoring this message");
            } else {
                try {
                    wmbusDevice = parseDevice(receivedDevice);
                    logger.trace("onChangedWMBusDevice(): updating status to online");
                    updateStatus(ThingStatus.ONLINE);

                    if (wmbusDevice != null) {
                        logger.trace("onChangedWMBusDevice(): inform all channels to refresh");
                        triggerRefresh();
                    }
                } catch (DecodingException e) {
                    // FIXME decoding exception should not be necessary over time
                    logger.debug("onChangedWMBusDevice(): could not decode frame {} because {}. Detail: {}",
                            HexUtils.bytesToHex(receivedDevice.getOriginalMessage().asBlob()), e.getMessage(),
                            receivedDevice.getOriginalMessage());
                }
            }
        } else {
            logger.trace("onChangedWMBusDevice(): no");
        }
        logger.trace("onChangedWMBusDevice(): return");
    }

    @Override
    protected void updateStatus(@NonNull ThingStatus status, @NonNull ThingStatusDetail statusDetail,
            @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
        this.status = status;
    }

    protected void triggerRefresh() {
        lastUpdate = System.currentTimeMillis();

        for (Channel curChan : getThing().getChannels()) {
            handleCommand(curChan.getUID(), RefreshType.REFRESH);
        }
    }

    protected State convertRecordData(DataRecord record) {

        switch (record.getDataValueType()) {
            case LONG:
            case DOUBLE:
            case BCD:
                return new DecimalType(record.getScaledDataValue());
            case DATE:
                return convertDate(record.getDataValue());
            case STRING:
            case NONE:
                return new StringType(record.getDataValue().toString());
        }
        return null;
    }

    protected DateFieldMode getDateFieldMode() {
        return getBridgeHandler().getDateFieldMode();
    }

    protected State convertDate(Object input) {
        DateTimeType value = null;
        if (input instanceof Date) {
            Date date = (Date) input;
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            zonedDateTime.truncatedTo(ChronoUnit.SECONDS); // throw away millisecond value to avoid, eg. _previous_date
                                                           // changed from 2018-02-28T00:00:00.353+0100 to
                                                           // 2018-02-28T00:00:00.159+0100
            value = new DateTimeType(zonedDateTime);
        }
        if (input instanceof DateTimeType) {
            value = (DateTimeType) input;
        }

        if (value == null) {
            return null;
        }

        switch (getDateFieldMode()) {
            case FORMATTED_STRING:
                return new StringType(value.format(null));
            case UNIX_TIMESTAMP:
                return new DecimalType(value.getZonedDateTime().toEpochSecond());
            default:
                return value;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler.");
        updateStatus(ThingStatus.UNKNOWN);

        Configuration config = getConfig();
        deviceAddress = (String) config.getProperties().get(PROPERTY_DEVICE_ADDRESS);

        if (deviceAddress == null || deviceAddress.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Missing device address, please update.");
            return;
        }

        try {
            wmbusDevice = getDevice();
            if (wmbusDevice != null) {
                initialize(wmbusDevice);
            }
        } catch (WMBusException e) {
            logger.error("Could not obtain Wireless M-Bus device information", e);
        }

        Long updateFrequency = Optional.of(config.getProperties())
                .map(cfg -> cfg.get(PROPERTY_DEVICE_FREQUENCY_OF_UPDATES)) //
                .filter(BigDecimal.class::isInstance) //
                .map(BigDecimal.class::cast) //
                .map(BigDecimal::longValue) //
                .orElse(DEFAULT_DEVICE_FREQUENCY_OF_UPDATES);
        this.frequencyOfUpdates = TimeUnit.MINUTES.toMillis(updateFrequency);

        if (Boolean.valueOf(thing.getProperties().get(PROPERTY_DEVICE_ENCRYPTED))) {
            Optional<byte[]> encryptionKey = Optional.of(config.getProperties()) //
                    .map(cfg -> cfg.get(PROPERTY_DEVICE_ENCRYPTION_KEY)) //
                    .filter(String.class::isInstance) //
                    .map(String.class::cast) //
                    .map(HexUtils::hexToBytes);
            if (!encryptionKey.isPresent()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Please provide encryption key to read device communication.");
            }
            encryptionKey.ifPresent(key -> keyStorage.registerKey(HexUtils.hexToBytes(deviceAddress), key));
        }
    }

    protected void initialize(T device) {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler.");
        this.deviceAddress = null;
        this.wmbusDevice = null;
    }

    protected synchronized WMBusBridgeHandlerBase getBridgeHandler() {
        logger.trace("getBridgeHandler() begin");
        if (bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof WMBusBridgeHandlerBase) {
                bridgeHandler = (WMBusBridgeHandlerBase) handler;
            } else {
                return null;
            }
        }
        logger.trace("getBridgeHandler() returning bridgehandler");
        return bridgeHandler;
    }

    protected T getDevice() throws WMBusException {
        logger.trace("getDevice() begin");
        WMBusBridgeHandlerBase bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Device handler is not linked with bridge, skipping call");
            return null;
        }

        logger.trace("Lookup known devices by address {}", deviceAddress);
        WMBusDevice device = bridgeHandler.getDeviceByAddress(deviceAddress);
        if (device != null) {
            logger.trace("Found device matching given addresss {}, {}", deviceAddress, device);
            try {
                return parseDevice(device);
            } catch (DecodingException e) {
                logger.trace("Unable to parse received message {}", device, e);
                return null;
            }
        }

        logger.trace("Couldn't find device matching address {}", deviceAddress);
        return null;
    }

    boolean checkMessage(WMBusDevice receivedDevice) {
        return true;
    }

    public void checkStatus() {
        // status check is relevant only if device is considered to be online - we determine if it should be marked
        // offline
        if (this.status != ThingStatus.ONLINE) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (lastUpdate == null || lastUpdate + frequencyOfUpdates <= currentTime) {
            logger.info("WMBus device was not seen since {}, marking it as offline", new Date(lastUpdate));
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @SuppressWarnings("unchecked")
    protected T parseDevice(WMBusDevice device) throws DecodingException {
        device.decode();
        return (T) device;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }
}
