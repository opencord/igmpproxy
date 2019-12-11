# IGMP Proxy app

IGMP Proxy app receives and decodes igmp report messega from hosts.

## Design Document

Please checkout the detailed design [document](DesignDoc.md).

## Json Examples

After you activated the app in ONOS with
```bash
app activate org.opencord.igmpproxy
```
You need to submit json files for configuration.
Some example can be found in the [examples folder](examples).
Please modify them accordingluy

The command to load a netcfg file is 
```bash
onos netcfg <onos_ip> path/to/file.json
```