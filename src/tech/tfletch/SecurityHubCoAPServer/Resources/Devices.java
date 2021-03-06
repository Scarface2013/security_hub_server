package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Device;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceConfiguration;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceNotFoundException;

import java.util.stream.Collectors;

public class Devices extends CoapResource {

    private SecurityHub securityHub;

    public Devices(SecurityHub securityHub){
        super("Devices");

        this.securityHub = securityHub;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();
        try {
            // Permissions aren't implemented yet so we technically don't need the device, but we will and
            // we don't want unregistered devices accessing any info
            Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());

            exchange.respond(CoAP.ResponseCode.CONTENT, "[" +
                    securityHub.getConnectedDevices()
                            .stream()
                            .map(Device::getName)
                            .collect(Collectors.joining(",")) + "]"
            );

        }catch(DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register your device before you can GET" +
                    " active devices");
        }
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();

        // Check if address is already registered. We _expect_ an Exception, and die if we don't get one (odd, I know)
        // This will eventually get overhauled to allow devices to re-POST themselves to update their config
        try{
            Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());

            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Address already registered");
        }
        catch (DeviceNotFoundException e){

            // Parse payload
            try {
                DeviceConfiguration deviceConfiguration
                    = DeviceConfiguration.fromJson(exchange.getRequestText());

                // GSON doesn't have a way to define required fields...
                if(deviceConfiguration == null || deviceConfiguration.deviceID == null){
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "deviceID not supplied");
                    return;
                }
                else if(deviceConfiguration.deviceType == null){
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "deviceType not supplied");
                    return;
                }

                // Until we get proper security in place, devices are 'verified' by their IP.
                // It's Ick and brittle and terrible but it's the easiest way to simulate provenance
                // (And doesn't require me to dirty my JSON specs in the meantime)
                deviceConfiguration.address = exchange.getSourceAddress();

                // Add device to SH internal representation
                Device device = new Device( deviceConfiguration );
                securityHub.addDevice(device);

                exchange.respond(CoAP.ResponseCode.CREATED);
            }
            catch(JsonParseException ee){
                exchange.respond(
                    CoAP.ResponseCode.BAD_REQUEST,
                    "Invalid DeviceConfiguration JSON format"
                );
            }
        }
    }
}
