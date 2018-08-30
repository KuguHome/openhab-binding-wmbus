package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openmuc.jmbus.DataRecord;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.RecordType;

@Component(service = { ADEUNISGasMeterHandler.class,
        WMBusDeviceHandler.class }, properties = "OSGI-INF/adeunis_rf.properties")
public class ADEUNISGasMeterHandler extends WMBusDeviceHandler {

    public static String THING_TYPE_NAME_ADEUNIS_GAS_METER_3;
    private Set<ThingTypeUID> supportedThingTypes;
    public static ThingTypeUID THING_TYPE_ADEUNIS_GAS_METER_3;
    public static String CHANNEL_CURRENT_VOLUME_INST_VAL = "current_volume_instant_value_m3";

    public static final Logger logger = LoggerFactory.getLogger(ADEUNISGasMeterHandler.class);

    private static RecordType TYPE_CURRENT_VOLUME_INST_VAL;

    private int dib;
    private int vib;

    public ADEUNISGasMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
                + command.toString());

        if (command == RefreshType.REFRESH) {
            logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
            State newState = UnDefType.NULL;
            if (wmbusDevice != null) {
                logger.trace("handleCommand(): (3/5) deviceMessage != null");
                if (CHANNEL_CURRENT_VOLUME_INST_VAL.equals(channelUID.getId())) {
                    logger.trace("handleCommand(): (4/5): got a valid channel: VOLUME_INST_VAL");
                    DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_VOLUME_INST_VAL);
                    if (record != null) {
                        newState = new DecimalType(record.getScaledDataValue());
                    } else {
                        logger.trace("handleCommand(): record not found in message");
                    }
                } else {
                    logger.debug(
                            "handleCommand(): (4/5): no channel to put this value into found: " + channelUID.getId());
                }
                logger.trace("handleCommand(): (5/5) assigning new state to channel '" + channelUID.getId().toString()
                        + "': " + newState.toString());
                updateState(channelUID.getId(), newState);

            }

        }

    }

    @Activate
    protected void activate(Map<String, String> properties) {
        this.dib = Integer.valueOf(properties.get("volume.dib"));
        this.vib = Integer.valueOf(properties.get("volume.vib"));
        TYPE_CURRENT_VOLUME_INST_VAL = new RecordType(dib, vib);
        THING_TYPE_NAME_ADEUNIS_GAS_METER_3 = properties.get("thing.type");
        THING_TYPE_ADEUNIS_GAS_METER_3 = new ThingTypeUID(properties.get("binding.id"),
                THING_TYPE_NAME_ADEUNIS_GAS_METER_3);
        supportedThingTypes = Sets.newHashSet(THING_TYPE_ADEUNIS_GAS_METER_3);
        CHANNEL_CURRENT_VOLUME_INST_VAL = properties.get("channel.volume");
    }

    @Deactivate
    protected void deactivate() {
    }

    public Set<ThingTypeUID> getSupportedThingTypes() {
        return supportedThingTypes;
    }

}
