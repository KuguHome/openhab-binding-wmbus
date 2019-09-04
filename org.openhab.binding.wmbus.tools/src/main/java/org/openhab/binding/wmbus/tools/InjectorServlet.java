/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very basic servlet which allows to send a test frame to deployed binding.
 *
 * @author Łuaksz Dywicki - Initial contribution.
 */
@Component
public class InjectorServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(InjectorServlet.class);

    private ThingRegistry thingregistry;
    private HttpService httpService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String form = IOUtils.toString(getClass().getResourceAsStream("/form.html"));

        form = form.replace("__adapters__", getAdapters());
        form = form.replace("__error__", getError(req));

        IOUtils.write(form, resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String adapterId = req.getParameter("adapter");
        String frame = req.getParameter("frame");
        String aesKey = req.getParameter("aesKey");

        if (adapterId == null || frame == null || adapterId.trim().isEmpty() || frame.trim().isEmpty()) {
            doGet(req, resp);
            return;
        }

        Optional<Thing> wmBusAdapter = adapters().stream()
                .filter(adapter -> adapterId.equals(adapter.getUID().toString())).findFirst();

        if (wmBusAdapter.isPresent()) {
            inject((WMBusAdapter) wmBusAdapter.get().getHandler(), frame, req, resp, aesKey);
        }

        doGet(req, resp);
    }

    private String getError(HttpServletRequest req) {
        return Optional.ofNullable(req.getAttribute("error")).map(e -> (Throwable) e).map(e -> render(e)).orElse("");
    }

    private String render(Throwable error) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
        error.printStackTrace(new PrintWriter(bos));
        return "<pre>" + error.getMessage() + "\n" + new String(bos.toByteArray()) + "</pre>";
    }

    private Collection<Thing> adapters() {
        return thingregistry.getAll().stream()
                .filter(thing -> WMBusBindingConstants.BINDING_ID.equals(thing.getUID().getBindingId()))
                .filter(thing -> thing.getHandler() instanceof WMBusAdapter).collect(Collectors.toList());
    }

    private void inject(WMBusAdapter adapter, String frames, HttpServletRequest req, HttpServletResponse resp,
            String aesKey) throws IOException {
        int skipBytes = Optional.ofNullable(req.getParameter("skipBytes")).map(Integer::parseInt).orElse(0);
        boolean stripCRC = Optional.ofNullable(req.getParameter("stripCRC")).map(value -> Boolean.TRUE).orElse(false);
        boolean calculateLength = Optional.ofNullable(req.getParameter("calculateLength")).map(value -> Boolean.TRUE)
                .orElse(false);

        String[] frameArray = frames.split("\n");
        try {
            for (String frame : frameArray) {
                frame = frame.trim().replace(" ", "");

                if (skipBytes > 0) {
                    // one byte is 2 characters in hex representation
                    frame = frame.substring(skipBytes * 2);
                }

                if (stripCRC) {
                    String strippedframe = "";
                    strippedframe += frame.substring(Math.min(0, frame.length()), Math.min(18, frame.length()));
                    strippedframe += frame.substring(Math.min(22, frame.length()), Math.min(54, frame.length()));
                    strippedframe += frame.substring(Math.min(58, frame.length()), Math.min(90, frame.length()));
                    // String strippedframe = frame.substring(0, 18) + frame.substring(22, frame.length());
                    /*
                     * TODO: general implementation
                     *
                     *
                     * Integer position = 2;
                     * while (position < frame.length()) {
                     * strippedframe += frame.substring(position, position+16);
                     * position += 16 ;
                     * }
                     */
                    frame = strippedframe;
                }

                if (calculateLength) {
                    // remember of hex notation which doubles length
                    Integer len = frame.length() / 2;
                    frame = Integer.toHexString(len) + frame;
                }
                // logger.debug("Seen frame:");
                // logger.debug(frame);

                byte[] bytes = HexUtils.hexToBytes(frame);
                WMBusMessage message = VirtualWMBusMessageHelper.decode(bytes, 0, Collections.emptyMap());
                if (aesKey != null && !aesKey.trim().isEmpty()) {
                    byte[] key = HexUtils.hexToBytes(aesKey);
                    message = VirtualWMBusMessageHelper.decode(bytes, 0,
                            Collections.singletonMap(message.getSecondaryAddress(), key));
                }
                WMBusDevice device = new WMBusDevice(message, adapter);
                adapter.processMessage(device);
            }
        } catch (DecodingException e) {
            req.setAttribute("error", e);
        }
    }

    private String getAdapters() {
        String options = adapters().stream().map(adapter -> "<option value=\"" + adapter.getUID() + "\">"
                + adapter.getUID() + " " + adapter.getLabel() + "</option>").collect(Collectors.joining());
        return options;
    }

    @Reference
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        this.thingregistry = thingRegistry;
    }

    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingregistry = null;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Activate
    public void activate() throws ServletException, NamespaceException {
        httpService.registerServlet("/wmbus", this, new Hashtable(), null);
    }

    @Deactivate
    public void deactivate() {
        httpService.unregister("/wmbus");
    }

}
