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
package org.openhab.binding.wmbus.internal;

import java.util.Map;

import org.openhab.binding.wmbus.BindingConfiguration;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of configuration which rely on OSGi configuration admin and keep updating of time to live once its
 * set.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
@Component(service = BindingConfiguration.class, configurationPid = "binding.wmbus")
public class DynamicBindingConfiguration implements BindingConfiguration {

    private final Logger logger = LoggerFactory.getLogger(DynamicBindingConfiguration.class);
    private Long timeToLive = WMBusBindingConstants.DEFAULT_TIME_TO_LIVE;
    private Boolean includeBridgeUID = true;

    @Activate
    public void activate(ComponentContext context) {
        setTimeToLive(context.getProperties().get(WMBusBindingConstants.CONFKEY_BINDING_TIME_TO_LIVE));
        setIncludeBridgeUID(context.getProperties().get(WMBusBindingConstants.CONFKEY_BINDING_INCLUDE_BRIDGE_UID));
    }

    private void setTimeToLive(Object value) {
        if (value == null) {
            logger.debug("Setting up time to live to default value");
            this.timeToLive = WMBusBindingConstants.DEFAULT_TIME_TO_LIVE;
            return;
        }

        logger.debug("Setting up time to live to new value {}", value);
        if (value instanceof Long) {
            this.timeToLive = (Long) value;
        }

        if (value instanceof String) {
            this.timeToLive = Long.parseLong((String) value);
        }
    }

    @Override
    public Long getTimeToLive() {
        return timeToLive;
    }

    private void setIncludeBridgeUID(Object value) {
        if (value == null) {
            logger.debug("Setting up includeBridgeUID to default value");
            this.includeBridgeUID = true;
            return;
        }

        logger.debug("Setting up includeBridgeUID to new value {}", value);
        if (value instanceof Boolean) {
            this.includeBridgeUID = (Boolean) value;
        }

        if (value instanceof String) {
            this.includeBridgeUID = Boolean.parseBoolean((String) value);
        }
    }

    @Override
    public Boolean getIncludeBridgeUID() {
        return includeBridgeUID;
    }

    @Modified
    void updated(Map<String, Object> configuration) {
        setTimeToLive(configuration.get(WMBusBindingConstants.CONFKEY_BINDING_TIME_TO_LIVE));
        setIncludeBridgeUID(configuration.get(WMBusBindingConstants.CONFKEY_BINDING_INCLUDE_BRIDGE_UID));
    }
}
