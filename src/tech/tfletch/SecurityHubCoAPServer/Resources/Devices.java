package tech.tfletch.SecurityHubCoAPServer.Resources;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import tech.tfletch.SecurityHubCoAPServer.SecurityHub;

public class Devices extends CoapResource {

    private SecurityHub securityHub;

    public Devices(SecurityHub securityHub){
        super("Devices");

        this.securityHub = securityHub;
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();
    }
}
