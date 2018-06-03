package tech.tfletch.SecurityHubCoAPServer;

import java.util.ArrayList;

public class SecurityHub {
    private ArrayList<Device> deviceList;

    public SecurityHub(){

    }

    public void addDevice(Device device){
        deviceList.add( device );
    }

    public Device[] getConnectedDevices(){
        return deviceList.toArray( new Device[0] );
    }
}
