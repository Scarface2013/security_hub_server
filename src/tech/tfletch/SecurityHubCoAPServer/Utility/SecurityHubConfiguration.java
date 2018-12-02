package tech.tfletch.SecurityHubCoAPServer.Utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.InetAddress;

public class SecurityHubConfiguration {

    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(InetAddress.class, new AddressDeserializer())
            .create();

    public InetAddress superPeer;

    public static SecurityHubConfiguration fromJson(String json){
        return gson.fromJson(json, SecurityHubConfiguration.class);
    }

    public static String toJson(SecurityHubConfiguration message){
        return gson.toJson(message);
    }
}
