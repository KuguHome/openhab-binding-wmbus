package org.openhab.binding.wmbus.device.techem;

import org.assertj.core.api.Assertions;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.internal.ThingImpl;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;

public class TechemHandlerFactoryTest implements TechemBindingConstants {

    private final TechemHandlerFactory handlerFactory = new TechemHandlerFactory();

    @Before
    public void setUp() {
        handlerFactory.setTechemFrameDecoder(new CompositeTechemFrameDecoder());
    }

    @Test
    public void testLegacyHKVSupport() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_HKV, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testHKV61Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_HKV61, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testHKV64Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_HKV64, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testHKV69Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_HKV69, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testHKV76Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_HKV76, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testWZ62Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_WARM_WATER_METER, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testWZ72Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_COLD_WATER_METER, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

    @Test
    public void testWZ43Support() {
        ThingImpl thing = new ThingImpl(THING_TYPE_TECHEM_HEAT_METER, "00000");

        ThingHandler handler = handlerFactory.createHandler(thing);
        Assertions.assertThat(handler).isNotNull();
    }

}
