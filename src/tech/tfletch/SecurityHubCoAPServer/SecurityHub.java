package tech.tfletch.SecurityHubCoAPServer;

import java.net.InetAddress;
import java.util.ArrayList;

public class SecurityHub {
    private ArrayList<Device> deviceList;

    public SecurityHub(){
        deviceList = new ArrayList<>();
    }

    public void addDevice(Device device){
        deviceList.add( device );
    }

    public ArrayList<Device> getConnectedDevices(){
        return deviceList;
    }

    public Device getDeviceByIP(InetAddress address){
        for(Device device : deviceList){
            if(device.getAddress().equals(address)){
                return device;
            }
        }
        return null;
    }
}
