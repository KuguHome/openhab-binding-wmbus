package org.openhab.binding.wmbus.device.itron;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;

public class ItronConfigStatusDataParserTest {

    private ItronConfigStatusDataParser parser = new ItronConfigStatusDataParser(HexUtils.hexToBytes("0C173E7E00208080"));

    @Test
    public void testParser() {
        assertThat(parser.getBillingDate())
            .isEqualTo(12);

        assertThat(parser.isRemovalOccurred())
            .isEqualTo(true);

        assertThat(parser.isProductInstalled())
            .isEqualTo(true);

        assertThat(parser.getOperationMode())
            .isEqualTo(1);

        assertThat(parser.isPerimeterIntrusionOccurred())
            .isEqualTo(true);

        assertThat(parser.isSmokeInletBlockedOccurred())
            .isEqualTo(false);

        assertThat(parser.isOutOfRangeTemperatureOccurred())
            .isEqualTo(false);

        assertThat(parser.getProductCode())
            .isEqualTo((byte) 0x3E);
    }

}