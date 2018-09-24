package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.google.gson.JsonParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Device;
import tech.tfletch.SecurityHubCoAPServer.Responses.TopicConfiguration;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;
import tech.tfletch.SecurityHubCoAPServer.Topic;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceNotFoundException;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceRegistrationException;

import java.util.stream.Collectors;

public class Topics extends CoapResource {

    private SecurityHub securityHub;

    public Topics(SecurityHub securityHub){
        super("Topics");

        this.securityHub = securityHub;
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        exchange.accept();

        try {
            Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());
            String topicID = exchange.getQueryParameter("topicID");

            try {
                securityHub.getQueueHandler().subscribeDeviceToTopic(
                    device,
                    securityHub.getQueueHandler().getTopicByName(topicID)
                );
                exchange.respond(CoAP.ResponseCode.CREATED);
            }catch (DeviceNotFoundException e){
                // This one actually shouldn't happen. It means the device exists but has no bucket
                System.err.println("Device found in device list but no corresponding bucket");
                e.printStackTrace();

                throw e;
            }catch (DeviceRegistrationException e){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Device is already registered to topic");
            }
        }catch (DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register your device before you subscribe" +
                    "to topics");
        }
    }
    @Override
    public void handleDELETE(CoapExchange exchange) {
        exchange.accept();

        try {
            Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());
            String topicID = exchange.getQueryParameter("topicID");

            try {
                securityHub.getQueueHandler().unsubscribeDeviceFromTopic(
                    device,
                    securityHub.getQueueHandler().getTopicByName(topicID)
                );
                exchange.respond(CoAP.ResponseCode.CREATED);
            }catch (DeviceNotFoundException e){
                // This one actually shouldn't happen. It means the device exists but has no bucket
                System.err.println("Device found in device list but no corresponding bucket");
                e.printStackTrace();

                // We can't just crash, so I guess it's fine to log it and respond like it isn't registered.
                // It might also be smart to create the missing bucket but I'm not even sure that his will
                // ever happen yet so...
                throw e;
            }
            catch(DeviceRegistrationException e){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Device is not registered to topic");
            }
        }catch (DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register your device before you subscribe" +
                    "to topics");
        }
    }
    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();

        try{
            // Permissions aren't implemented yet so we technically don't need the device, but we will and
            // we don't want unregistered devices creating topics
            Device device = securityHub.getDeviceByIP(exchange.getSourceAddress());

            TopicConfiguration topicConfiguration = TopicConfiguration.fromJson(exchange.getRequestText());

            if(topicConfiguration.topicID == null || topicConfiguration.topicID.equals("")){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Please supply a topic name");
            }

            Topic t = new Topic(topicConfiguration);

            if(!(securityHub.getQueueHandler().getTopicByName(t.getName()) == null)){
                exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Topic " + t.getName() + " already exists");
            }

            securityHub.getQueueHandler().addTopic(t);
            exchange.respond(CoAP.ResponseCode.CREATED);

        }catch (JsonParseException e){
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Invalid TopicConfiguration json format");
        }
        catch (DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.UNAUTHORIZED, "You must register " +
                    "your device before you can POST messages"
            );
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();

        try {
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
        catch(DeviceNotFoundException e){
            exchange.respond(CoAP.ResponseCode.FORBIDDEN, "You must register your device before you can request topics");
        }

    }
}
