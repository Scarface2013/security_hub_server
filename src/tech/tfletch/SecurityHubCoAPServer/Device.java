package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceConfiguration;

import java.net.InetAddress;

public class Device {
    private String name;

    private InetAddress address;
    public InetAddress getAddress() {
        return address;
    }

    public Device(DeviceConfiguration deviceConfiguration){
        this.name = deviceConfiguration.deviceName;
        this.address = deviceConfiguration.address;
    }

    public String getName() {
        return name;
    }

}
