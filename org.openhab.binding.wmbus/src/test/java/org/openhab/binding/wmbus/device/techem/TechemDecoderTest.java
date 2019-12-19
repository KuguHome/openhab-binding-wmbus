package org.openhab.binding.wmbus.device.techem;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.junit.Test;
import org.openhab.binding.wmbus.device.AbstractWMBusTest;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;
import org.openmuc.jmbus.DeviceType;

import tec.uom.se.unit.Units;

public class TechemDecoderTest extends AbstractWMBusTest {

    private final CompositeTechemFrameDecoder reader = new CompositeTechemFrameDecoder();

    @Test
    public void testWarmWater112Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_112_WARM_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.WARM_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH11298_6.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 6.5, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 59.7, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testColdWater112Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_112_COLD_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.COLD_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH112114_16.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 36.3, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 190.2, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testWarmWater116Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_116_WARM_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.WARM_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH11698_6.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 2.1, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 7.5, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testColdWater116Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_116_COLD_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.COLD_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH116114_16.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 18.1, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 43.5, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testHeat113Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_113_HEAT));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatMeter.class,
                expectedDevice(DeviceType.HEAT_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH11367_4_A2.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(5)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 1769472.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 8913920.0)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV45() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_69_HKV));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH6967_8.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(6)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 5240.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 18727.0)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV6480() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_100_HKV));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH100128_8.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(6)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 65.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 104.0)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV6980() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_105_HKV));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH105128_8.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(8)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 410.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 1999.0))
                .areAtLeastOne(record(Record.Type.ROOM_TEMPERATURE, 21.52, SIUnits.CELSIUS))
                .areAtLeastOne(record(Record.Type.RADIATOR_TEMPERATURE, 23.73, SIUnits.CELSIUS)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV148() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_148_HKV));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH148128_8.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(8)
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 657.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 412.0))
                .areAtLeastOne(record(Record.Type.ROOM_TEMPERATURE, 26.48, SIUnits.CELSIUS))
                .areAtLeastOne(record(Record.Type.RADIATOR_TEMPERATURE, 26.31, SIUnits.CELSIUS)).areAtLeastOne(rssi());
    }

    @Test
    public void testSD76F0() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_118_SD_1));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemSmokeDetector.class,
                expectedDevice(DeviceType.SMOKE_DETECTOR));
        Assertions.assertThat(device.getDeviceType())
                .isEqualTo(TechemBindingConstants._68TCH118255_161_A0.getTechemType());

        // FIXME add parsing of frame
        Assertions.assertThat(device.getMeasurements()).isEmpty();
    }

    @Test
    public void testSD76F0_extra() throws Exception {
        // just another test frame
        TechemDevice device = reader.decode(message(MESSAGE_118_SD_2));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemSmokeDetector.class,
                expectedDevice(DeviceType.SMOKE_DETECTOR));
        Assertions.assertThat(device.getDeviceType())
                .isEqualTo(TechemBindingConstants._68TCH118255_161_A0.getTechemType());

        // FIXME add parsing of frame
        Assertions.assertThat(device.getMeasurements()).isEmpty();
    }

    private Condition<Record<?>> record(Type type, double expectedValue) {
        ValuePredicate predicate = new ValuePredicate(type, expectedValue);

        return new Condition<>(predicate, predicate.description(), predicate.arguments());
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
            Assertions.assertThat(device.getTechemDeviceType()).isEqualTo(deviceType);
        };
    }

    static class ValuePredicate implements Predicate<Record<?>> {

        protected final Type type;
        protected final float expectedValue;

        ValuePredicate(Type type, Double expectedValue) {
            this.type = type;
            this.expectedValue = expectedValue.floatValue();
        }

        @Override
        public boolean test(Record<?> record) {
            try {
                Assertions.assertThat(record.getType()).isEqualTo(type);

                testValue(record.getValue());
            } catch (AssertionError e) {
                return false;
            }
            return true;
        }

        protected void testValue(Object value) {
            Assertions.assertThat(value).isInstanceOf(Float.class);

            Assertions.assertThat(((Float) value)).isEqualTo(expectedValue);
        }

        String description() {
            return "record of type %s, with value %f";
        }

        Object[] arguments() {
            return new Object[] { type, expectedValue };
        }
    }

    static class QuantityPredicate extends ValuePredicate {

        private final Unit<?> unit;

        QuantityPredicate(Type type, double expectedValue, Unit<?> unit) {
            super(type, expectedValue);
            this.unit = unit;
        }

        @Override
        protected void testValue(Object value) {
            Assertions.assertThat(value).isInstanceOf(Quantity.class);

            Quantity<?> quantity = (Quantity<?>) value;
            Assertions.assertThat(quantity.getValue().floatValue())
                    .isEqualTo(Double.valueOf(expectedValue).floatValue());
            Assertions.assertThat(quantity.getUnit()).isEqualTo(unit);
        }

        @Override
        String description() {
            return "record of type %s, with value %f in %s";
        }

        @Override
        Object[] arguments() {
            return new Object[] { type, expectedValue, unit };
        }
    }

    static class RssiPredicate implements Predicate<Record<?>> {

        private final int expectedValue;

        RssiPredicate(int expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public boolean test(Record<?> record) {
            try {
                Assertions.assertThat(record.getType()).isEqualTo(Type.RSSI);

                Object value = record.getValue();
                Assertions.assertThat(value).isEqualTo(expectedValue);
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
