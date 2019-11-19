package org.openhab.binding.wmbus.device;

import java.util.Collections;

import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;

public class AbstractWMBusTest {

    // -> cold water
    public final static String MESSAGE_112_COLD_WATER = "";
    // -> warm water
    public final static String MESSAGE_112_WARM_WATER = "";
    // 7472A2 -> cold water
    public final static String MESSAGE_116_COLD_WATER = "2F446850122320417472A2069F23B301D016B50000000609090908080C09080A0A0A0A09080907080609090707060707000000";
    // 7462A2 -> warm water
    public final static String MESSAGE_116_WARM_WATER = "2F446850878465427462A2069F234B00D016150000000101010100010100000100000101020201020201020101020201E60000";

    // 7143A0 -> heat meter
    public final static String MESSAGE_113_HEAT = "36446850180780147143A0009F23880400581B0000000800000000000000000000000000000000000000000001081C60400001001000001F0000";

    // 4543A1 -> HKVv69
    public final static String MESSAGE_69_HKV = "37446850468519484543A1009F23274900607814000039434C3169650F3EB358D207121AD050451C4C2092C926E960F6577AD5E1C556639F16";
    // 61???? -> HKVv97
    public final static String MESSAGE_97_HKV = "";
    // 6480A0 -> HKVv100
    public final static String MESSAGE_100_HKV = "2E446850382041606480A0119F236800D016410000000000000000000000000000000000000009090F110F0C09000FDA0000";
    // 6980A0 -> HKVv105
    public final static String MESSAGE_105_HKV = "32446850591266506980A0119F23CF07E0169A01680845091A000100000200000000000000000000110B0020361D221F6E9287490000";
    // 9480A2 -> HKVv148
    public final static String MESSAGE_148_HKV = "33446850789492029480A20F9F259C01B031910201580A470A0000000000000000071F241E454E42588478775E4F2E1400000000DB0000";

    // 76F0A0 -> SDv118
    public final static String MESSAGE_118_SD = "294468507764866476F0A000DE246F2500586F2500001A000013006BA1007CB2008DC3009ED4000FE500EE0000";

    public final static int RSSI = 10;

    protected final WMBusDevice message(String messageHex) throws DecodingException {
        return message(messageHex, null);
    }

    protected final WMBusDevice message(String messageHex, WMBusAdapter adapter) throws DecodingException {
        byte[] buffer = HexUtils.hexToBytes(messageHex);
        WMBusMessage message = VirtualWMBusMessageHelper.decode(buffer, RSSI, Collections.emptyMap());

        return new WMBusDevice(message, adapter);
    }

}
