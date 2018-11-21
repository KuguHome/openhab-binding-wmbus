package org.openhab.binding.wmbus.device.techem;

import org.openmuc.jmbus.DeviceType;

public class Variant {

    public final int version;
    public final int type;
    public final int desiredType;

    public Variant(int version, DeviceType type, DeviceType desiredType) {
        this(version, type.getId(), desiredType.getId());
    }

    public Variant(int version, int type, DeviceType desiredType) {
        this(version, type, desiredType.getId());
    }

    public Variant(int version, int type, int desiredType) {
        this.version = version;
        this.type = type;
        this.desiredType = desiredType;
    }

    public String getRawType() {
        return "68" + TechemBindingConstants.MANUFACTURER_ID + version + "" + type;
    }

    public String getTechemType() {
        return getRawType() + desiredType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + desiredType;
        result = prime * result + type;
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
        if (type != other.type) {
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