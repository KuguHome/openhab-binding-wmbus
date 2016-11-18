# openhab-binding-wmbus

This is a binding for the openHAB / eclipse Smart Home home automation system. It aims to make the data available to the user, which is already sent by lots of modern metering hardware (e.g. heat cost allocators, electricity/gas/water/heat meters) already widely deployed in houses and especially flats.

Development has just started, so the binding is not really functioning yet. Because the only device I have access to is the Techem heat cost allocators, the binding focuses on them. Also you need a device to receive the transmissions. The underlying library supports transceivers of Amber and Radiocrafts, the binding is developed and tested only with the [Amber Wireless AMB8465-M](https://www.amber-wireless.de/de/produkte/wireless-m-bus/alle-usb-sticks/wireless-m-bus-868-mhz-usb-stick-int-antenne-amb8465-m.html).

This is intended to show it around to other people who might be interested and collect testers, reviewers and contributors who have other OSes than Linux, other recieving hardware (e.g. a CUL) or other metering devices which would be interesting to include.

What is already working:
* manually add the receive stick as a thing, set serial console device
* receive messages and display them in the console
* populate inbox with discovery results for Techem heat cost allocators
* add discovery results as new things

What is not yet working
* map the message data to thing channels and display it to the user
* general smoothness
