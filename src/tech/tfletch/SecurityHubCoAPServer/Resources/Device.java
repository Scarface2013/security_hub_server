package tech.tfletch.SecurityHubCoAPServer.Resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceNotFoundException;

public class Device extends CoapResource {
    private SecurityHub securityHub;

    public Device(SecurityHub securityHub){
        super("Device");

        this.securityHub = securityHub;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();
        String wantedDeviceID = exchange.getQueryParameter("deviceID");

        try{
            tech.tfletch.SecurityHubCoAPServer.Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());
        }catch( DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register your device before obtaining" +
                    " information on other devices");
            return;
        }

        try{
            if(wantedDeviceID == null || wantedDeviceID.equals("")){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Please supply a deviceID");
                return;
            }

            exchange.respond(CoAP.ResponseCode.CONTENT,
                securityHub.getConnectedDevices()
                    .stream()
                    .filter(wantedDevice -> wantedDevice.getName().equals(wantedDeviceID))
                    .map(tech.tfletch.SecurityHubCoAPServer.Device::toString)
                    .findFirst()
                    .orElseThrow(DeviceNotFoundException::new)
            );
        }catch (DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Device " + wantedDeviceID + " does not exist");
        }
    }
}
