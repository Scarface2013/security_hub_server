package tech.tfletch.SecurityHubCoAPServer;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.network.EndpointManager;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceConfiguration;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceUpdateInformation;
import tech.tfletch.SecurityHubCoAPServer.Responses.Message;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

/*
* Represents a device that has registered for the network.
*
* The device is created after a POST to the /devices/ endpoint and is stored in
* SecurityHub's deviceList. Devices can be fetched by their name or IP but are NOT
* to be constructed outside of that endpoint.
* */
public class Device {
    private String name; // Probably better called UID because it needs to be unique.
    private String deviceType;
    private URL manufacturerURL;
    private String currentVersion;

    public boolean needsUpdate = false;

    private InetAddress address;
    public InetAddress getAddress() {
        return address;
    }
    public URL getManufacturerURL(){
        return manufacturerURL;
    }
    public String getDeviceType(){
        return deviceType;
    }
    public String getCurrentVersion() {
        return currentVersion;
    }

    public Device(DeviceConfiguration deviceConfiguration){
        this.name = deviceConfiguration.deviceID;
        this.address = deviceConfiguration.address;
        this.deviceType = deviceConfiguration.deviceType;
        this.manufacturerURL = deviceConfiguration.manufacturerURL;
        this.currentVersion = deviceConfiguration.currentVersion;
    }

    public DeviceConfiguration deviceConfiguration(){
        return new DeviceConfiguration(
                this.name,
                this.deviceType,
                this.manufacturerURL,
                this.currentVersion,
                this.address
        );
    }

    @Override
    public String toString(){
        return DeviceConfiguration.toJson(this.deviceConfiguration());
    }

    public String getName() {
        return name;
    }

    public void register(){
        System.err.println("Registering security hub with super peer at address=" + "coap:/" + address.toString() + ":5683/Messages");
        CoapClient sender = new CoapClient("coap:/" + address.toString() + ":5683/Devices");
        CoapHandler handler = new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                System.out.println("Device registered with response=(" +coapResponse.getCode() + ") " + coapResponse.getResponseText());
            }

            @Override
            public void onError() {
                System.err.println("Device registration failed with superPeer");
            }
        };

        try {
            DeviceConfiguration deviceConfiguration = new DeviceConfiguration(
                    UUID.randomUUID().toString(),
                    "Security Hub",
                    new URL("http://127.0.0.1"),
                    "0.0.3",
                    EndpointManager.getEndpointManager().getDefaultEndpoint().getAddress().getAddress()
            );

            sender.post(handler, DeviceConfiguration.toJson(deviceConfiguration), MediaTypeRegistry.APPLICATION_JSON);
        }
        catch(MalformedURLException e){
            e.printStackTrace();
        }
    }

    // This should only be done on devices that implement the Security Hub CoAP Server (parent, superPeer, etc.).
    public void sendMessage(Message message){
        System.err.println("Sending message to super peer at address=" + "coap:/" + address.toString() + ":5683/Messages");
        CoapClient sender = new CoapClient("coap:/" + address.toString() + ":5683/Messages");
        CoapHandler handler = new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                System.err.println("Message sent with response=(" +coapResponse.getCode() + ") " + coapResponse.getResponseText());
            }

            @Override
            public void onError() {
                System.err.println("Message failed to send");
            }
        };
        System.err.println(Message.toJson(message));
        sender.post(handler, Message.toJson(message), MediaTypeRegistry.APPLICATION_JSON);
    }

    // Pings the device for activity
    public String getStatus(){
        CoapClient pinger = new CoapClient("coap:/" + address.toString() + ":5683/status");

        CoapResponse resp = pinger.get();
        if(resp != null){
            return resp.getResponseText();
        }else{
            System.err.println("Device " + this.getName() + " not reachable at " +
                "coap:/" + this.getAddress().toString() + ":5683/status"
            );
            return "Unavailable";
        }
    }

    public void pushUpdate(DeviceUpdateInformation deviceUpdateInformation){
        // First, we need to tell the device to lock so that it doesn't try to
        // do anything while we're preparing / pushing the update
        CoapClient locker = new CoapClient("coap:/" + address.toString() + ":5683/lock");
        locker.post("",MediaTypeRegistry.TEXT_PLAIN);

        // Then, we make sure that the file is in the correct place on disk
        try{
            File update = new File("update/" + this.getDeviceType() +
                "/" + deviceUpdateInformation.updateId
            );

            InputStream updateStream = new FileInputStream(update);

            CoapClient updater = new CoapClient("coap:/" + address.toString()
                    + ":5683/update"
            );
            byte[] updateBinary = updateStream.readAllBytes();
            updater.post(updateBinary, MediaTypeRegistry.APPLICATION_OCTET_STREAM);

            this.currentVersion = deviceUpdateInformation.updateId;
        }
        catch(FileNotFoundException e){
            System.err.println(
                "Update file can not be found for device" + this.getName()
            );
            e.printStackTrace(System.err);
        }
        catch(IOException e){
            System.err.println(
                "Problem reading update into memory for device" + this.getName()
            );
            e.printStackTrace(System.err);
        }
        finally{
            CoapClient unlocker = new CoapClient("coap:/" + address.toString() + ":5683/unlock");
            unlocker.post("",MediaTypeRegistry.TEXT_PLAIN);
        }
    }

}
