package org.openhab.binding.wmbus.device;

import java.util.function.Predicate;

import org.openhab.binding.wmbus.device.techem.Record;

/**
 * Predicate for testing records reported by techem decoders.
 *
 * @author Łukasz Dywicki - initial contribution
 */
public interface RecordPredicate extends Predicate<Record<?>> {

    /**
     * Predicate/condition description.
     *
     * @return Textual description of predicate.
     */
    String description();

    /**
     * Arguments passed additionally to format description text.
     *
     * @return Arguments for formatting predicate description.
     */
    Object[] arguments();
}
