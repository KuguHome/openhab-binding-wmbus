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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.tools.processor.*;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.util.HexUtils;
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
        String form = new String(getClass().getResourceAsStream("/form.html").readAllBytes(), StandardCharsets.UTF_8);

        form = form.replace("__adapters__", getAdapters());
        form = form.replace("__error__", getError(req));

        resp.getOutputStream().write(form.getBytes(StandardCharsets.UTF_8));
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

        int rssiIndex = Optional.ofNullable(req.getParameter("rssiIndex")).map(Integer::parseInt).orElse(0);
        int rssiValue = Optional.ofNullable(req.getParameter("rssiValue")).map(Integer::parseInt).orElse(100);

        int skipBytes = Optional.ofNullable(req.getParameter("skipBytes")).map(Integer::parseInt).orElse(0);
        boolean stripCRC = Optional.ofNullable(req.getParameter("stripCRC")).map(value -> Boolean.TRUE).orElse(false);
        boolean calculateLength = Optional.ofNullable(req.getParameter("calculateLength")).map(value -> Boolean.TRUE)
                .orElse(false);
        boolean recalculateLength = Optional.ofNullable(req.getParameter("recalculateLength"))
                .map(value -> Boolean.TRUE).orElse(false);

        List<Processor<String>> processors = new ArrayList<>();
        processors.add(new RssiProcessor(rssiIndex, rssiValue));
        if (skipBytes > 0) {
            processors.add(new SkipProcessor(skipBytes));
        }
        if (stripCRC) {
            processors.add(new SkipCrcProcessor());
        }
        if (calculateLength) {
            processors.add(new CalculateLength());
        }
        if (recalculateLength) {
            processors.add(new RecalculateLength());
        }

        String[] frameArray = frames.split("\n");
        try {
            for (String frame : frameArray) {
                frame = frame.trim().replace(" ", "");

                Map<String, Object> context = new HashMap<>();
                frame = Processors.process(frame, context, processors);
                int rssi = (int) context.getOrDefault(Processor.RSSI, rssiValue);

                byte[] bytes = HexUtils.hexToBytes(frame);
                WMBusMessage message = VirtualWMBusMessageHelper.decode(bytes, rssi, Collections.emptyMap());
                if (aesKey != null && !aesKey.trim().isEmpty()) {
                    byte[] key = HexUtils.hexToBytes(aesKey);
                    message = VirtualWMBusMessageHelper.decode(bytes, rssi,
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
