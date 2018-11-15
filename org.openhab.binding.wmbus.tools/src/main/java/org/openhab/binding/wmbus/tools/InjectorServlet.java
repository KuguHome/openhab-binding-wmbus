/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingManager;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusBindingConstants;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.wireless.VirtualWMBusMessageHelper;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.*;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Very basic servlet which allows to send a test frame to deployed binding.
 *
 * @author Łuaksz Dywicki - Initial contribution.
 */
@Component
public class InjectorServlet extends HttpServlet {

    private ThingRegistry thingRegistry;
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

        if (adapterId == null || frame == null || adapterId.trim().isEmpty() || frame.trim().isEmpty()) {
            doGet(req, resp);
            return;
        }

        Optional<Thing> wmBusAdapter = adapters().stream()
            .filter(adapter -> adapterId.equals(adapter.getUID().toString()))
            .findFirst();

        if (wmBusAdapter.isPresent()) {
            inject((WMBusAdapter) wmBusAdapter.get().getHandler(), frame, req, resp);
        }

        doGet(req, resp);
    }

    private String getError(HttpServletRequest req) {
        return Optional.ofNullable(req.getAttribute("error"))
                .map(e -> (Throwable) e)
                .map(e -> render(e))
                .orElse("");
    }

    private String render(Throwable error) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
        error.printStackTrace(new PrintWriter(bos));
        return "<pre>" + error.getMessage() + "\n" + new String(bos.toByteArray()) + "</pre>";
    }

    private Collection<Thing> adapters() {
        return thingRegistry.getAll().stream()
            .filter(thing -> WMBusBindingConstants.BINDING_ID.equals(thing.getUID().getBindingId()))
            .filter(thing -> thing.getHandler() instanceof WMBusAdapter)
            .collect(Collectors.toList());
    }

    private void inject(WMBusAdapter adapter, String frame, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        byte[] bytes = HexUtils.hexToBytes(frame.replace(" ", ""));
        try {
            WMBusMessage message = VirtualWMBusMessageHelper.decode(bytes, 0, Collections.emptyMap());
            WMBusDevice device = new WMBusDevice(message, adapter);
            adapter.processMessage(device);
        } catch (DecodingException e) {
            req.setAttribute("error", e);
        }
    }

    private String getAdapters() {
        String options = adapters().stream()
                .map(adapter -> "<option value=\"" + adapter.getUID() + "\">" + adapter.getUID().getBridgeIds() + " " + adapter.getLabel() + "</option>")
                .collect(Collectors.joining());
        return options;
    }

    @Reference
    protected void setThingManager(ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }
    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
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
