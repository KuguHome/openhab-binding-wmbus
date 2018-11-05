/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.wmbus.device;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.wmbus.handler.WMBusDeviceHandler;
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

@Component(service = { UnknownMeter.class }, properties = "OSGI-INF/unknown.properties")
public class UnknownMeter extends Meter {

    public static final Logger logger = LoggerFactory.getLogger(UnknownMeter.class);

    @Activate
    protected void activate(Map<String, String> properties) {
    }

    @Deactivate
    protected void deactivate() {
    }

    public static class UnknownWMBusDeviceHandler extends WMBusDeviceHandler {

        public UnknownWMBusDeviceHandler(Thing thing) {
            super(thing);
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
