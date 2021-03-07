/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.transportlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.MessageFormat;

class TcpLayer implements TransportLayer {
    private final String hostAddress;
    private final int port;
    private final int timeout;
    private final int connectionTimeout;

    private Socket client;
    private DataOutputStream os;
    private DataInputStream is;

    TcpLayer(String hostAddress, int port, int connectionTimeout, int timeout) {
        this.hostAddress = hostAddress;
        this.port = port;
        this.timeout = timeout;
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public void open() throws IOException {
        InetAddress hostname = InetAddress.getByName(hostAddress);

        try {
            SocketAddress socketAddress = new InetSocketAddress(hostname, port);
            this.client = new Socket();
            this.client.connect(socketAddress, connectionTimeout);
            this.client.setSoTimeout(timeout);
        } catch (IOException e) {
            String msg = MessageFormat.format("Connecting to {0}:{1} failed.", hostname, port);
            throw new IOException(msg, e);
        }

        initialiseIOStreams();
    }

    private void initialiseIOStreams() throws IOException {
        try {
            this.os = new DataOutputStream(client.getOutputStream());
            this.is = new DataInputStream(client.getInputStream());
        } catch (IOException e) {
            close();
            throw new IOException("Error getting output or input stream from TCP connection.", e);
        }
    }

    @Override
    public void close() {
        if (client == null || client.isClosed()) {
            return;
        }
        try {
            client.close();
        } catch (IOException e) {
            // ignore this here
        }
        client = null;
    }

    @Override
    public DataOutputStream getOutputStream() {
        return os;
    }

    @Override
    public DataInputStream getInputStream() {
        return is;
    }

    @Override
    public boolean isClosed() {
        return client.isClosed();
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        client.setSoTimeout(timeout);
    }

    @Override
    public int getTimeout() throws IOException {
        return client.getSoTimeout();
    }
}
