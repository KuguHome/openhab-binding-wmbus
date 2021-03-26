/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.openmuc.jmbus;

import java.util.HashMap;
import java.util.Map;

/**
 * The device type that is part of the data header of a Variable Data Response.
 */
public enum DeviceType {
    OTHER(0x00),
    OIL_METER(0x01),
    ELECTRICITY_METER(0x02),
    GAS_METER(0x03),
    HEAT_METER(0x04),
    STEAM_METER(0x05),
    WARM_WATER_METER(0x06),
    WATER_METER(0x07),
    HEAT_COST_ALLOCATOR(0x08),
    COMPRESSED_AIR(0x09),
    COOLING_METER_OUTLET(0x0a),
    COOLING_METER_INLET(0x0b),
    HEAT_METER_INLET(0x0c),
    HEAT_COOLING_METER(0x0d),
    BUS_SYSTEM_COMPONENT(0x0e),
    UNKNOWN(0x0f),
    RESERVED_FOR_METER_16(0x10),
    RESERVED_FOR_METER_17(0x11),
    RESERVED_FOR_METER_18(0x12),
    RESERVED_FOR_METER_19(0x13),
    CALORIFIC_VALUE(0x14),
    HOT_WATER_METER(0x15),
    COLD_WATER_METER(0x16),
    DUAL_REGISTER_WATER_METER(0x17),
    PRESSURE_METER(0x18),
    AD_CONVERTER(0x19),
    SMOKE_DETECTOR(0x1a),
    ROOM_SENSOR_TEMP_HUM(0x1b),
    GAS_DETECTOR(0x1c),
    RESERVED_FOR_SENSOR_0X1D(0x1d),
    RESERVED_FOR_SENSOR_0X1E(0x1e),
    RESERVED_FOR_SENSOR_0X1F(0x1f),
    BREAKER_ELEC(0x20),
    VALVE_GAS_OR_WATER(0x21),
    RESERVED_FOR_SWITCHING_DEVICE_0X22(0x22),
    RESERVED_FOR_SWITCHING_DEVICE_0X23(0x23),
    RESERVED_FOR_SWITCHING_DEVICE_0X24(0x24),
    CUSTOMER_UNIT_DISPLAY_DEVICE(0x25),
    RESERVED_FOR_CUSTOMER_UNIT_0X26(0x26),
    RESERVED_FOR_CUSTOMER_UNIT_0X27(0x27),
    WASTE_WATER_METER(0x28),
    GARBAGE(0x29),
    RESERVED_FOR_CO2(0x2a),
    RESERVED_FOR_ENV_METER_0X2B(0x2b),
    RESERVED_FOR_ENV_METER_0X2C(0x2c),
    RESERVED_FOR_ENV_METER_0X2D(0x2d),
    RESERVED_FOR_ENV_METER_0X2E(0x2e),
    RESERVED_FOR_ENV_METER_0X2F(0x2f),
    RESERVED_FOR_SYSTEM_DEVICES_0X30(0x30),
    COM_CONTROLLER(0x31),
    UNIDIRECTION_REPEATER(0x32),
    BIDIRECTION_REPEATER(0x33),
    RESERVED_FOR_SYSTEM_DEVICES_0X34(0x34),
    RESERVED_FOR_SYSTEM_DEVICES_0X35(0x35),
    RADIO_CONVERTER_SYSTEM_SIDE(0x36),
    RADIO_CONVERTER_METER_SIDE(0x37),
    RESERVED_FOR_SYSTEM_DEVICES_0X38(0x38),
    RESERVED_FOR_SYSTEM_DEVICES_0X39(0x39),
    RESERVED_FOR_SYSTEM_DEVICES_0X3A(0x3a),
    RESERVED_FOR_SYSTEM_DEVICES_0X3B(0x3b),
    RESERVED_FOR_SYSTEM_DEVICES_0X3C(0x3c),
    RESERVED_FOR_SYSTEM_DEVICES_0X3D(0x3d),
    RESERVED_FOR_SYSTEM_DEVICES_0X3E(0x3e),
    RESERVED_FOR_SYSTEM_DEVICES_0X3F(0x3f),
    RESERVED(0xff);

    private final int id;

    private static final Map<Integer, DeviceType> idMap = new HashMap<>();

    static {
        for (DeviceType enumInstance : DeviceType.values()) {
            if (idMap.put(enumInstance.getId(), enumInstance) != null) {
                throw new IllegalArgumentException("duplicate ID: " + enumInstance.getId());
            }
        }
    }

    private DeviceType(int id) {
        this.id = id;
    }

    /**
     * Returns the ID of this DeviceType.
     * 
     * @return the ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the DeviceType that corresponds to the given ID. Returns DeviceType.RESERVED if no DeviceType with the
     * given ID exists.
     * 
     * @param id
     *            the ID
     * @return the DeviceType that corresponds to the given ID
     */
    public static DeviceType getInstance(int id) {
        DeviceType enumInstance = idMap.get(id);
        if (enumInstance == null) {
            enumInstance = DeviceType.RESERVED;
        }
        return enumInstance;
    }
}
