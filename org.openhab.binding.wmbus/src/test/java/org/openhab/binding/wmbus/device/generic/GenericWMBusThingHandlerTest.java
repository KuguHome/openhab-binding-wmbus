package org.openhab.binding.wmbus.device.generic;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.State;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.wmbus.RecordType;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusBridgeHandler;
import org.openhab.binding.wmbus.internal.units.CompositeUnitRegistry;
import org.openhab.io.transport.mbus.wireless.MapKeyStorage;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.DlmsUnit;

import com.google.common.collect.ImmutableMap;

/**
 * Test of generic handler and its behavior upon receipt of new wmbus frame.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@RunWith(MockitoJUnitRunner.class)
public class GenericWMBusThingHandlerTest {

    private static final String CHANNEL_VOLUME = "volume";
    private static final String DEVICE_ID = "10";
    private static final Map<String, Object> CONFIGURATION = ImmutableMap
            .of(WMBusBindingConstants.PROPERTY_DEVICE_ADDRESS, DEVICE_ID);

    private static final RecordType VOLUME = new RecordType(0x0C, 0x14);
    private static final Double VOLUME_VALUE = 10.0;

    private static final String THING_ID = "foobar";
    private static final String BRIDGE_ID = "usb0";
    private final static ThingUID THING_UID = new ThingUID(WMBusBindingConstants.THING_TYPE_METER, THING_ID);
    private final static ChannelUID CHANNEL_UID = new ChannelUID(THING_UID, CHANNEL_VOLUME);

    private final static Map<String, RecordType> CHANNEL_MAPPING = ImmutableMap.of(CHANNEL_VOLUME, VOLUME);

    private final GenericWMBusThingHandler<WMBusDevice> handler;

    @Mock
    public WMBusBridgeHandler adapter;

    @Mock
    private ThingHandlerCallback callback;

    public GenericWMBusThingHandlerTest() {
        handler = new GenericWMBusThingHandler<WMBusDevice>(createTestThing(), new MapKeyStorage(),
                new CompositeUnitRegistry(), CHANNEL_MAPPING) {
            @Override
            protected org.eclipse.smarthome.core.thing.Bridge getBridge() {
                return createTestBridge(adapter);
            };
        };

    }

    @Before
    public void setUp() {
        handler.initialize();
        handler.setCallback(callback);
    }

    @After
    public void tearDown() {
        handler.dispose();
    }

    @Test
    public void testChannelUpdate() throws Exception {
        WMBusDevice device = Mockito.mock(WMBusDevice.class);
        DataRecord data = Mockito.mock(DataRecord.class);

        Mockito.when(device.getDeviceId()).thenReturn(DEVICE_ID);
        Mockito.when(device.findRecord(VOLUME)).thenReturn(data);
        Mockito.when(data.getUnit()).thenReturn(DlmsUnit.CUBIC_METRE);
        Mockito.when(data.getScaledDataValue()).thenReturn(VOLUME_VALUE);
        handler.onChangedWMBusDevice(adapter, device);

        ArgumentCaptor<State> stateCapture = ArgumentCaptor.forClass(State.class);
        Mockito.verify(callback).stateUpdated(ArgumentMatchers.eq(CHANNEL_UID), stateCapture.capture());

        Assertions.assertThat(stateCapture.getAllValues()).hasSize(1);

        Assertions.assertThat(stateCapture.getAllValues().get(0))
                .isEqualTo(new QuantityType<>(VOLUME_VALUE, SIUnits.CUBIC_METRE));
    }

    public static Thing createTestThing() {
        ThingBuilder thing = ThingBuilder.create(WMBusBindingConstants.THING_TYPE_METER, THING_ID);
        thing.withConfiguration(new Configuration(CONFIGURATION));

        Channel channel = ChannelBuilder.create(CHANNEL_UID, "DecimalType").build();
        thing.withChannel(channel);

        return thing.build();
    }

    static Bridge createTestBridge(ThingHandler handler) {
        Bridge bridge = BridgeBuilder.create(WMBusBindingConstants.THING_TYPE_BRIDGE, BRIDGE_ID).build();
        bridge.setHandler(handler);
        return bridge;
    }

}
