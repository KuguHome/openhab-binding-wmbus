/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.wmbus.device;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.io.transport.mbus.wireless.KeyStorage;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UnknownMeter} class defines unknown abstract Meter device
 *
 * @author Roman Malyugin - Initial contribution
 */

@Component(service = { UnknownMeter.class })
public class UnknownMeter extends Meter {

    public static final Logger logger = LoggerFactory.getLogger(UnknownMeter.class);

    @Activate
    protected void activate(Map<String, String> properties) {
    }

    @Deactivate
    protected void deactivate() {
    }

    public static class UnknownWMBusDeviceHandler extends WMBusDeviceHandler {

        public UnknownWMBusDeviceHandler(Thing thing, KeyStorage keyStorage) {
            super(thing, keyStorage);
        }

        @Override
        public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
            logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
                    + command.toString());
            if (command == RefreshType.REFRESH) {
                logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
                State newState = UnDefType.NULL;
                if (wmbusDevice != null) {
                    logger.trace("handleCommand(): (3/5) deviceMessage != null");
                    logger.trace("handleCommand(): (4/5): got channel id: " + channelUID.getId());
                    logger.trace("handleCommand(): (5/5) assigning new state to channel '"
                            + channelUID.getId().toString() + "': " + newState.toString());
                    updateState(channelUID.getId(), newState);

                }

            }
        }
    }
}
