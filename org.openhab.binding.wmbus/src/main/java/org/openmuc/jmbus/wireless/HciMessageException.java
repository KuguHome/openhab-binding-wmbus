package org.openmuc.jmbus.wireless;

import java.io.IOException;

class HciMessageException extends IOException {

    private static final long serialVersionUID = -1789394121831686912L;
    private final byte[] data;

    public HciMessageException(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
