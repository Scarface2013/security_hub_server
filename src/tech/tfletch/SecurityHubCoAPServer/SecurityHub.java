package tech.tfletch.SecurityHubCoAPServer;

import java.net.InetAddress;
import java.util.ArrayList;

/*
* The Security Hub is the main interface through which the CoAP Server does work
*
* It keeps track of registered devices and acts as a pass-through for the queue
* */
public class SecurityHub {
    private ArrayList<Device> deviceList;
    private QueueHandler queueHandler;
    private UpdateManager updateManager;

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

    public UpdateManager getUpdateManager(){
        return updateManager;
    }
    void attachUpdateHandler(UpdateManager updateManager){
        this.updateManager = updateManager;
    }

    // Device Methods
    public void addDevice(Device device){
        updateManager.trackDevice(device);
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
