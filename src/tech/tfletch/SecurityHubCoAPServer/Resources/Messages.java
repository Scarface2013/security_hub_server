package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Responses.Message;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicNotFoundException;

public class Messages extends CoapResource {

    private SecurityHub securityHub;

    public Messages(SecurityHub securityHub){
        super("Messages");

        this.securityHub = securityHub;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();
        try {
            Message message = Message.fromJson(exchange.getRequestText());
            securityHub.getQueueHandler().addMessage(message);
            exchange.respond(CoAP.ResponseCode.VALID);
        }catch(TopicNotFoundException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Topic not found");
        }catch(JsonParseException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Malformed JSON");
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();
        Message message = securityHub.getQueueHandler().nextMessage(
            securityHub.getDeviceByIP(exchange.getSourceAddress())
        );
        exchange.respond(CoAP.ResponseCode.VALID, Message.toJson(message));
    }
}
