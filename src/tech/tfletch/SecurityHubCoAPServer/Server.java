package tech.tfletch.SecurityHubCoAPServer;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import tech.tfletch.SecurityHubCoAPServer.Resources.Device;
import tech.tfletch.SecurityHubCoAPServer.Resources.Devices;
import tech.tfletch.SecurityHubCoAPServer.Resources.Messages;
import tech.tfletch.SecurityHubCoAPServer.Resources.Topics;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceConfiguration;
import tech.tfletch.SecurityHubCoAPServer.Utility.SecurityHubConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/*
* Server is the way in which the devices interact with the Security Hub
* */
public class Server extends CoapServer {

    private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

    private Server() throws SocketException {
        SecurityHubConfiguration configuration;
        try {
            File file = new File("config.json");
            System.err.println(file.getAbsolutePath());
            Scanner s = new Scanner(file);
            StringBuilder configData = new StringBuilder();
            while(s.hasNextLine()) {
                configData.append(s.nextLine());
            }

            configuration = SecurityHubConfiguration.fromJson(
                configData.toString()
            );

            // Internal Tools
            SecurityHub securityHub = new SecurityHub();
            QueueHandler queueHandler = new QueueHandler(
                    new tech.tfletch.SecurityHubCoAPServer.Device(
                            new DeviceConfiguration(
                                    "superPeer",
                                    "superPeer",
                                    new URL("http://127.0.0.1:80"),
                                    "0.0.4",
                                    configuration.superPeer
                            )
                    )
            );

            securityHub.attachQueueHandler(queueHandler);
            //securityHub.attachUpdateHandler(new UpdateManager());

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
        catch(FileNotFoundException e){
            System.err.println("Configuration file not found");
            System.exit(1);
        }
        catch (MalformedURLException e){
            e.printStackTrace();
            System.exit(1);
        }

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
