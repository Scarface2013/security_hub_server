package tech.tfletch.SecurityHubCoAPServer.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tech.tfletch.SecurityHubCoAPServer.Utility.AddressDeserializer;

import java.net.InetAddress;

public class DeviceConfiguration {
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(InetAddress.class,new AddressDeserializer())
            .create();

    public String deviceName;
    public InetAddress address;

    public static DeviceConfiguration fromJson(String json){
        return gson.fromJson(json, DeviceConfiguration.class);
    }

    public static String toJson(DeviceConfiguration message){
        return gson.toJson(message);
    }
}
