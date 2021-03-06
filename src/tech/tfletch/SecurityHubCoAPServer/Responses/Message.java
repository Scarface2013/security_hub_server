package tech.tfletch.SecurityHubCoAPServer.Responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Message {
    public String topicID;
    public String messageBody;

    private static Gson gson = new GsonBuilder().create();

    public Message(String topic, String messageBody){
        this.topicID = topic;
        this.messageBody = messageBody;
    }

    public static Message fromJson(String json){
        return gson.fromJson(json, Message.class);
    }

    public static String toJson(Message message){
        return gson.toJson(message);
    }
}
