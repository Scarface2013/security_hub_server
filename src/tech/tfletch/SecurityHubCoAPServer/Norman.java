package tech.tfletch.SecurityHubCoAPServer;

import java.util.HashMap;
import java.util.Queue;

/*
* This is Norman. Norman handles the queueing of resources that need
* computation. Norman is supposed to use a sophisticated queueing service
* to handles this, but for right now, He's just going to use his head.
* */
public class Norman {
    private HashMap<String,Queue<Message>> queues;

    public Norman(){
        queues = new HashMap<String, Queue<Message>>();
    }

    public void send(String topicName, Message message){

    }

    private void addToBucket(String bucketName,Message message) throws Exception{
        Queue<Message> bucket;
        if(!queues.containsKey(bucketName)) {
            // TODO: Make this exception more specific
            // Or maybe even handle this in-house (create the topic)
            throw new Exception();
        }

        bucket = queues.get(bucketName);

        bucket.add(message);
    }

    public void addTopic(String[] bucketNames, String topic){

    }

    public void subscribe(String bucketName){

    }

    public void removeTopic(){

    }
}
