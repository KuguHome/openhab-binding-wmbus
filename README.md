# openhab-binding-wmbus

This is a binding for the openHAB / eclipse Smart Home home automation system. It aims to make the data available to the user, which is already sent by lots of modern metering hardware (e.g. heat cost allocators, electricity/gas/water/heat meters) already widely deployed in houses and especially flats.

Development is still in progress, but the binding is already working quite nicely. Because the only device I have access to is the Techem heat cost allocators, the binding currently focuses on them. Also you need a device to receive the transmissions. The underlying library [jMBus](https://www.openmuc.org/m-bus/) supports transceivers of Amber and Radiocrafts, the binding is developed and tested only with the [Amber Wireless AMB8465-M](https://www.amber-wireless.de/de/produkte/wireless-m-bus/alle-usb-sticks/wireless-m-bus-868-mhz-usb-stick-int-antenne-amb8465-m.html).

This is intended to show it around to other people who might be interested and collect testers, reviewers and contributors who have other OSes than Linux, other recieving hardware (e.g. a CUL) or other metering devices which would be interesting to include.

What is already working:
* manually add the receive stick as a thing, set serial console device (e.g. /dev/ttyUSB0)
* populate inbox with discovery results for Techem heat cost allocators
* add discovery results as new things
* display and update data in channels/linked thing items

Work in progress:
* general smoothness
* more receiver sticks
* more parsed devices

There is some more information and discussion [in the forum](https://community.openhab.org/t/new-binding-wireless-m-bus-techem-heat-cost-allocators/16974).

## Images

![A Techem Heat Cost Allocator on the radiator](doc/techem.jpg)
![The display, showing radio mode](doc/techem_remote.jpg)
![Amber Wireless Stick on the RaspberryPi](doc/raspiamber.jpg)
![The Thing in the Control Screen](doc/control.png)
![Diagram in HABmin, fed by several HKVs ](doc/diagrams.png)


## Install

1. Run `mvn package` in the `src` directory..
2. The compilation result will be at `src/de.unidue.stud.sehawagn.openhab.binding.wmbus/target/de.unidue.stud.sehawagn.openhab.binding.wmbus-2.0.0-SNAPSHOT.jar`.
3. Drop this .jar into your openHAB2 Karaf deploy directory, e.g. `openhab2/addons`.
4. It should get automatically picked up and started by Karaf. 
5. Run `bundle:list` in the OSGi console, it should show a `wmbus` bundle in active state.
6. Open PaperUI in the browser.
7. Check that Configuration -> Bindings lists the WMBus Binding.
8. Go to Configuration -> Things.
9. Add new WMBus Binding Thing -> Amber Wireless WMBus Stick (exactly one).
11. Enter name of Serial Device (e.g. /dev/ttyUSB0, check via dmesg when plugging in the stick) as configuration parameter.
12. The Thing should show `ONLINE` as status. If not, edit the Thing, this screen should include some more error details, also check OSGi console and userdata/logs/openhab.log.
13. If everything is working correctly, heat cost allocators should be discovered automatically and turn up as new Things in the Inbox as soon as a message is received from them.
14. Search for the devices in your flat by typing in the 4 digit ID shown on the display into the search field of the Inbox and add those devices via the checkmark button.
15. Under Control, the Thing with it's different channels should display, readings should be updated regularly about every 10 minutes or so:
* Room and radiator temperature are always current (at the time of sending/receiving the message).
* "Current Reading" will only update once every day.
16. If a Persistence Add-on is installed, the readings will also be stored into the database.
17. In HABmin or HABPanel, diagrams/charts/graphs can be configured to have a look at the latest values in comparison.
18. If any Exceptions or other messages turn up in the logs or console, please let me know and open an issue here.


## Development

1. Install OpenHAB IDE according to their webpage. This is basically Eclipse IDE + Oomph + OpenHAB dev addons via Oomph.
2. Clone this repository.
3. File - Import - Maven - Existing Maven Projects. Give path to this git repository, select all three projects, add project to working set "WMBus" or similar.
