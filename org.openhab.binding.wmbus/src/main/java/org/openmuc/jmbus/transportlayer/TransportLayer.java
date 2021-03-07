/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.transportlayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * The MBus transport layer interface.
 */
public interface TransportLayer extends AutoCloseable {

    /**
     * Opens the transport layer. The layer needs to be opened before attempting to read a device.
     * 
     * @throws IOException
     *             if an I/O error occurs while opening.
     * 
     */
    void open() throws IOException;

    /**
     * Closes the transport layer.
     */
    @Override
    void close();

    /**
     * Get the output stream of the layer.
     * 
     * @return the output stream.
     */
    DataOutputStream getOutputStream();

    /**
     * Get the input stream of the layer.
     * 
     * @return the input stream.
     */
    DataInputStream getInputStream();

    /**
     * Check if the layer is open.
     * 
     * @return {@code true} if the layer is closed.
     */
    boolean isClosed();

    /**
     * Set the response timeout.
     * 
     * @param timeout
     *            the timeout in MILLIS.
     * @throws IOException
     *             if an I/O error occurs.
     */
    void setTimeout(int timeout) throws IOException;

    /**
     * Get the response timeout in MILLIS.
     * 
     * @return the response timeout in MILLIS.
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    int getTimeout() throws IOException;
}
