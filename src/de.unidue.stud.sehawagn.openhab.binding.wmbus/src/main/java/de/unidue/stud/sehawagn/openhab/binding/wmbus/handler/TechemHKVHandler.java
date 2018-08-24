package de.unidue.stud.sehawagn.openhab.binding.wmbus.handler;

import static de.unidue.stud.sehawagn.openhab.binding.wmbus.WMBusBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.TechemHKV;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.WMBusDevice;

public class TechemHKVHandler extends WMBusDeviceHandler {

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.newHashSet(THING_TYPE_TECHEM_HKV);
	private final Logger logger = LoggerFactory.getLogger(TechemHKVHandler.class);

	private TechemHKV techemDevice;

	public TechemHKVHandler(Thing thing) {
		super(thing);
	}

	@Override
	public void initialize() {
		super.initialize();
		if (wmbusDevice instanceof TechemHKV) {
			techemDevice = (TechemHKV) wmbusDevice;
			updateStatus(ThingStatus.ONLINE);
		} else {
			logger.error("This message is not a techem HKV, so the handler is not suited.");
			updateStatus(ThingStatus.OFFLINE);
		}
	}

	@Override
	public void onChangedWMBusDevice(WMBusDevice receivedDevice) {
		if (receivedDevice.getDeviceId().equals(deviceId)) {
			wmbusDevice = receivedDevice;
			if (wmbusDevice instanceof TechemHKV) {
				techemDevice = (TechemHKV) wmbusDevice;
			}
			triggerRefresh();
		}
	}

	@Override
	public void handleCommand(ChannelUID channelUID, Command command) {
		logger.trace("handleCommand {} for channel {}", command.toString(), channelUID.toString());
		if (command == RefreshType.REFRESH) {
			State newState = UnDefType.NULL;

			if (techemDevice != null) {
				switch (channelUID.getId()) {
				case CHANNEL_ROOMTEMPERATURE: {
					newState = new DecimalType(techemDevice.getT1());
					break;
				}
				case CHANNEL_RADIATORTEMPERATURE: {
					newState = new DecimalType(techemDevice.getT2());
					break;
				}
				case CHANNEL_CURRENTREADING: {
					newState = new DecimalType(techemDevice.getCurVal());
					break;
				}
				case CHANNEL_LASTREADING: {
					newState = new DecimalType(techemDevice.getLastVal());
					break;
				}
				case CHANNEL_RECEPTION: {
					newState = new DecimalType(techemDevice.getRssi());
					break;
				}
				case CHANNEL_LASTDATE: {
					newState = new DateTimeType(techemDevice.getLastDate());
					break;
				}
				case CHANNEL_CURRENTDATE: {
					newState = new DateTimeType(techemDevice.getCurDate());
					break;
				}
				case CHANNEL_ALMANAC: {
					newState = new StringType(techemDevice.getHistory());
					break;
				}
				default: {
					logger.debug("handleCommand(): channel {} unknown", channelUID.getId());
					break;
				}
				}
				if (newState != UnDefType.NULL) {
					logger.trace("handleCommand(): assigning new state {} to channel {}", newState, channelUID.getId());
					updateState(channelUID.getId(), newState);
				}
			}
		}
	}
}
