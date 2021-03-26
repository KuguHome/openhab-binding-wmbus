package org.openhab.binding.wmbus.tools.processor;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import junit.framework.TestCase;

public class SkipCrcProcessorTest extends TestCase {

    SkipCrcProcessor stripCrc = new SkipCrcProcessor();
    Map<String, Object> context = new HashMap<>();

    @Test
    public void testFrameFormatB() {
        String input = "11111111111111111100007A1111111111111111111111111111110000";
        String output = "1111111111111111117A111111111111111111111111111111";
        String result = stripCrc.process(input, context);

        assertEquals(output, result);
    }

    @Test
    public void testFrame1() {
        String input = "111111111111111111000011111111111111111111111111110000";
        String output = "1111111111111111111111111111111111111111111111";
        String result = stripCrc.process(input, context);

        assertEquals(output, result);
    }

    @Test
    public void testFrame3() {
        String input = "11111111111111111100001111111111111111111111111111111100001111111111110000";
        String output = "11111111111111111111111111111111111111111111111111111111111111";
        String result = stripCrc.process(input, context);

        assertEquals(output, result);
    }
}
