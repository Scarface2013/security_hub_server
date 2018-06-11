package tech.tfletch.SecurityHubCoAPServer;

import java.net.InetAddress;
import java.util.ArrayList;

public class SecurityHub {
    private ArrayList<Device> deviceList;
    private QueueHandler queueHandler;

    SecurityHub(){
        deviceList = new ArrayList<>();
    }

    // Queue Handler Methods
    public QueueHandler getQueueHandler() {
        return queueHandler;
    }
    void attachQueueHandler(QueueHandler queueHandler){
        this.queueHandler = queueHandler;
    }

    // Device Methods
    public void addDevice(Device device){
        deviceList.add( device );
        this.getQueueHandler().addBucket(device);
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
