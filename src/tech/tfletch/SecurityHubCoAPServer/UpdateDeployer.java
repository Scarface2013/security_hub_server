package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceUpdateInformation;

public class UpdateDeployer implements Runnable{
    private DeviceUpdateInformation deviceUpdateInformation;
    private Device device;

    private final int DEVICE_STATUS_COOLDOWN = 5000;

    UpdateDeployer(Device device, DeviceUpdateInformation deviceUpdateInformation){
        this.deviceUpdateInformation = deviceUpdateInformation;
        this.device = device;
    }

    public void run() {
        // We want to deploy to a device when it's inactive
        while(true) {
            try{
                String deviceStatus = device.getStatus();
                if(deviceStatus.equals("Inactive")){
                    device.pushUpdate(deviceUpdateInformation);
                    break;
                }
                else if(deviceStatus.equals("Busy")){
                    Thread.sleep(DEVICE_STATUS_COOLDOWN);
                }
                else{
                    System.err.println("Unknown status type: " + deviceStatus);
                    break;
                }
            }
            catch(InterruptedException e){
                break;
            }
        }
    }
}
