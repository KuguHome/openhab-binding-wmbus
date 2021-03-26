/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class MessageReceiver implements Runnable {

    private final ExecutorService executor;
    private final WMBusListener listener;

    public MessageReceiver(WMBusListener listener) {
        this.listener = listener;
        this.executor = Executors.newSingleThreadExecutor();
    }

    protected void shutdown() {
        this.executor.shutdown();
    }

    protected void notifyStoppedListening(final IOException ioException) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listener.stoppedListening(ioException);
            }
        });
    }

    protected void notifyNewMessage(final WMBusMessage wmBusMessage) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listener.newMessage(wmBusMessage);
            }
        });
    }

    protected void notifyDiscarded(final byte[] discardedBytes) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                listener.discardedBytes(discardedBytes);
            }
        });
    }
}
