/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.transportlayer;

/**
 * Connection builder for TCP connections.<br>
 * M-Bus over TCP has the same data like M-Bus over Serial connection, only the transport layer and below differs.
 */
public abstract class TcpBuilder<T, S extends TcpBuilder<T, S>> extends Builder<T, S> {

    private String hostAddress;
    private int port;
    private int connectionTimeout = 10000; // 10 s

    /**
     * Constructor of the TCP/IP Settings Builder, for connecting M-Bus devices over TCP/IP.
     * 
     * @param hostAddress
     *            examples for IP host address port are "127.0.0.1, localhost, ..."
     * @param port
     *            the TCP port of the host
     **/
    protected TcpBuilder(String hostAddress, int port) {
        setHostAddress(hostAddress);
        setPort(port);
    }

    /**
     * Sets the TCP port of this communication
     * 
     * @param port
     *            the TCP port of the host
     * @return the builder itself
     * 
     **/
    public S setPort(int port) {
        this.port = port;
        return self();
    }

    /**
     * Sets the IP host address of the device
     * 
     * @param hostAddress
     *            examples for IP host address port are "127.0.0.1, localhost, ..."
     * @return the builder itself
     */
    public S setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
        return self();
    }

    /**
     * Sets the TCP connection timeout.
     * 
     * @param connectionTimeout
     *            the TCP connection timeout
     * @return the builder itself
     * 
     **/
    public S setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return self();
    }

    @Override
    protected TransportLayer buildTransportLayer() {
        return new TcpLayer(hostAddress, port, connectionTimeout, getTimeout());
    }
}
