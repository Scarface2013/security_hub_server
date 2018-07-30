package tech.tfletch.SecurityHubCoAPServer;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceConfiguration;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceUpdateInformation;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;

/*
* Represents a device that has registered for the network.
*
* The device is created after a POST to the /devices/ endpoint and is stored in
* SecurityHub's deviceList. Devices can be fetched by their name or IP but are NOT
* to be constructed outside of that endpoint.
* */
public class Device {
    private String name;
    private String deviceType;
    private URL manufacturerURL;
    private String currentVersion;

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
        this.name = deviceConfiguration.deviceName;
        this.address = deviceConfiguration.address;
        this.deviceType = deviceConfiguration.deviceType;
        this.manufacturerURL = deviceConfiguration.manufacturerURL;
        this.currentVersion = deviceConfiguration.currentVersion;
    }

    public String getName() {
        return name;
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
