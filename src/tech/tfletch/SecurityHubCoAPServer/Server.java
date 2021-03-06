package tech.tfletch.SecurityHubCoAPServer;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import tech.tfletch.SecurityHubCoAPServer.Resources.Device;
import tech.tfletch.SecurityHubCoAPServer.Resources.Devices;
import tech.tfletch.SecurityHubCoAPServer.Resources.Messages;
import tech.tfletch.SecurityHubCoAPServer.Resources.Topics;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/*
* Server is the way in which the devices interact with the Security Hub
* */
public class Server extends CoapServer {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

    private Server() throws SocketException {
        // Internal Tools
        SecurityHub securityHub = new SecurityHub();
        QueueHandler queueHandler = new QueueHandler();

        securityHub.attachQueueHandler(queueHandler);
        securityHub.attachUpdateHandler(new UpdateManager());

        // Endpoints
        this.add(
            new Devices(securityHub).add(
                new Device(securityHub)
            )
        ).add(
            new Messages(securityHub)
        ).add(
            new Topics(securityHub)
        );
    }

    private void addEndpoints(){
        for( InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces() ){
            // Only bind on IPV4 addresses
            // Once the SH is actually up and running, we should explicitly
            // bind to out wlan0 address (10.0.0.1 currently)
            if( addr instanceof Inet4Address && !addr.isLoopbackAddress()){
                CoapEndpoint endpoint = new CoapEndpoint(new InetSocketAddress(addr,COAP_PORT));
                this.addEndpoint(endpoint);
            }
        }
    }

    public static void main(String[] args){

        // Launch the CoAP Server
        try {
            Server server = new Server();

            server.addEndpoints();
            server.start();

            System.out.println("Server started on " + server.getEndpoint(COAP_PORT).getUri().toString());
        }catch(SocketException e){
            System.err.println("Error creating server on port" + COAP_PORT);
        }

    }
}
