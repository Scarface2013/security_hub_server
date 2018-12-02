package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Device;
import tech.tfletch.SecurityHubCoAPServer.Responses.Message;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceNotFoundException;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicNotFoundException;

import java.util.Date;

public class Messages extends CoapResource {

    private SecurityHub securityHub;

    public Messages(SecurityHub securityHub){
        super("Messages");

        this.securityHub = securityHub;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        System.err.println("Post received");
        long startTime = new Date().getTime();
        exchange.accept();
        try {
            Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());

            Message message = Message.fromJson(exchange.getRequestText());
            securityHub.getQueueHandler().addMessage(message);
            exchange.respond(CoAP.ResponseCode.CREATED);
            System.err.println("Message reveiced from " + device.getName() + "(" + device.getAddress().toString() + ")");
        }catch(TopicNotFoundException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Topic not found");
        }catch(JsonParseException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Malformed JSON");
        }catch(DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register your device before you can sent a message");
        }
        System.err.println(new Date().getTime() - startTime);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();
        try {
            Message message = securityHub.getQueueHandler().nextMessage(
                    securityHub.getDeviceByIP(exchange.getSourceAddress())
            );
            exchange.respond(CoAP.ResponseCode.CONTENT, Message.toJson(message));
        }
        catch(DeviceNotFoundException e){
           exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register your device before you can poll for messages");
        }
    }
}
