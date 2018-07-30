package tech.tfletch.SecurityHubCoAPServer.Utility;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.net.URL;

public class URLParser extends TypeAdapter<URL> {
    @Override
    public URL read(JsonReader reader) throws IOException{
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        return new URL(reader.nextString());
    }

    @Override
    public void write(JsonWriter jsonWriter, URL url) throws IOException {
        jsonWriter.value(url.toString());
    }
}
