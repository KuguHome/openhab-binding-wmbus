package org.openhab.binding.wmbus.device.techem;

import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.device.AbstractWMBusTest;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.internal.DynamicBindingConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;
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
    public void testWZ7062Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_112_WARM_WATER);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_WARM_WATER_METER);
    }

    @Test
    public void testWZ7072Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_112_COLD_WATER);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_COLD_WATER_METER);
    }

    @Test
    public void testWZ7462Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_116_WARM_WATER);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_WARM_WATER_METER);
    }

    @Test
    public void testWZ7472Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_116_COLD_WATER);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_COLD_WATER_METER);
    }

    @Test
    public void testWMZ43Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_113_HEAT);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HEAT_METER);
    }

    @Test
    public void testHKV4543Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_69_HKV);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV45);
    }

    @Test
    public void testHKV6480Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_100_HKV);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV64);
    }

    @Test
    public void testHKV6980Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_105_HKV);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV69);
    }

    @Test
    public void testHKV9480Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_148_HKV);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_HKV94);
    }

    @Test
    public void testSD76Support() throws Exception {
        DiscoveryResult result = result(MESSAGE_118_SD_1);
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getThingTypeUID()).isEqualTo(THING_TYPE_TECHEM_SD76);
    }

    private DiscoveryResult result(String message) throws DecodingException {
        when(adapter.getUID()).thenReturn(new ThingUID(WMBusBindingConstants.THING_TYPE_BRIDGE, "bridge0"));

        return discoverer.createResult(message(message, adapter));
    }
}
