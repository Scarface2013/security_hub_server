package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Responses.Message;
import tech.tfletch.SecurityHubCoAPServer.Norman;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicNotFoundException;

public class Messages extends CoapResource {

    private Norman queueHandler;

    public Messages(Norman norman){
        super("Messages");

        this.queueHandler = norman;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();
        try {
            Message message = Message.fromJson(exchange.getRequestText());
            queueHandler.enqueue(message);
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
        Message message = queueHandler.getMessageByIP(exchange.getSourceAddress());
        exchange.respond(CoAP.ResponseCode.VALID, Message.toJson(message));
    }
}
