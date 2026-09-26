package ru.nilsson03.library.bukkit.notify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class DurationTypeAdapterTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .create();

    @Test
    void serializesDurationAsIso8601String() {
        assertEquals("\"PT30.25S\"", gson.toJson(Duration.ofMillis(30_250), Duration.class));
    }

    @Test
    void deserializesIso8601Duration() {
        assertEquals(Duration.ofMinutes(5), gson.fromJson("\"PT5M\"", Duration.class));
    }

    @Test
    void deserializesLegacyGsonObject() {
        String legacyJson = "{\"seconds\":30,\"nanos\":250000000}";

        assertEquals(Duration.ofMillis(30_250), gson.fromJson(legacyJson, Duration.class));
    }

    @Test
    void supportsNullDuration() {
        assertNull(gson.fromJson("null", Duration.class));
        assertEquals("null", gson.toJson(null, Duration.class));
    }
}
