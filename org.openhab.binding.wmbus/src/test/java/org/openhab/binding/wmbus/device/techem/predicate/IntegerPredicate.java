package org.openhab.binding.wmbus.device.techem.predicate;

import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.openhab.binding.wmbus.device.techem.Record;

public class IntegerPredicate implements Predicate<Record<?>> {

    protected final Record.Type type;
    protected final int expectedValue;

    public IntegerPredicate(Record.Type type, int expectedValue) {
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
        Assertions.assertThat(value).isInstanceOf(Integer.class).isEqualTo(expectedValue);
    }

    public String description() {
        return "record of type %s, with value %d";
    }

    public Object[] arguments() {
        return new Object[] { type, expectedValue };
    }
}
