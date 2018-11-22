package org.openhab.binding.wmbus.device.techem;

import org.openmuc.jmbus.DeviceType;

public class Variant {

    public final int version;
    public final int type;
    public final DeviceType reportedType;
    public final int desiredType;

    public Variant(int version, DeviceType type, DeviceType desiredType) {
        this(version, type.getId(), type, desiredType.getId());
    }

    public Variant(int version, int type, DeviceType desiredType) {
        this(version, type, DeviceType.getInstance(type), desiredType.getId());
    }

    public Variant(int version, int type, DeviceType reportedType, DeviceType desiredType) {
        this(version, type, reportedType, desiredType.getId());
    }

    public Variant(int version, int type, DeviceType reportedType, int desiredType) {
        this.version = version;
        this.type = type;
        this.reportedType = reportedType;
        this.desiredType = desiredType;
    }

    public String getRawType() {
        return "68" + TechemBindingConstants.MANUFACTURER_ID + version + "" + reportedType.getId();
    }

    public String getTechemType() {
        return getRawType() + desiredType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + desiredType;
        result = prime * result + reportedType.hashCode();
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