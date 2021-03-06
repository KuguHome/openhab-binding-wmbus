package org.openhab.binding.wmbus.device.techem.predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.openhab.binding.wmbus.device.RecordPredicate;
import org.openhab.binding.wmbus.device.techem.Record;

public class LocalDatePredicate implements RecordPredicate {

    protected final Record.Type type;
    protected final LocalDate expectedValue;

    public LocalDatePredicate(Record.Type type, LocalDate expectedValue) {
        this.type = type;
        this.expectedValue = expectedValue;
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
        Assertions.assertThat(value).isInstanceOf(LocalDateTime.class);
        LocalDate date = ((LocalDateTime) value).toLocalDate();

        Assertions.assertThat(date).isEqualTo(expectedValue);
    }

    public String description() {
        return "record of type %s, with value %s";
    }

    public Object[] arguments() {
        return new Object[] { type, expectedValue };
    }
}
