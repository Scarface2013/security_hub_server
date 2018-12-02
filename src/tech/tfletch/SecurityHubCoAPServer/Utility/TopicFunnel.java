package tech.tfletch.SecurityHubCoAPServer.Utility;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import tech.tfletch.SecurityHubCoAPServer.Topic;

import java.nio.charset.Charset;

public enum TopicFunnel implements Funnel<Topic> {
    INSTANCE;

    @Override
    public void funnel(Topic topic, PrimitiveSink primitiveSink) {
        primitiveSink.putString(topic.getName(), Charset.defaultCharset());
    }
}
