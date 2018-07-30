package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceUpdateInformation;
import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceUpdateInformationList;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class UpdateService implements Callable<DeviceUpdateInformation> {
    private Device device;

    public UpdateService(Device device){
        this.device = device;
    }

    // Return whether an update for this device is necessary
    public DeviceUpdateInformation call() {
        URL manufacturerBaseURL = device.getManufacturerURL();

        // Check manufacturer for update information
        boolean needsUpdate = false;
        DeviceUpdateInformation deviceUpdateInformation;
        try {
            URL manufacturerAvailableUpdatesURL = new URL(
                    manufacturerBaseURL,
                    "/update/" + device.getDeviceType()
            );

            URLConnection availableUpdate = manufacturerAvailableUpdatesURL.openConnection();

            BufferedReader resultReader = new BufferedReader(
                new InputStreamReader(
                    availableUpdate.getInputStream()
                )
            );

            String result = resultReader.lines().collect(Collectors.joining("\n"));

            DeviceUpdateInformationList updateInformationList = DeviceUpdateInformationList.fromJson(result);

            // We are only really concerned with the latest update (at index 0, according to the spec)
            deviceUpdateInformation = updateInformationList.updates.get(0);

            // We expect the updateId to be a semanticVersion with 3 parts
            String[] currentVersion = device.getCurrentVersion().split("\\.");
            String[] nextAvailableVersion = deviceUpdateInformation.updateId.split("\\.");

            for(int i = 0; i < 3; i++){
                if(Integer.parseInt(currentVersion[i]) < Integer.parseInt(nextAvailableVersion[i])){
                    needsUpdate = true;
                    System.out.println("Update found for device=" + device.getName() + " " +
                        "(deviceType=" + device.getDeviceType() + "). " +
                        "CurrentVersion=" + device.getCurrentVersion() + " " +
                        "nextAvailableVersion=" + deviceUpdateInformation.updateId + " " +
                        "size=" + deviceUpdateInformation.size
                    );
                    break;
                }
            }

            // Attach needsUpdate to the return object so that updateManger can
            // process accordingly
            deviceUpdateInformation.needsUpdate = needsUpdate;
        }
        catch(IOException e){
            System.err.println("Error: Problem contacting manufacturer for update information");
            e.printStackTrace(System.err);
            return null;
        }

        // If manufacturer has newer update, download it and save it to disk to
        // be distributed to the device
        try {
            if (needsUpdate && updateDoesNotExist(deviceUpdateInformation)) {

                String updatePath = "update/" + device.getDeviceType() + "/" + deviceUpdateInformation.updateId;
                URL manufacturerUpdateURL = new URL(
                        manufacturerBaseURL,
                        "/" + updatePath
                );

                URLConnection updateConnection = manufacturerUpdateURL.openConnection();

                // Save to file
                File saveFile = new File(updatePath);
                saveFile.getParentFile().mkdirs();
                byte[] buffer = new byte[8 * 1024];
                BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));
                while(updateConnection.getInputStream().read(buffer) != -1){
                    String s = new String(buffer);
                    writer.write(s);
                }
            }
        }
        catch(IOException e){
            System.err.println("Error: Problem contacting manufacturer for update binary");
            e.printStackTrace(System.err);

        }
        return deviceUpdateInformation;
    }

    private boolean updateDoesNotExist(DeviceUpdateInformation deviceUpdateInformation){
        // Try to open file
        File updateLocation = new File("update/" + deviceUpdateInformation.deviceType +
            "/" + deviceUpdateInformation.updateId
        );

        return !updateLocation.exists();
    }
}
