package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.Message;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceNotFoundException;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceRegistrationException;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;

/*
* This is QueueHandler. QueueHandler handles the queueing of resources that need
* computation. QueueHandler is supposed to use a sophisticated queueing service
* to handles this, but for right now, He's just going to use his head.
* */
public class QueueHandler {
    private ArrayList<Bucket> buckets;
    private HashMap<Topic,ArrayList<Bucket>> topics;

    public QueueHandler(){
        this.buckets = new ArrayList<>();
        this.topics = new HashMap<>();
    }

    public void addBucket(Device device){
        Bucket bucket = new Bucket(device);
        this.buckets.add(bucket);

        Topic topic = new Topic(device.getName());
        addTopic(topic);
        this.topics.get(topic).add(bucket);
    }
    public void addTopic(Topic topic){
        this.topics.put(topic,new ArrayList<>());
    }
    public Topic getTopicByName(String topicName){
        for(Topic t : topics.keySet()){
            if(t.getName().equals(topicName)){
                return t;
            }
        }
        return null;
    }
    public ArrayList<Topic> getSubscribedTopics(Device device){
        ArrayList<Topic> toRet = new ArrayList<>();

        for(Topic t : topics.keySet()){
            for(Bucket b : topics.get(t)){
                if(b.forDevice(device)){
                    toRet.add(t);
                }
            }
        }

        System.err.println(topics.keySet());
        return toRet;
    }
    public ArrayList<Topic> getAvailableTopics(Device device){
        // This currently returns all topics. In the future, there will be
        // filtering for security purposes
        return new ArrayList<>(topics.keySet());
    }

    public void addMessage(Message message) throws TopicNotFoundException{
        Topic topic = getTopicByName(message.topicID);
        if(topic == null){
            throw new TopicNotFoundException();
        }

        this.topics.get(topic).forEach(
            bucket -> bucket.queue.add(message)
        );
    }

    public void subscribeDeviceToTopic(Device device, Topic topic) throws DeviceNotFoundException, DeviceRegistrationException{
        Bucket bucketToAdd = this.buckets
            .stream()
            .filter(bucket -> bucket.device.equals(device))
            .findFirst()
            .orElseThrow(DeviceNotFoundException::new);
        if(this.topics.get(topic).contains(bucketToAdd)){
            throw new DeviceRegistrationException();
        }
        else {
            this.topics
                    .get(topic)
                    .add(bucketToAdd);
        }
    }
    public void unsubscribeDeviceFromTopic(Device device, Topic topic) throws DeviceNotFoundException, DeviceRegistrationException {
        Bucket bucketToRemove = this.buckets
            .stream()
            .filter(bucket -> bucket.device.equals(device))
            .findFirst()
            .orElseThrow(DeviceNotFoundException::new);

        if(this.topics.get(topic).contains(bucketToRemove)){
            this.topics
                .get(topic)
                .remove(bucketToRemove);
        }else{
            throw new DeviceRegistrationException();
        }

    }
    public Message nextMessage(Device device){
        for(Bucket bucket : this.buckets){
            if(bucket.forDevice(device)){
                return bucket.queue.poll();
            }
        }
        return null;
    }
}
