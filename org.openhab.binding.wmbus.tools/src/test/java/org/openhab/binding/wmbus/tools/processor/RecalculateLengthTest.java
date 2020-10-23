package org.openhab.binding.wmbus.tools.processor;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class RecalculateLengthTest {

    RecalculateLength recalculateLength = new RecalculateLength();
    Map<String, Object> context = new HashMap<>();

   @Test
    public void testValidFrame() throws DecodingException {
        String frame = "2e44972682213893071A7AB80020A56BE69947E41B7346595CCA02F2BF0E62C906B80BD18240811FD4879DA1296D3F";
        String expectedFrame = "2e44972682213893071A7AB80020A56BE69947E41B7346595CCA02F2BF0E62C906B80BD18240811FD4879DA1296D3F";
        assertThat(recalculateLength.process(frame, context))
            .isEqualTo(expectedFrame);
        byte[] payload = HexUtils.hexToBytes(expectedFrame);
        WMBusMessage message = VirtualWMBusMessageHelper.decode(payload, 100, Collections.emptyMap());
        assertThatThrownBy(() -> message.getVariableDataResponse().decode())
            .isInstanceOf(DecodingException.class).hasMessageContaining("address key");
    }

    @Test
    public void testShortHeader() throws DecodingException {
        String frame = "3244972682213893071A7AB80040A56BE69947E41B7346595CCA02F2BF0E62C906B80BD18240811FD4879DA1296D3F4279385A";
        String expectedFrame = "2e44972682213893071A7AB80020A56BE69947E41B7346595CCA02F2BF0E62C906B80BD18240811FD4879DA1296D3F";
        assertThat(recalculateLength.process(frame, context))
                .isEqualTo(expectedFrame);
        byte[] payload = HexUtils.hexToBytes(expectedFrame);
        WMBusMessage message = VirtualWMBusMessageHelper.decode(payload, 100, Collections.emptyMap());
        assertThatThrownBy(() -> message.getVariableDataResponse().decode())
                .isInstanceOf(DecodingException.class).hasMessageContaining("address key");
    }

    @Test
    public void testLongHeader() throws DecodingException {
        String frame = "3244C5140401806003077261885616C51400075B0B90054CC7D04A56C6919495f6565704D1D9085134926D705D40D2EE699337";
        String expectedFrame = "2644C5140401806003077261885616C51400075B0B10054CC7D04A56C6919495f6565704D1D908";
        assertThat(recalculateLength.process(frame, context))
                .isEqualTo(expectedFrame);
        byte[] payload = HexUtils.hexToBytes(expectedFrame);
        WMBusMessage message = VirtualWMBusMessageHelper.decode(payload, 100, Collections.emptyMap());
        System.out.println(message.getVariableDataResponse().getNumberOfEncryptedBlocks());
        assertThatThrownBy(() -> message.getVariableDataResponse().decode())
                .isInstanceOf(DecodingException.class).hasMessageContaining("secondary address");
    }

}