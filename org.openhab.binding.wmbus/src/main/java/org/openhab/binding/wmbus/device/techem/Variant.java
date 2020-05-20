package org.openhab.binding.wmbus.device.techem;

import java.util.Objects;

import org.openmuc.jmbus.DeviceType;

public class Variant {

    public final int version;
    public final int reportedType;
    public final int coding;
    public final DeviceType desiredType;
    public final int matchingType; // since all unknown device types are converted to RESERVED/255 by jMBus

    public Variant(int version, int reportedType, int coding, DeviceType desiredType) {
        this(version, reportedType, coding, desiredType, DeviceType.getInstance(reportedType));
    }

    public Variant(int version, int reportedType, int coding, DeviceType desiredType, DeviceType matchingType) {
        this.version = version;
        this.reportedType = reportedType;
        this.coding = coding;
        this.desiredType = desiredType;
        this.matchingType = matchingType.getId();
    }

    public int getVersion() {
        return version;
    }

    public String getRawType() {
        return "68" + TechemBindingConstants.MANUFACTURER_ID + version + "" + reportedType;
    }

    public int getDesiredType() {
        return desiredType.getId();
    }

    public DeviceType getDesiredWMBusType() {
        return desiredType;
    }

    public int getCoding() {
        return coding;
    }

    public String getTechemType() {
        return getRawType() + desiredType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Variant)) {
            return false;
        }
        Variant variant = (Variant) o;
        return version == variant.version && reportedType == variant.reportedType && coding == variant.coding
                && desiredType == variant.desiredType && matchingType == variant.matchingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, reportedType, coding, desiredType, matchingType);
    }

    @Override
    public String toString() {
        return getRawType() + "->" + getTechemType();
    }
}