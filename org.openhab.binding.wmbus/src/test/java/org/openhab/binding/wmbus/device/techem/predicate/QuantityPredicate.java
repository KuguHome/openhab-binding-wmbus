package org.openhab.binding.wmbus.device.techem.predicate;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.assertj.core.api.Assertions;
import org.openhab.binding.wmbus.device.techem.Record;

public class QuantityPredicate extends FloatPredicate {

    private final Unit<?> unit;

    public QuantityPredicate(Record.Type type, double expectedValue, Unit<?> unit) {
        super(type, expectedValue);
        this.unit = unit;
    }

    @Override
    protected void testValue(Object value) {
        Assertions.assertThat(value).isInstanceOf(Quantity.class);

        Quantity<?> quantity = (Quantity<?>) value;
        Assertions.assertThat(quantity.getValue().floatValue()).isEqualTo(Double.valueOf(expectedValue).floatValue());
        Assertions.assertThat(quantity.getUnit()).isEqualTo(unit);
    }

    @Override
    public String description() {
        return "record of type %s, with value %f in %s";
    }

    @Override
    public Object[] arguments() {
        return new Object[] { type, expectedValue, unit };
    }
}
