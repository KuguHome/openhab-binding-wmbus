/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.transportlayer;

import java.io.IOException;

/**
 * A abstract builder to an active M-Bus connection.
 *
 * @param <C>
 *            the resulting connection after calling {@link #build()}.
 * @param <B>
 *            the inheriting builder type.
 */
public abstract class Builder<C, B extends Builder<C, B>> {

    private int timeout;

    protected Builder() {
        this.timeout = 500;
    }

    /**
     * Set the connection timeout. The timeout must be &ge; zero. A timeout of zero is interpreted as infinite timeout,
     * 
     * @param timeout
     *            a timeout in milliseconds.
     * @return the builder itself.
     */
    public B setTimeout(int timeout) {
        this.timeout = timeout;
        return self();
    }

    int getTimeout() {
        return timeout;
    }

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    /**
     * Build the TransportLayer with the given settings.
     * 
     * @return TransportLayer to connect to the M-Bus device
     * @throws IOException
     *             if an I/O exception occurred.
     */
    protected abstract TransportLayer buildTransportLayer() throws IOException;

    /**
     * This return an active M-Bus connection.
     * 
     * @return a new active M-Bus connection.
     * @throws IOException
     *             if an I/O exception occurred opening the connection to the remote device.
     */
    public abstract C build() throws IOException;
}
