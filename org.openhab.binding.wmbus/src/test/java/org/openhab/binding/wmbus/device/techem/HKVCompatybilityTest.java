package org.openhab.binding.wmbus.device.techem;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;

import javax.measure.Quantity;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.wmbus.device.AbstractWMBusTest;
import org.openhab.binding.wmbus.device.techem.Record.Type;
import org.openhab.binding.wmbus.device.techem.decoder.CompositeTechemFrameDecoder;

@Ignore // 0x76 variant is smoke detector and not HKV
@Deprecated
@SuppressWarnings("deprecation")
public class HKVCompatybilityTest extends AbstractWMBusTest {

    private final CompositeTechemFrameDecoder reader = new CompositeTechemFrameDecoder();

    @Test
    public void testLegacyParsingAfterMigration() throws Exception {
        TechemDevice newDevice = reader.decode(message(MESSAGE_118_SD));

        TechemHKV oldDevice = new TechemHKV(message(MESSAGE_118_SD).getOriginalMessage(), null);
        oldDevice.decode();

        LocalDateTime lastDate = oldDevice.getLastDate().toLocalDateTime();
        LocalDateTime curDate = oldDevice.getCurDate().toLocalDateTime();

        int lastVal = oldDevice.getLastVal();
        int curVal = oldDevice.getCurVal();
        float t1 = oldDevice.getT1();
        float t2 = oldDevice.getT2();

        Function<Object, Integer> intCast = value -> ((Float) value).intValue();
        Function<Object, Float> quantityCast = value -> ((Quantity) value).getValue().floatValue();

        Assertions.assertThat(record(newDevice, Type.PAST_READING_DATE, LocalDateTime.class::cast)).isCloseTo(lastDate,
                Assertions.within(10, ChronoUnit.SECONDS));
        Assertions.assertThat(record(newDevice, Type.CURRENT_READING_DATE, LocalDateTime.class::cast))
                .isCloseTo(curDate, Assertions.within(10, ChronoUnit.SECONDS));

        Assertions.assertThat(record(newDevice, Type.PAST_VOLUME, intCast)).isEqualTo(lastVal);
        Assertions.assertThat(record(newDevice, Type.CURRENT_VOLUME, intCast)).isEqualTo(curVal);
        Assertions.assertThat(record(newDevice, Type.ROOM_TEMPERATURE, quantityCast)).isEqualTo(t1);
        Assertions.assertThat(record(newDevice, Type.RADIATOR_TEMPERATURE, quantityCast)).isEqualTo(t2);
    }

    @Test
    public void testLegacyParsingBeforeMigration() throws Exception {
        TechemDevice newDevice = reader.decode(message(MESSAGE_118_SD));

        LegacyTechemHKV oldDevice = new LegacyTechemHKV(message(MESSAGE_118_SD).getOriginalMessage());
        oldDevice.decode();

        LocalDateTime lastDate = oldDevice.getLastDate().toLocalDateTime();
        LocalDateTime curDate = oldDevice.getCurDate().toLocalDateTime();

        int lastVal = oldDevice.getLastVal();
        int curVal = oldDevice.getCurVal();
        float t1 = oldDevice.getT1();
        float t2 = oldDevice.getT2();

        Function<Object, Integer> intCast = value -> ((Float) value).intValue();
        Function<Object, Float> quantityCast = value -> ((Quantity) value).getValue().floatValue();

        Assertions.assertThat(record(newDevice, Type.PAST_READING_DATE, LocalDateTime.class::cast)).isCloseTo(lastDate,
                Assertions.within(10, ChronoUnit.SECONDS));
        Assertions.assertThat(record(newDevice, Type.CURRENT_READING_DATE, LocalDateTime.class::cast))
                .isCloseTo(curDate, Assertions.within(10, ChronoUnit.SECONDS));

        Assertions.assertThat(record(newDevice, Type.PAST_VOLUME, intCast)).isEqualTo(lastVal);
        Assertions.assertThat(record(newDevice, Type.CURRENT_VOLUME, intCast)).isEqualTo(curVal);
        Assertions.assertThat(record(newDevice, Type.ROOM_TEMPERATURE, quantityCast)).isEqualTo(t1);
        Assertions.assertThat(record(newDevice, Type.RADIATOR_TEMPERATURE, quantityCast)).isEqualTo(t2);
    }

    private Object record(TechemDevice device, Type type) {
        return record(device, type, Function.identity());
    }

    private <T> T record(TechemDevice device, Type type, Function<Object, T> converter) {
        return device.getRecord(type).map(Record::getValue).map(converter)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find record of type " + type.name()));
    }

}
