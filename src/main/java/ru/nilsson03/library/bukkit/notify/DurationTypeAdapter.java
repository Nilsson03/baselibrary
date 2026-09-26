package ru.nilsson03.library.bukkit.notify;

import java.io.IOException;
import java.time.Duration;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Serializes {@link Duration} without reflective access to JDK internals.
 * Also supports the object representation produced by older Gson versions.
 */
final class DurationTypeAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
            return;
        }
        out.value(duration.toString());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (token == JsonToken.STRING) {
            return Duration.parse(in.nextString());
        }
        if (token != JsonToken.BEGIN_OBJECT) {
            throw new IOException("Expected Duration as ISO-8601 string or object, but was " + token);
        }

        long seconds = 0;
        int nanos = 0;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if ("seconds".equals(name)) {
                seconds = in.nextLong();
            } else if ("nanos".equals(name)) {
                nanos = in.nextInt();
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        return Duration.ofSeconds(seconds, nanos);
    }
}
