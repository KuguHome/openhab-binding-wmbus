package org.openhab.binding.wmbus.device.techem.predicate;

import org.assertj.core.api.Assertions;
import org.openhab.binding.wmbus.device.RecordPredicate;
import org.openhab.binding.wmbus.device.techem.Record;

public class FloatPredicate implements RecordPredicate {

    protected final Record.Type type;
    protected final float expectedValue;

    public FloatPredicate(Record.Type type, Double expectedValue) {
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
        Assertions.assertThat(value).isInstanceOf(Float.class).isEqualTo(expectedValue);
    }

    public String description() {
        return "record of type %s, with value %f";
    }

    public Object[] arguments() {
        return new Object[] { type, expectedValue };
    }
}
