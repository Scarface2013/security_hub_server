package tech.tfletch.SecurityHubCoAPServer.Utility;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.InetAddress;

public class AddressDeserializer extends TypeAdapter<InetAddress> {
    @Override
    public InetAddress read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return InetAddress.getByName(reader.nextString());
    }

    @Override
    public void write(JsonWriter jsonWriter, InetAddress address) throws IOException {
        jsonWriter.value(address.getHostAddress());
    }
}
