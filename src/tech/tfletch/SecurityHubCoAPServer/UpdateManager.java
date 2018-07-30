package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.DeviceUpdateInformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// Background process to check for updates to devices on network
// as well as deploying those update to a device during inactivity
public class UpdateManager implements Runnable{
    private final ExecutorService pool;
    private final int COOLDOWN = 30_000; // 5 minutes
    private final int NUM_THREADS = 2;
    private final ArrayList<Device> trackedDevices;
    private final HashMap<Device, Future<DeviceUpdateInformation>> deviceFutureHashMap;

    public UpdateManager(){
        pool = Executors.newFixedThreadPool(NUM_THREADS);
        trackedDevices = new ArrayList<>();
        deviceFutureHashMap = new HashMap<>();

        // Spin up processing in a new thread
        new Thread(this).start();
    }

    public void trackDevice(Device device){
        System.out.println("Device " + device.getName() + " added to tracked devices");
        trackedDevices.add(device);
    }

    public void run(){
        System.out.println("Update manager started");
        while(true) {
            for (Device d : trackedDevices) {
                Future<DeviceUpdateInformation> future = pool.submit(new UpdateService(d));
                deviceFutureHashMap.put(d, future);
            }

            // Wait for all our futures to resolve and then deploy update if
            // needed
            while (deviceFutureHashMap.size() > 0) {
                for (Device d : deviceFutureHashMap.keySet()) {
                    try {
                        if (deviceFutureHashMap.get(d).isDone()) {
                            // Check if update was found & downloaded
                            DeviceUpdateInformation deviceUpdateInformation
                                = deviceFutureHashMap.get(d).get();

                            pool.execute(new UpdateDeployer(d, deviceUpdateInformation));
                            deviceFutureHashMap.remove(d);
                            }
                    }
                    catch(InterruptedException|ExecutionException e){
                        System.err.println(
                            "Problem executing updateService future" +
                            " on device=" + d.getName()
                        );
                        e.printStackTrace(System.err);
                    }
                }
                try {
                    Thread.sleep(5000);
                }
                catch(InterruptedException e){
                    return;
                }
            }
            // Wait COOLDOWN between update checks
            try {
                Thread.sleep(COOLDOWN);
            }
            catch(InterruptedException e){
                return;
            }
        }
    }
}
