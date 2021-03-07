/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools.processor;

import java.util.List;
import java.util.Map;

import org.openhab.binding.wmbus.tools.Processor;

/**
 * Helper type to orchestrate execution of several frame processors at once.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class Processors {

    public static <T> T process(final T value, Map<String, Object> context, List<Processor<T>> processors) {
        if (processors.isEmpty()) {
            return value;
        }

        T result = value;
        for (Processor<T> processor : processors) {
            result = processor.process(result, context);
        }

        return result;
    }
}
