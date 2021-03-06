/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.transportlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;

class SerialLayer implements TransportLayer {
    private final SerialPortBuilder serialPortBuilder;
    private final int timeout;

    private DataOutputStream os;
    private DataInputStream is;
    private SerialPort serialPort;

    public SerialLayer(int timeout, SerialPortBuilder serialPortBuilder) {
        this.serialPortBuilder = serialPortBuilder;
        this.timeout = timeout;
    }

    @Override
    public void open() throws IOException {
        serialPort = serialPortBuilder.build();
        serialPort.setSerialPortTimeout(timeout);

        os = new DataOutputStream(serialPort.getOutputStream());
        is = new DataInputStream(serialPort.getInputStream());
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
    public void close() {
        if (serialPort == null || serialPort.isClosed()) {
            return;
        }
        try {
            serialPort.close();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public boolean isClosed() {
        return serialPort == null;
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        serialPort.setSerialPortTimeout(timeout);
    }

    @Override
    public int getTimeout() {
        return serialPort.getSerialPortTimeout();
    }
}
