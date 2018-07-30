package tech.tfletch.SecurityHubCoAPServer.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class DeviceUpdateInformationList {
    private static Gson gson = new GsonBuilder().create();

    public ArrayList<DeviceUpdateInformation> updates;

    public static DeviceUpdateInformationList fromJson(String json){
        return gson.fromJson(json, DeviceUpdateInformationList.class);
    }
}
