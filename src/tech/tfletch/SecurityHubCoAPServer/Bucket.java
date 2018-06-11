package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.Message;

import java.util.LinkedList;
import java.util.Queue;

public class Bucket {
    Queue<Message> queue;
    Device device;

    Bucket(Device device){
        this.device = device;
        this.queue = new LinkedList<>();
    }

    boolean forDevice(Device device){
        return this.device == device;
    }

}
