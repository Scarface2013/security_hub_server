package tech.tfletch.SecurityHubCoAPServer.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import tech.tfletch.SecurityHubCoAPServer.Utility.AddressDeserializer;
import tech.tfletch.SecurityHubCoAPServer.Utility.URLParser;

import java.net.InetAddress;
import java.net.URL;

public class DeviceConfiguration {
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(InetAddress.class,new AddressDeserializer())
            .registerTypeAdapter(URL.class, new URLParser())
            .create();

    public String deviceName;
    public InetAddress address;
    public URL manufacturerURL;
    public String deviceType;
    public String currentVersion;

    public static DeviceConfiguration fromJson(String json){
        return gson.fromJson(json, DeviceConfiguration.class);
    }

    public static String toJson(DeviceConfiguration message){
        return gson.toJson(message);
    }
}
