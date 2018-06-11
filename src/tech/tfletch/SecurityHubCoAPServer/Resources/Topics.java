package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Device;
import tech.tfletch.SecurityHubCoAPServer.Responses.TopicConfiguration;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;
import tech.tfletch.SecurityHubCoAPServer.Topic;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Topics extends CoapResource {

    private SecurityHub securityHub;

    public Topics(SecurityHub securityHub){
        super("Topics");

        this.securityHub = securityHub;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();

        TopicConfiguration topicConfiguration;

        try{
            topicConfiguration = TopicConfiguration.fromJson(exchange.getRequestText());
        }catch (JsonParseException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid TopicConfiguration json format");
            return;
        }

        Topic t = new Topic(topicConfiguration);

        // Make sure topic doesn't exist
        if(!(securityHub.getQueueHandler().getTopicByName(t.getName()) == null)){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Topic " + t.getName() + " already exists");
        }

        securityHub.getQueueHandler().addTopic(t);

        exchange.respond(CoAP.ResponseCode.VALID);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();
        Device requester = securityHub.getDeviceByIP(exchange.getSourceAddress());

        StringBuilder response = new StringBuilder();
        response.append("{\"messageBody\":[");
        response.append(
                securityHub.getQueueHandler().getSubscribedTopics(requester)
                .stream()
                .map(Topic::getName)
                .collect(Collectors.joining(","))
        );
        response.append("]}");

        exchange.respond(CoAP.ResponseCode.CONTENT, response.toString());
    }
}
