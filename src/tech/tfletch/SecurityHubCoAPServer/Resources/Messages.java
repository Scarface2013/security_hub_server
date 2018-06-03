package tech.tfletch.SecurityHubCoAPServer.Resources;

import com.rabbitmq.tools.json.JSONReader;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.Norman;

public class Messages extends CoapResource {

    private Norman queueHandler;

    public Messages(Norman norman){
        super("Messages");

        this.queueHandler = norman;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();


    }

    @Override
    public void handleGET(CoapExchange exchange) {
        super.handleGET(exchange);
    }
}
