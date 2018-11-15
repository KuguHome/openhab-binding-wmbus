/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wmbus.tools;

import com.google.common.collect.EvictingQueue;
import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.wmbus.WMBusDevice;
import org.openhab.binding.wmbus.handler.WMBusAdapter;
import org.openhab.binding.wmbus.handler.WMBusMessageListener;
import org.openmuc.jmbus.SecondaryAddress;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Servlet which collects and displays all frames which been exchanged singe last start.
 *
 * This tool is very basic (and memory hungry) frame storage.
 *
 * @author Łuaksz Dywicki - Initial contribution.
 */
@Component
public class CollectorServlet extends HttpServlet implements WMBusMessageListener {

    private HttpService httpService;

    // keep insertion order
    private Map<SecondaryAddress, EvictingQueue<Entry>> entries = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String table = IOUtils.toString(getClass().getResourceAsStream("/table.html"));

        table = table.replace("__rows__", getRows());


        Optional<String> frameCount = entries.values().stream().map(Queue::size).reduce(Integer::sum).map(Object::toString);
        table = table.replace("__devices__", "" + entries.size());
        table = table.replace("__frames__", frameCount.orElse("0"));

        IOUtils.write(table, resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    private String getRows() throws IOException {
        String rows = "";
        String template = IOUtils.toString(getClass().getResourceAsStream("/row.html"));

        Set<SecondaryAddress> addresses = entries.keySet();
        for (SecondaryAddress address : addresses) {
            String row = template.replace("__ID__", address.getDeviceId().toString());
            row = row.replace("__Manufacturer__", address.getManufacturerId());
            row = row.replace("__Type__", address.getDeviceType().name());
            row = row.replace("__Version__", "" + address.getVersion());
            row = row.replace("__Frame__", getFrames(entries.get(address)));

            rows += row;
        }

        return rows;
    }

    private String getFrames(EvictingQueue<Entry> entries) {
        String list = "<ul>";

        for (Entry entry : entries) {
            LocalDateTime dateTime = Instant.ofEpochMilli(entry.time).atZone(ZoneId.systemDefault()).toLocalDateTime();
            list += "<li>" + dateTime + "<br />" + HexUtils.bytesToHex(entry.frame) + "</li>";
        }

        return list + "</ul>";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    @Activate
    public void activate() throws ServletException, NamespaceException {
        httpService.registerServlet("/wmbus/collector", this, new Hashtable(), null);
    }

    @Deactivate
    public void deactivate() {
        httpService.unregister("/wmbus/collector");
    }

    @Override
    public void onNewWMBusDevice(WMBusAdapter wmBusAdapter, WMBusDevice wmBusDevice) {
        push(wmBusDevice);
    }

    @Override
    public void onChangedWMBusDevice(WMBusAdapter wmBusAdapter, WMBusDevice wmBusDevice) {
        push(wmBusDevice);
    }

    private void push(WMBusDevice device) {
        SecondaryAddress address = device.getOriginalMessage().getSecondaryAddress();

        if (!entries.containsKey(address)) {
            entries.put(address, EvictingQueue.create(2000));
        }

        Entry entry = new Entry(System.currentTimeMillis(), device.getOriginalMessage().asBlob());
        entries.get(address).add(entry);
    }

    static class Entry {

        public final long time;
        public final byte[] frame;

        public Entry(long time, byte[] frame) {
            this.time = time;
            this.frame = frame;
        }
    }
}
