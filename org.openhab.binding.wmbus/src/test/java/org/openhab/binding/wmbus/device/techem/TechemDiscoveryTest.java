package org.openhab.binding.wmbus.device.techem;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.internal.DynamicBindingConfiguration;
import org.openmuc.jmbus.DecodingException;

@RunWith(MockitoJUnitRunner.class)
public class TechemDiscoveryTest extends AbstractWMBusTest implements TechemBindingConstants {

    private final TechemDiscoveryParticipant discoverer = new TechemDiscoveryParticipant();

    @Mock
    private WMBusAdapter adapter;

    @Before
    public void setUp() {
        discoverer.setTechemFrameDecoder(new CompositeTechemFrameDecoder());
        discoverer.setBindingConfiguration(new DynamicBindingConfiguration());
    }

    @Test
    public void testHKV64Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_100);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV64);
    }

    @Test
    public void testHKV69Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_105);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV69);
    }

    @Test
    public void testHKV76Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_118);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV76);
    }

    @Test
    public void testWZ62Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_116_WARM_WATER);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_WARM_WATER_METER);
    }

    @Test
    public void testWZ72Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_116_COLD_WATER);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_COLD_WATER_METER);
    }

    @Test
    public void testWZ43Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_113_HEAT);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HEAT_METER);
    }

    private DiscoveryResult result(String message) throws DecodingException {
        when(adapter.getUID()).thenReturn(new ThingUID(WMBusBindingConstants.THING_TYPE_BRIDGE, "bridge0"));

        return discoverer.createResult(message(message, adapter));
    }

}
