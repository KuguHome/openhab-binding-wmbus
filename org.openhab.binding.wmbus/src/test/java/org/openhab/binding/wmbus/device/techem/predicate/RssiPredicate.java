package org.openhab.binding.wmbus.device.techem.predicate;

import org.openhab.binding.wmbus.device.techem.Record;

public class RssiPredicate extends IntegerPredicate {

    public RssiPredicate(int expectedValue) {
        super(Record.Type.RSSI, expectedValue);
    }

    public String description() {
        return "Missing RSSI record, with expected value %d";
    }
}
