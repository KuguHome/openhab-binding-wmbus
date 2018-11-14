/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.device.techem.decoder.wz;

import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.device.techem.TechemDevice;
import org.openhab.binding.wmbus.device.techem.decoder.AbstractTechemFrameDecoder;
import org.openmuc.jmbus.SecondaryAddress;

public abstract class AbstractTechemWZFrameDecoder<T extends TechemDevice> extends AbstractTechemFrameDecoder<T> {

    protected AbstractTechemWZFrameDecoder() {
        super(TechemWZFrameDecoder.DEVICE_VARIANT);
    }

    @Override
    protected T decode(WMBusDevice device, SecondaryAddress address, byte[] buffer) {
        int offset = address.asByteArray().length + 2;
        // TechemWZ_ParseID(@) ("$m[6]$m[5]$m[4]$m[3]", "$m[4]$m[3]");
        // TechemWZ_ParseSubVersion(@) "$m[7]";
        // type = TechemWZ_ParseSubType(@) "$m[8]";
        //
        // TechemWZ_ParseLastDate(@) hex("$m[12]$m[11]");
        // TechemWZ_ParseLastPeriod(@) hex("$m[14]$m[13]");
        // TechemWZ_ParseActualDate(@) hex("$m[16]$m[15]");
        // TechemWZ_ParseActualPeriod(@) hex("$m[18]$m[17]");
        //
        // TechemWZ_WMZ_Type1_ParseLastPeriod(@) hex("$m[15]$m[14]$m[13]");
        // TechemWZ_WMZ_Type1_ParseActualPeriod(@) hex("$m[19]$m[18]$m[17]");
        // TechemWZ_WMZ_Type1_ParseActualDate(@) hex("$m[21]$m[20]") hex("$m[16]") >> 3)
        //
        //
        //
        // # metertype specific adjustment
        // if ($message->{type} =~ /62|72/) {
        // $message->{lastVal} = TechemWZ_ParseLastPeriod(@m);
        // $message->{actualVal} = TechemWZ_ParseActualPeriod(@m);
        // ($message->{actual}->{year}, $message->{actual}->{month}, $message->{actual}->{day}) =
        // TechemWZ_ParseActualDate(@m);
        // ($message->{last}->{year}, $message->{last}->{month}, $message->{last}->{day}) = TechemWZ_ParseLastDate(@m);
        // $message->{lastVal} /= 10;
        // $message->{actualVal} /= 10;
        // $message->{meter} = $message->{lastVal} + $message->{actualVal};
        // }

        int coding = buffer[offset] & 0xFF;

        if (coding == 0xA2) {
            // System.out.println(parseLastDate(buffer, offset + 2));
            // System.out.println(parseValue(buffer, offset + 4, _SCALE_FACTOR_1_10th));
            // System.out.println(parseCurrentDate(buffer, offset + 6));
            // System.out.println(parseValue(buffer, offset + 8, _SCALE_FACTOR_1_10th));
            return null;
        }

        return null;
    }

}
