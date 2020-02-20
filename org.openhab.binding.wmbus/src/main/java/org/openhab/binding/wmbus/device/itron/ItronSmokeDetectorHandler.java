package org.openhab.binding.wmbus.device.itron;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.UnitRegistry;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.generic.GenericWMBusThingHandler;
import org.openhab.binding.wmbus.device.techem.decoder.Buffer;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.openmuc.jmbus.DecodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;

public class ItronSmokeDetectorHandler extends GenericWMBusThingHandler<WMBusDevice> {

    private static final Map<String, RecordType> CHANNEL_MAPPING = ImmutableMap.of(
        ItronBindingConstants.CHANNEL_CURRENT_DATE, new RecordType(0x6, 0x6D),
        ItronBindingConstants.CHANNEL_CURRENT_DATE_NUMBER, new RecordType(0x06, 0x6D),
        ItronBindingConstants.CHANNEL_CURRENT_DATE_STRING, new RecordType(0x06, 0x6D)
    );

    private final Logger logger = LoggerFactory.getLogger(ItronSmokeDetectorHandler.class);
    private Map<String, Object> parsedFrame = new HashMap<>();

    public ItronSmokeDetectorHandler(Thing thing, KeyStorage keyStorage, UnitRegistry unitRegistry) {
        super(thing, keyStorage, unitRegistry, CHANNEL_MAPPING);
    }

    @Override
    protected WMBusDevice parseDevice(WMBusDevice device) throws DecodingException {
        WMBusDevice parsedDevice = super.parseDevice(device);

        byte[] manufacturerData = parsedDevice.getOriginalMessage().getVariableDataResponse().getManufacturerData();

        Buffer buffer = new Buffer(manufacturerData);
        ItronManufacturerDataParser parser = new ItronManufacturerDataParser(buffer);

        LocalDateTime eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_START_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_START_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_START_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_END_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_END_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_ALERT_END_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_BEEPER_STOPPED_DURING_SMOKE_ALERT_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_OCCURRED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_PERIMETER_INTRUSION_OBSTACLE_REMOVED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_SMOKE_INLET_BLOCKING_REMOVED_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEMPERATURE_OUT_OF_RANGE_DATE_STRING, eventDate);

        eventDate = parser.readShortDateTime();
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEST_SWITCH_DATE, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEST_SWITCH_DATE_NUMBER, eventDate);
        parsedFrame.put(ItronBindingConstants.CHANNEL_LAST_TEST_SWITCH_DATE_STRING, eventDate);

        parsedFrame.put(ItronBindingConstants.CHANNEL_NUMBER_OF_TEST_SWITCHES_OPERATED, buffer.readShort());
        parsedFrame.put(ItronBindingConstants.CHANNEL_PERIMETER_INTRUSION_DAY_COUNTER_CUMULATED, buffer.readShort());
        parsedFrame.put(ItronBindingConstants.CHANNEL_SMOKE_INLET_DAY_COUNTER_CUMULATED, buffer.available() < 2 ? buffer.readByte() : buffer.readShort());

        return parsedDevice;
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        if (parsedFrame.containsKey(channelUID.getId())) {
            // channel directly maps to manufacturer data appended to frame
            logger.debug("Mapping custom smoke detector channel {} to manufacturer data", channelUID);

            Object value = parsedFrame.get(channelUID.getId());
            if (value == null) {
                updateState(channelUID, UnDefType.NULL);
            } else if (value instanceof LocalDateTime) {
                updateState(channelUID, convertDate(value));
            } else if (value instanceof Number) {
                updateState(channelUID, new DecimalType(((Number) value).floatValue()));
            } else {
                logger.warn("Unsupported value type {}", value);
            }
        } else {
            // try to do a lookup based on channel to record mapping
            super.handleCommand(channelUID, command);
        }
    }



}
