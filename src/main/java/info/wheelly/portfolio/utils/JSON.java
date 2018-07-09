package info.wheelly.portfolio.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOError;
import java.io.IOException;
import java.io.StringReader;

public final class JSON {
    private static final ObjectMapper JSON = new ObjectMapper();
    private JSON() { }

    public static <T> T fromJson(String json, Class<T> type) {
        try (StringReader reader = new StringReader(json)) {
            return JSON.readValue(reader, type);
        }
        catch (IOException error) {
            throw new IOError(error);
        }
    }

    public static String toJson(Object value) {
        try {
            return JSON.writeValueAsString(value);
        }
        catch (JsonProcessingException error) {
            throw new IOError(error);
        }
    }
}
