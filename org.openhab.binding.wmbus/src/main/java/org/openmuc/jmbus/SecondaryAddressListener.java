/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

import java.util.EventListener;

/**
 * Listener to get secondary address scan message e.g. for console tools and to get messages.
 */
public interface SecondaryAddressListener extends EventListener {

    /**
     * New scan message.
     * 
     * @param message
     *            messages from scan secondary address
     */
    void newScanMessage(String message);

    /**
     * New device found.
     * 
     * @param secondaryAddress
     *            secondary address of detected device
     */
    void newDeviceFound(SecondaryAddress secondaryAddress);
}
