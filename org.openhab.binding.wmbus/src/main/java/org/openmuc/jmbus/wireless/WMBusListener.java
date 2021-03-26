/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.IOException;
import java.util.EventListener;

/**
 * The wireless M-Bus event/new message listener interface.
 */
public interface WMBusListener extends EventListener {

    /**
     * Received a new wireless M-Bus message.
     * 
     * @param message
     *            the message.
     */
    void newMessage(WMBusMessage message);

    /**
     * Callback, when noisy data has been discarded.
     * 
     * @param bytes
     *            the data which has been discarded.
     */
    void discardedBytes(byte[] bytes);

    /**
     * Callback, if the connection has been interrupted.
     * 
     * @param cause
     *            the cause of the interruption.
     */
    void stoppedListening(IOException cause);
}
