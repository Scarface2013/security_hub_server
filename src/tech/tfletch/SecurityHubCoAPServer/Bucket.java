package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.Message;

import java.util.LinkedList;
import java.util.Queue;
/*
* A Bucket is where we put message before they are sent out.
*
* Buckets live inside of the QueueHandler
* */
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
