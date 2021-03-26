package org.openhab.binding.wmbus.device.techem;

import java.time.LocalDate;
import java.util.function.Consumer;

import javax.measure.Unit;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.openhab.binding.wmbus.device.AbstractWMBusTest;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;
import org.openhab.binding.wmbus.device.techem.predicate.FloatPredicate;
import org.openhab.binding.wmbus.device.techem.predicate.IntegerPredicate;
import org.openhab.binding.wmbus.device.techem.predicate.LocalDatePredicate;
import org.openhab.binding.wmbus.device.techem.predicate.QuantityPredicate;
import org.openhab.binding.wmbus.device.techem.predicate.RssiPredicate;
import org.openhab.binding.wmbus.device.techem.predicate.StringPredicate;
import org.openhab.core.library.unit.SIUnits;
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

        Assertions.assertThat(device.getMeasurements()).hasSize(8).areAtLeastOne(record(Record.Type.STATUS, 0))
                .areAtLeastOne(record(Record.Type.COUNTER, 3))
                .areAtLeastOne(record(Record.Type.ALMANAC, "4;4;3;3;3;3;3;3;2;3;3;3;3;3;3;3;4;2;4"))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 6.5, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 59.7, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testColdWater112Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_112_COLD_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.COLD_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH112114_16.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(6).areAtLeastOne(record(Record.Type.STATUS, 0))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 36.3, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 190.2, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testWarmWater116Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_116_WARM_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.WARM_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH11698_6.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(8).areAtLeastOne(record(Record.Type.STATUS, 6))
                .areAtLeastOne(record(Record.Type.COUNTER, 0))
                .areAtLeastOne(record(Record.Type.ALMANAC, "1;1;1;1;0;1;1;0;0;1;0;0;1;1;2;2;1;2;2;1;2;1;1;2;2;1;230;0"))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 2.1, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 7.5, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testColdWater116Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_116_COLD_WATER));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemWaterMeter.class,
                expectedDevice(DeviceType.COLD_WATER_METER));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH116114_16.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(6).areAtLeastOne(record(Record.Type.STATUS, 6))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 18.1, Units.CUBIC_METRE))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 43.5, Units.CUBIC_METRE)).areAtLeastOne(rssi());
    }

    @Test
    public void testHeat113Parser() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_113_HEAT));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatMeter.class,
                expectedDevice(DeviceType.HEAT_METER));
        Assertions.assertThat(device.getDeviceType())
                .isEqualTo(TechemBindingConstants._68TCH11367_4_A2.getTechemType());

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

        Assertions.assertThat(device.getMeasurements()).hasSize(7).areAtLeastOne(record(Record.Type.STATUS, 0))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 5240.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 18727.0)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV6480() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_100_HKV));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH100128_8.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(7).areAtLeastOne(record(Record.Type.STATUS, 17))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 65.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 104.0)).areAtLeastOne(rssi());
    }

    @Test
    public void testHKV6980() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_105_HKV));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemHeatCostAllocator.class,
                expectedDevice(DeviceType.HEAT_COST_ALLOCATOR));
        Assertions.assertThat(device.getDeviceType()).isEqualTo(TechemBindingConstants._68TCH105128_8.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(10).areAtLeastOne(record(Record.Type.STATUS, 17))
                .areAtLeastOne(record(Record.Type.COUNTER, 16))
                .areAtLeastOne(record(Record.Type.ALMANAC,
                        "1;0;0;2;0;0;0;0;0;0;0;0;0;0;17;11;0;32;54;29;34;31;110;146;135;73;0"))
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

        Assertions.assertThat(device.getMeasurements()).hasSize(10).areAtLeastOne(record(Record.Type.STATUS, 15))
                .areAtLeastOne(record(Record.Type.COUNTER, 0))
                .areAtLeastOne(record(Record.Type.ALMANAC,
                        "0;0;0;0;0;0;7;31;36;30;69;78;66;88;132;120;119;94;79;46;20;0;0;0;0;219;0"))
                .areAtLeastOne(record(Record.Type.CURRENT_VOLUME, 258.0))
                .areAtLeastOne(record(Record.Type.PAST_VOLUME, 412.0))
                .areAtLeastOne(record(Record.Type.ROOM_TEMPERATURE, 26.48, SIUnits.CELSIUS))
                .areAtLeastOne(record(Record.Type.RADIATOR_TEMPERATURE, 26.31, SIUnits.CELSIUS)).areAtLeastOne(rssi());
    }

    @Test
    public void testSD76F0() throws Exception {
        TechemDevice device = reader.decode(message(MESSAGE_118_SD_3));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemSmokeDetector.class,
                expectedDevice(DeviceType.SMOKE_DETECTOR));
        Assertions.assertThat(device.getDeviceType())
                .isEqualTo(TechemBindingConstants._68TCH118255_161_A0.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(4).areAtLeastOne(record(Type.STATUS, 0))
                .areAtLeastOne(record(Type.CURRENT_READING_DATE, LocalDate.of(LocalDate.now().getYear(), 3, 23)))
                .areAtLeastOne(record(Type.CURRENT_READING_DATE_SMOKE, LocalDate.of(2019, 11, 27)))
                .areAtLeastOne(rssi());
    }

    @Test
    public void testSD76F0_extra() throws Exception {
        // just another test frame
        TechemDevice device = reader.decode(message(MESSAGE_118_SD_2));

        Assertions.assertThat(device).isNotNull().isInstanceOfSatisfying(TechemSmokeDetector.class,
                expectedDevice(DeviceType.SMOKE_DETECTOR));
        Assertions.assertThat(device.getDeviceType())
                .isEqualTo(TechemBindingConstants._68TCH118255_161_A0.getTechemType());

        Assertions.assertThat(device.getMeasurements()).hasSize(4).areAtLeastOne(record(Type.STATUS, 0))
                .areAtLeastOne(record(Type.CURRENT_READING_DATE, LocalDate.of(LocalDate.now().getYear(), 2, 22)))
                .areAtLeastOne(record(Type.CURRENT_READING_DATE_SMOKE, LocalDate.of(2018, 11, 15)))
                .areAtLeastOne(rssi());
    }

    private Condition<Record<?>> record(Type type, String expectedValue) {
        StringPredicate predicate = new StringPredicate(type, expectedValue);

        return new Condition<>(predicate, predicate.description(), predicate.arguments());
    }

    private Condition<Record<?>> record(Type type, LocalDate expectedValue) {
        LocalDatePredicate predicate = new LocalDatePredicate(type, expectedValue);

        return new Condition<>(predicate, predicate.description(), predicate.arguments());
    }

    private Condition<Record<?>> record(Type type, int expectedValue) {
        IntegerPredicate predicate = new IntegerPredicate(type, expectedValue);

        return new Condition<>(predicate, predicate.description(), predicate.arguments());
    }

    private Condition<Record<?>> record(Type type, double expectedValue) {
        FloatPredicate predicate = new FloatPredicate(type, expectedValue);

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
}
