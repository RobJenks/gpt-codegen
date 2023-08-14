package org.rj.modelgen.llm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Function;

public class Util {
    private static final Logger LOG = LoggerFactory.getLogger(Util.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static String serializeOrThrow(Object obj) {
        return serializeOrThrow(obj, ex -> new RuntimeException(String.format("Failed to serialize object (%s)", ex.getMessage()), ex));
    }

    public static String serializeOrThrow(Object obj, Function<Exception, RuntimeException> onError) {
        try {
            return objectMapper.writeValueAsString(obj);
        }
        catch (Exception ex) {
            throw onError.apply(ex);
        }
    }

    public static byte[] serializeBinaryOrThrow(Object obj) {
        return serializeBinaryOrThrow(obj, ex -> new RuntimeException(String.format("Failed to binary serialize object (%s)", ex.getMessage()), ex));
    }

    public static byte[] serializeBinaryOrThrow(Object obj, Function<Exception, RuntimeException> onError) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        }
        catch (Exception ex) {
            throw onError.apply(ex);
        }
    }

    public static <T> T deserializeOrThrow(String serialized, Class<T> toClass) {
        return deserializeOrThrow(serialized, toClass, ex -> new RuntimeException(String.format("Failed to deserialize object (%s)", ex.getMessage()), ex));
    }

    public static <T> T deserializeOrThrow(String serialized, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        try {
            return objectMapper.readValue(serialized, toClass);
        }
        catch (Exception ex) {
            throw onError.apply(ex);
        }
    }

    public static <T> T deserializeBinaryOrThrow(byte[] serialized, Class<T> toClass) {
        return deserializeBinaryOrThrow(serialized, toClass, ex -> new RuntimeException(String.format("Failed to deserialize binary object (%s)", ex.getMessage()), ex));
    }

    public static <T> T deserializeBinaryOrThrow(byte[] serialized, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        try {
            return objectMapper.readValue(serialized, toClass);
        }
        catch (Exception ex) {
            throw onError.apply(ex);
        }
    }

    public static <T> T convertOrThrow(Object obj, Class<T> toClass) {
        return convertOrThrow(obj, toClass, ex -> new RuntimeException(String.format("Failed to convert object (%s)", ex.getMessage()), ex));
    }

    public static <T> T convertOrThrow(Object obj, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        try {
            return objectMapper.convertValue(obj, toClass);
        }
        catch (Exception ex) {
            throw onError.apply(ex);
        }
    }

   public static String loadStringResource(String resource) {
        return Optional.ofNullable(resource)
                .map(x -> x.startsWith("/") ? x : ("/" + x))
                .map(resourcePath -> {
                    try {
                        return IOUtils.resourceToString(resourcePath, Charset.defaultCharset());
                    }
                    catch (IOException ex) {
                        throw new RuntimeException(String.format("Failed to load resource '%s': %s", resourcePath, ex.getMessage()), ex);
                    }
                })
                .orElseThrow(() -> new RuntimeException("Cannot load invalid null resource"));
    }

   public static Optional<String> loadOptionalStringResource(String resource) {
        return Optional.ofNullable(resource)
                .map(x -> x.startsWith("/") ? x : ("/" + x))
                .map(resourcePath -> {
                    try {
                        return IOUtils.resourceToString(resourcePath, Charset.defaultCharset());
                    }
                    catch (IOException ex) {
                        return null;
                    }
                });
    }

    public static int estimateTokenSize(String string) {
        if (StringUtils.isBlank(string)) return 0;

        return StringUtils.countMatches(string, ' ') + 1;
    }
}
