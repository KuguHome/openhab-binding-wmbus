package org.openhab.binding.wmbus.device.techem;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.assertj.core.api.Condition;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.device.techem.TechemDecoderTest.QuantityPredicate;
import org.openhab.binding.wmbus.device.techem.TechemDecoderTest.RssiPredicate;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;

import tec.uom.se.unit.Units;

public class TechemDecoderTest {

    // 72A2 -> cold water
    private final static String MESSAGE_116_COLD_WATER = "2F446850122320417472A2069F23B301D016B50000000609090908080C09080A0A0A0A09080907080609090707060707000000";
    // 62A2 -> warm water
    private final static String MESSAGE_116_WARM_WATER = "2F446850878465427462A2069F234B00D016150000000101010100010100000100000101020201020201020101020201E60000";

    // 6480A0 -> HKV
    private final static String MESSAGE_100 = "2E446850382041606480A0119F236800D016410000000000000000000000000000000000000009090F110F0C09000FDA0000";
    // 6980A0 -> HKV
    private final static String MESSAGE_105 = "32446850591266506980A0119F23CF07E0169A01680845091A000100000200000000000000000000110B0020361D221F6E9287490000";

    private final static int RSSI = 10;

    private CompositeTechemFrameDecoder reader = new CompositeTechemFrameDecoder();

    @Test
    public void testColdWaterParser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_116_COLD_WATER));

        assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.COLD_WATER_METER));
        assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH1162552272);

        assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 18.1, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 43.5, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testWarmWaterParser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_116_WARM_WATER));

        assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.WARM_WATER_METER));
        assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH116255662);

        assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 2.1, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 7.5, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV6480() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_100));

        assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH100255864);

        assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 65.0, SIUnits.WATT))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 104.0, SIUnits.WATT)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV6980() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_105));

        assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH105255869);

        assertThat(device.getMeasurements()).hasSize(7)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 410.0, SIUnits.WATT))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 1999.0, SIUnits.WATT))
                .areAtLeastOne(record(Record.Type.ROOM_TEMPERATURE, 21.52, SIUnits.CELSIUS))
                .areAtLeastOne(record(Record.Type.RADIATOR_TEMPERATURE, 23.73, SIUnits.CELSIUS)).areAtLeastOne(rssi());
    }

    private Condition<Record<?>> record(Type type, double expectedValue, Unit<?> unit) {
        QuantityPredicate predicate = new QuantityPredicate(type, expectedValue, unit);

        return new Condition<>(predicate, predicate.description(), predicate.arguments());
    }

    private Condition<Record<?>> rssi() {
        RssiPredicate predicate = new RssiPredicate(RSSI);

        return new Condition<>(predicate, predicate.description(), predicate.arguments());
    }

    private <T extends TechemDevice> Consumer<T> expectedDevice(DeviceType deviceType) {
        return device -> {
            assertThat(device.getTechemDeviceType()).isEqualTo(deviceType);
        };
    }

    private WMBusDevice message(String messageHex) throws DecodingException {
        byte[] buffer = HexUtils.hexToBytes(messageHex);
        WMBusMessage message = VirtualWMBusMessageHelper.decode(buffer, RSSI, Collections.emptyMap());

        return new WMBusDevice(message, null);
    }

    static class QuantityPredicate implements Predicate<Record<?>> {

        private Type type;
        private double expectedValue;
        private Unit<?> unit;

        QuantityPredicate(Type type, double expectedValue, Unit<?> unit) {
            this.type = type;
            this.expectedValue = expectedValue;
            this.unit = unit;
        }

        @Override
        public boolean test(Record<?> record) {
            try {
                assertThat(record.getType()).isEqualTo(type);

                Object value = record.getValue();
                assertThat(value).isInstanceOf(Quantity.class);

                Quantity<?> quantity = (Quantity<?>) value;
                assertThat(quantity.getValue().floatValue()).isEqualTo(Double.valueOf(expectedValue).floatValue());
                assertThat(quantity.getUnit()).isEqualTo(unit);
            } catch (AssertionError e) {
                return false;
            }
            return true;
        }

        String description() {
            return "record of type %s, with value %f in %s";
        }

        Object[] arguments() {
            return new Object[] { type, expectedValue, unit };
        }
    }

    static class RssiPredicate implements Predicate<Record<?>> {

        private int expectedValue;

        RssiPredicate(int expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public boolean test(Record<?> record) {
            try {
                assertThat(record.getType()).isEqualTo(Type.RSSI);

                Object value = record.getValue();
                assertThat(value).isEqualTo(expectedValue);
            } catch (AssertionError e) {
                return false;
            }
            return true;
        }

        String description() {
            return "Missing RSSI record, with expected value %d";
        }

        Object[] arguments() {
            return new Object[] { expectedValue };
        }
    }
}
