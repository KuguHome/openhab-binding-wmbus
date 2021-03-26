/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus;

/**
 * Signals that a M-Bus message could not be decoded.
 */
public class DecodingException extends Exception {

    private static final long serialVersionUID = 1735527302166708223L;

    public DecodingException(String msg) {
        super(msg);
    }

    public DecodingException(Throwable cause) {
        super(cause);
    }

    public DecodingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
