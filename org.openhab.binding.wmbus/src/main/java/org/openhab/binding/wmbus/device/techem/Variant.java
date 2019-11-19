package org.openhab.binding.wmbus.device.techem;

import org.openmuc.jmbus.DeviceType;

public class Variant {

    public final int version;
    public final int reportedType;
    public final int desiredType;
    public final int matchingType; // since all unknown device types are converted to RESERVED/255 by jMBus

    public Variant(int version, DeviceType reportedType, DeviceType desiredType) {
        this(version, reportedType.getId(), desiredType);
    }

    public Variant(int version, int reportedType, DeviceType desiredType) {
        this(version, reportedType, desiredType, DeviceType.getInstance(reportedType));
    }

    public Variant(int version, int reportedType, DeviceType desiredType, DeviceType matchingType) {
        this.version = version;
        this.reportedType = reportedType;
        this.desiredType = desiredType.getId();
        this.matchingType = matchingType.getId();
    }

    public String getRawType() {
        return "68" + TechemBindingConstants.MANUFACTURER_ID + version + "" + reportedType;
    }

    public String getTechemType() {
        return getRawType() + desiredType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + desiredType;
        result = prime * result + reportedType;
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Variant other = (Variant) obj;
        if (desiredType != other.desiredType) {
            return false;
        }
        if (reportedType != other.reportedType) {
            return false;
        }
        if (version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getRawType() + "->" + getTechemType();
    }
}