package org.openhab.binding.wmbus.device.eielectronics;

import java.util.BitSet;
import org.openhab.binding.wmbus.device.techem.decoder.Buffer;

public class EieHeadStatusDataParser {

    private final BitSet flags;

    EieHeadStatusDataParser(byte[] buffer) {
        this(new Buffer(buffer));
    }

    EieHeadStatusDataParser(Buffer buffer) {
        flags = BitSet.valueOf(buffer.readBytes(4));
    }

    // bit 4...0
    public int getDustLevel() {
        return convert(flags.get(0, 4));
    }

    // bit 5
    public boolean isSounderFault() {
        return flags.get(5);
    }

    // bit 6
    public boolean isAlarmRemoved() {
        return flags.get(6);
    }

    // bit 7
    public boolean isEOLReached() {
        return flags.get(7);
    }

    //  bit 11..8
    public int getBatteryVoltageLevel() {
        return convert(flags.get(8, 11));
    }

    // bit 12
    public boolean isLowBattery() {
        return flags.get(12);
    }

    // bit 13
    public boolean isAlarmSensorFault() {
        return flags.get(13);
    }

    // bit 14
    public boolean isObstacleDetectionFault() {
        return flags.get(14);
    }

    // bit 15
    public boolean isEOL12Months() {
        return flags.get(15);
    }

    // bit 16
    public boolean isSEODSInstallationComplete() {
        return flags.get(16);
    }

    // bit 17
    public boolean isEnvironmentChanged() {
        return flags.get(17);
    }

    // bit 18
    public boolean isCommunicationToHeadFault() {
        return flags.get(18);
    }

    // bit 19
    public boolean isObstacleDetectionInterferenced() {
        return flags.get(19);
    }

    // bit 22...20  bit 23-Future use
    public String getDistance() {
        short value = convert(flags.get(20, 22));
        switch (value) {
            case 0:
                return "Unknown as Installation Survey not completed";
            case 1:
                return "No Obstacle detected within full range at last installation survey";
            case 2:
                return "0.450-0.600m";
            case 3:
                return "0.375-0.525m";
            case 4:
                return "0.325-0.475m";
            case 5:
                return "0.275-0.400m";
            case 6:
                return "0.200-0.325m";
            case 7:
                return "0.000-0.250m";
        }
        return "Unknown (" + value + "/0x" + Integer.toHexString(value) + ")";
    }

    // bit 24
    public boolean isObstacleDetected() {
        return flags.get(24);
    }

    // bit 25
    public boolean isSmokeAlarmCoveringDetected() {
        return flags.get(25);
    }

    // bit  31..26 for future use - reserved

    // helper method to turn passed bitset back into an number.
    private static short convert(BitSet bits) {
        short value = 0;
        for (int i = 0; i < bits.length(); ++i) {
            value += bits.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

}
