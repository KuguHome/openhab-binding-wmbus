/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.IOException;
import java.text.MessageFormat;

import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.transportlayer.SerialBuilder;
import org.openmuc.jmbus.transportlayer.TcpBuilder;
import org.openmuc.jmbus.transportlayer.TransportLayer;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.StopBits;

/**
 * A Wireless Mbus Connection.
 * 
 * @see #addKey(SecondaryAddress, byte[])
 */
public interface WMBusConnection extends AutoCloseable {

    /**
     * Closes the service access point.
     */
    @Override
    void close() throws IOException;

    /**
     * Stores a pair of secondary address and cryptographic key. The stored keys are automatically used to decrypt
     * messages when a wireless M-Bus message is been decoded.
     * 
     * @param address
     *            the secondary address.
     * @param key
     *            the cryptographic key.
     * 
     * @see #removeKey(SecondaryAddress)
     */
    void addKey(SecondaryAddress address, byte[] key);

    /**
     * Removes the stored key for the given secondary address.
     * 
     * @param address
     *            the secondary address for which to remove the stored key.
     * 
     * @see #addKey(SecondaryAddress, byte[])
     */
    void removeKey(SecondaryAddress address);

    class WMBusSerialBuilder extends SerialBuilder<WMBusConnection, WMBusSerialBuilder> {

        private final Builder builder;

        public WMBusSerialBuilder(WMBusManufacturer wmBusManufacturer, WMBusListener listener, String serialPortName) {
            super(serialPortName);
            builder = new Builder(wmBusManufacturer, listener);

            switch (wmBusManufacturer) {
                case RADIO_CRAFTS:
                    setBaudrate(19200);
                    break;
                case AMBER:
                    setBaudrate(9600);
                    break;
                case IMST:
                    setBaudrate(57600);
                    break;
                case CUL:
                    setBaudrate(38400);
                    break;
                default:
                    // should not occur
                    throw new RuntimeException(
                            MessageFormat.format("Error unknown manufacturer {0}.", wmBusManufacturer));
            }
            setStopBits(StopBits.STOPBITS_1).setParity(Parity.NONE).setDataBits(DataBits.DATABITS_8);
        }

        public WMBusSerialBuilder setMode(WMBusMode mode) {
            builder.mode = mode;
            return self();
        }

        public WMBusSerialBuilder setWmBusManufacturer(WMBusManufacturer wmBusManufacturer) {
            builder.wmBusManufacturer = wmBusManufacturer;
            return self();
        }

        public WMBusSerialBuilder setListener(WMBusListener connectionListener) {
            builder.listener = connectionListener;
            return self();
        }

        @Override
        public WMBusConnection build() throws IOException {
            return builder.build(buildTransportLayer());
        }
    }

    class WMBusTcpBuilder extends TcpBuilder<WMBusConnection, WMBusTcpBuilder> {

        private final Builder builder;

        public WMBusTcpBuilder(WMBusManufacturer wmBusManufacturer, WMBusListener listener, String hostAddress,
                int port) {
            super(hostAddress, port);
            builder = new Builder(wmBusManufacturer, listener);
        }

        public WMBusTcpBuilder setMode(WMBusMode mode) {
            builder.mode = mode;
            return self();
        }

        public WMBusTcpBuilder setWmBusManufacturer(WMBusManufacturer wmBusManufacturer) {
            builder.wmBusManufacturer = wmBusManufacturer;
            return self();
        }

        public WMBusTcpBuilder setListener(WMBusListener connectionListener) {
            builder.listener = connectionListener;
            return self();
        }

        @Override
        public WMBusConnection build() throws IOException {
            return builder.build(buildTransportLayer());
        }
    }

    class Builder {

        private WMBusManufacturer wmBusManufacturer;
        private WMBusMode mode;
        private WMBusListener listener;

        Builder(WMBusManufacturer wmBusManufacturer, WMBusListener listener) {
            this.listener = listener;
            this.wmBusManufacturer = wmBusManufacturer;
            this.mode = WMBusMode.T;
        }

        WMBusConnection build(TransportLayer transportLayer) throws IOException {
            AbstractWMBusConnection wmBusConnection;
            switch (this.wmBusManufacturer) {
                case AMBER:
                    wmBusConnection = new WMBusConnectionAmber(this.mode, this.listener, transportLayer);
                    break;
                case IMST:
                    wmBusConnection = new WMBusConnectionImst(this.mode, this.listener, transportLayer);
                    break;
                case RADIO_CRAFTS:
                    wmBusConnection = new WMBusConnectionRadioCrafts(this.mode, this.listener, transportLayer);
                    break;
                case CUL:
                    wmBusConnection = new WMBusConnectionCUL(this.mode, this.listener, transportLayer);
                    break;
                default:
                    // should not occur.
                    throw new RuntimeException("Unknown Manufacturer.");
            }

            wmBusConnection.open();
            return wmBusConnection;
        }
    }

    public enum WMBusManufacturer {
        AMBER,
        IMST,
        RADIO_CRAFTS,
        CUL
    }
}
