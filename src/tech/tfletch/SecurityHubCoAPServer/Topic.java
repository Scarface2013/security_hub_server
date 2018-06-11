package tech.tfletch.SecurityHubCoAPServer;

import tech.tfletch.SecurityHubCoAPServer.Responses.TopicConfiguration;

public class Topic implements Subscribable {
    String name;

    public Topic(String name){
        this.name = name;
    }
    public Topic(TopicConfiguration topicConfiguration){
        name = topicConfiguration.topicName;
    }

    public String getName() {
        return name;
    }
}
