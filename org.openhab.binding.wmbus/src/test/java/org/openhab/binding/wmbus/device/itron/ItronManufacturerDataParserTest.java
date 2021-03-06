package org.openhab.binding.wmbus.device.itron;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.Ignore;
import org.junit.Test;
import org.openhab.binding.wmbus.device.techem.decoder.Buffer;
import org.openhab.core.util.HexUtils;

public class ItronManufacturerDataParserTest {

    public static final byte[] _2019_12_29_0313 = hex("0D 23 7D 2C");
    public static final byte[] _2020_01_09_1241 = hex("29 2C 89 21");
    public static final byte[] _2020_01_08_2121 = hex("15 35 88 21");
    public static final byte[] _2020_01_10_132057 = hex("39 14 AD 8A 21");

    private static byte[] hex(String hexStr) {
        return HexUtils.hexToBytes(hexStr.replace(" ", ""));
    }

    @Test
    public void testShortDate() {
        assertThat(new ItronManufacturerDataParser(new Buffer(_2019_12_29_0313)).readShortDateTime())
                .isEqualTo(LocalDateTime.of(2019, 12, 29, 3, 13));

        assertThat(new ItronManufacturerDataParser(new Buffer(_2020_01_09_1241)).readShortDateTime())
                .isEqualTo(LocalDateTime.of(2020, 1, 9, 12, 41));

        assertThat(new ItronManufacturerDataParser(new Buffer(_2020_01_08_2121)).readShortDateTime())
                .isEqualTo(LocalDateTime.of(2020, 1, 8, 21, 21));
    }

    @Test
    @Ignore // parsing logic is not yet done
    public void testLongDate() {
        assertThat(new ItronManufacturerDataParser(new Buffer(_2020_01_10_132057)).readLongDateTime())
                .isEqualTo(LocalDateTime.of(2020, 1, 10, 13, 20, 57));
    }
}
