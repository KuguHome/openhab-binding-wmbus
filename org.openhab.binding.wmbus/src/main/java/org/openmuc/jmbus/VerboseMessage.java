/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

/**
 * This class represents a verbose message. This may be useful to debug a connection.
 * 
 * @see VerboseMessageListener
 */
public class VerboseMessage {

    private final MessageDirection messageDirection;
    private final byte[] message;

    VerboseMessage(MessageDirection messageDirection, byte[] message) {
        this.messageDirection = messageDirection;
        this.message = message;
    }

    /**
     * Get the message data.
     * 
     * @return an octet string.
     */
    public byte[] getMessage() {
        return message;
    }

    /**
     * Get the direction of the message.
     * 
     * @return the message direction.
     */
    public MessageDirection getMessageDirection() {
        return messageDirection;
    }

    /**
     * The direction of message.
     */
    public enum MessageDirection {
        SEND,
        RECEIVE;
    }
}
