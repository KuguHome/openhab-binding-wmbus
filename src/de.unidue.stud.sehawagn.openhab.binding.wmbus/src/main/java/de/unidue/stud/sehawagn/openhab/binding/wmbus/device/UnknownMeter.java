package de.unidue.stud.sehawagn.openhab.binding.wmbus.device;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
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

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.RecordType;

@Component(service = { UnknownMeter.class }, properties = "OSGI-INF/virtual.properties")
public class UnknownMeter extends Meter {

    public static final Logger logger = LoggerFactory.getLogger(UnknownMeter.class);

    public static final String CHANNEL_CODE_VIRTUAL_BRIDGE = "wmbusvirtualbridge_code";

    private static RecordType TYPE_CURRENT_VOLUME_INST_VAL;

    private int dib;
    private int vib;

    @Activate
    protected void activate(Map<String, String> properties) {
    }

    @Deactivate
    protected void deactivate() {
    }

    public class UnknownMeterHandler extends WMBusDeviceHandler {

        public UnknownMeterHandler(Thing thing) {
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
                    if (CHANNEL_CODE_VIRTUAL_BRIDGE.equals(channelUID.getId())) {
                        logger.trace("handleCommand(): (4/5): got a valid channel: VOLUME_INST_VAL");
                        DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_VOLUME_INST_VAL);
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("handleCommand(): record not found in message");
                        }
                    } else {
                        logger.debug("handleCommand(): (4/5): no channel to put this value into found: "
                                + channelUID.getId());
                    }
                    logger.trace("handleCommand(): (5/5) assigning new state to channel '"
                            + channelUID.getId().toString() + "': " + newState.toString());
                    updateState(channelUID.getId(), newState);

                }

            }

        }

    }
}
