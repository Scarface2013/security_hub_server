package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.Message;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceNotFoundException;
import tech.tfletch.SecurityHubCoAPServer.Utility.DeviceRegistrationException;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicFunnel;
import tech.tfletch.SecurityHubCoAPServer.Utility.TopicNotFoundException;

import com.google.common.hash.BloomFilter;

import java.util.*;

/*
* This is version 2 of the Queue Handler. It takes advantage of bloom filters to allow the
* security hub to work in an extremely large, decentralized hierarchy of nodes, each
* communicating through the security hub protocol.
* */
public class QueueHandler {
    private Device superPeer;

    // Answers the question 'Does one of my descendants subscribe to this topic?'
    private BloomFilter<Topic> nodeFilter;

    // Answers the question 'Which of my direct descendants contains this topic?'
    private HashMap<Device, BloomFilter<Topic>> descendantNodeFilters;

    // Maps a topic to a list of devices. Contains n-most popular topics under this node
    private HashMap<Topic, ArrayList<Device>> descendantNodeCache;

    // Dictionary of topics to existing topics
    private HashMap<String, Topic> topics;

    // Messages queued for each device
    private HashMap<Device, Queue<Message>> messageQueue;

    public QueueHandler(Device superPeer){
        this.superPeer = superPeer;

        superPeer.register();

        this.nodeFilter = BloomFilter.create(
            TopicFunnel.INSTANCE, 1_000
        );
        this.descendantNodeFilters = new HashMap<>();
        this.descendantNodeCache = new HashMap<>();
        this.topics = new HashMap<>();

        messageQueue = new HashMap<>();
    }

    public void registerDevice(Device device){
        Topic topic = new Topic(device.getName());
        this.addTopic(topic);
        messageQueue.put(device, new LinkedList<>());

        try {
            this.subscribeDeviceToTopic(device, topic);
        }
        catch(DeviceRegistrationException|DeviceNotFoundException e){
            e.printStackTrace();
        }
    }
    public void addTopic(Topic topic){
        topics.put(topic.getName(), topic);
    }

    public Topic getTopicByName(String topicName){
        return topics.get(topicName);
    }

    // This question really can't be answered in the new design. I'll think about it.
    public ArrayList<Topic> getSubscribedTopics(Device device){
        return null;
    }
    public ArrayList<Topic> getAvailableTopics(Device device){
        return new ArrayList<>(topics.values());
    }


    public void addMessage(Message message) throws TopicNotFoundException {
        Topic topic = getTopicByName(message.topicID);
        if(topic == null) throw new TopicNotFoundException();

        // If we're the first device to see this message (we got it from an IOT node), then we need to sent it up
        // to our super peer to see if it's anywhere else in the network instead of handling it normally
        if(!message.hasPropagatedToSuperPeer){
            message.hasPropagatedToSuperPeer = true;
            superPeer.sendMessage(message);
            return;
        }

        // Check hot filter
        if(descendantNodeCache.get(topic) != null){
            ArrayList<Device> devices = descendantNodeCache.get(topic);

            for(Device device : devices){
                Queue<Message> queue = messageQueue.get(device);
                queue.add(message);
            }
        }
        // Check cold filter
        else if(nodeFilter.mightContain(topic)){
            // Check child cold filters
            for(Map.Entry<Device, BloomFilter<Topic>> entry : descendantNodeFilters.entrySet()){
                Device device = entry.getKey();
                BloomFilter<Topic> bloomFilter = entry.getValue();

                // Queue message in the device's respective bucket for fetching
                if(bloomFilter.mightContain(topic)){
                    messageQueue.get(device).add(message);
                }
            }
        }


    }

    public void subscribeDeviceToTopic(Device device, Topic topic) throws DeviceNotFoundException, DeviceRegistrationException{
        BloomFilter<Topic> filter = descendantNodeFilters.get(device);
        if(filter == null) {
            filter = BloomFilter.create(
                TopicFunnel.INSTANCE, 1_000
            );
            descendantNodeFilters.put(device, filter);
        }

        filter.put(topic);
        this.nodeFilter.put(topic);
    }
    // We can't do this...
    public void unsubscribeDeviceFromTopic(Device device, Topic topic) throws DeviceNotFoundException, DeviceRegistrationException {

    }
    public Message nextMessage(Device device){
        return messageQueue.get(device).poll();
    }
}
