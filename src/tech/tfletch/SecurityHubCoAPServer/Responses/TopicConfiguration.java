package tech.tfletch.SecurityHubCoAPServer.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TopicConfiguration {
    private static Gson gson = new GsonBuilder().create();
    public String topicName;

    public static TopicConfiguration fromJson(String json){
        return gson.fromJson(json, TopicConfiguration.class);
    }

    public static String toJson(TopicConfiguration message){
        return gson.toJson(message);
    }
}
