package org.openmuc.jmbus.wireless;

import java.io.IOException;

class HciMessageException extends IOException {

    private final byte[] data;

    public HciMessageException(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
