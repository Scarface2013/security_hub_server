package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.Message;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicNotFoundException;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/*
* This is Norman. Norman handles the queueing of resources that need
* computation. Norman is supposed to use a sophisticated queueing service
* to handles this, but for right now, He's just going to use his head.
* */
public class Norman {
    private HashMap<String,Queue<Message>> buckets;
    private HashMap<String,ArrayList<String>> topics;
    private SecurityHub securityHub;

    public Norman(SecurityHub securityHub){
        this.securityHub = securityHub;

        buckets = new HashMap<>();
        topics = new HashMap<>();
    }

    public void enqueue(Message message) throws TopicNotFoundException {
        // Ensure topic exists
        if(message.topic == null || !topics.containsKey(message.topic)) {
            throw new TopicNotFoundException();
        }

        // Distribute message based on topic
        for(String bucketName : topics.get(message.topic)){
            Queue<Message> queue = buckets.get(bucketName);
            queue.add(message);
        }
    }

    public Message request(String bucketName){
        return buckets.get(bucketName).poll();
    }

    public void addTopic(ArrayList<String> bucketNames, String topic){
        topics.put(topic, bucketNames);
    }

    public void addBucket(String bucketName){
        Queue<Message> queue = new LinkedList<>();
        buckets.put(bucketName, queue);

        // Every bucket must have a topic
        ArrayList<String> bucketNames = new ArrayList<>();
        bucketNames.add(bucketName);
        addTopic(bucketNames, bucketName);
    }

    public void subscribe(String topic, String bucketName){
        topics.get(topic).add(bucketName);
    }

    public void removeTopic(String topic){

    }

    public Message getMessageByIP(InetAddress address){
        return buckets.get(
            securityHub.getDeviceByIP(address).getName()
        ).poll();

    }
}
