package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Device;
import tech.tfletch.SecurityHubCoAPServer.QueueHandler;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceConfiguration;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;

import java.util.stream.Collectors;

public class Devices extends CoapResource {

    private SecurityHub securityHub;
    private QueueHandler queueHandler;

    public Devices(SecurityHub securityHub){
        super("Devices");

        this.securityHub = securityHub;
        this.queueHandler = securityHub.getQueueHandler();
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        StringBuilder response = new StringBuilder();
        exchange.accept();

        response.append("{\"messageBody\":[");
        response.append(
                securityHub.getConnectedDevices()
                .stream()
                .map(Device::getName)
                .collect(Collectors.joining(","))
        );
        response.append("]}");

        exchange.respond(CoAP.ResponseCode.CONTENT, response.toString());
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        // Check if address is already registered
        for( Device device : securityHub.getConnectedDevices()){
            if(exchange.getSourceAddress().equals(device.getAddress())){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Address already registered");
                return;
            }
        }

        // Parse payload
        DeviceConfiguration deviceConfiguration;
        try {
            deviceConfiguration
                = DeviceConfiguration.fromJson(exchange.getRequestText());
        }
        catch(JsonParseException e){
            exchange.respond(
                CoAP.ResponseCode.BAD_REQUEST,
                "Invalid DeviceConfiguration JSON format"
            );
            return;
        }

        if(deviceConfiguration == null || deviceConfiguration.deviceName == null){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Name not supplied");
            return;
        }
        deviceConfiguration.address = exchange.getSourceAddress();

        // Add device to SH internal representation
        Device device = new Device( deviceConfiguration );
        securityHub.addDevice(device);

        exchange.respond(CoAP.ResponseCode.VALID);
    }
}
