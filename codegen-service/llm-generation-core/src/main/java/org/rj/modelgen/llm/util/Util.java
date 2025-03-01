package org.rj.modelgen.llm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
        return trySerialize(obj).orElseThrow(onError);
    }

    public static Result<String, Exception> trySerialize(Object obj) {
        try {
            return Result.Ok(objectMapper.writeValueAsString(obj));
        }
        catch (Exception ex) {
            return Result.Err(ex);
        }
    }


    public static JSONObject serializeJsonOrThrow(Object obj) {
        return serializeJsonOrThrow(obj, ex -> new RuntimeException(String.format("Failed to serialize object to JSON (%s)", ex.getMessage()), ex));
    }

    public static JSONObject serializeJsonOrThrow(Object obj, Function<Exception, RuntimeException> onError) {
        return trySerializeJson(obj).orElseThrow(onError);
    }

    public static Result<JSONObject, Exception> trySerializeJson(Object obj) {
        return trySerialize(obj).map(JSONObject::new);
    }

    public static byte[] serializeBinaryOrThrow(Object obj) {
        return serializeBinaryOrThrow(obj, ex -> new RuntimeException(String.format("Failed to binary serialize object (%s)", ex.getMessage()), ex));
    }

    public static byte[] serializeBinaryOrThrow(Object obj, Function<Exception, RuntimeException> onError) {
        return trySerializeBinary(obj).orElseThrow(onError);
    }

    public static Result<byte[], Exception> trySerializeBinary(Object obj) {
        try {
            return Result.Ok(objectMapper.writeValueAsBytes(obj));
        }
        catch (Exception ex) {
            return Result.Err(ex);
        }
    }

    public static <T> T deserializeOrThrow(String serialized, Class<T> toClass) {
        return deserializeOrThrow(serialized, toClass, ex -> new RuntimeException(String.format("Failed to deserialize object (%s)", ex.getMessage()), ex));
    }

    public static <T> T deserializeOrThrow(String serialized, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        return tryDeserialize(serialized, toClass).orElseThrow(onError);
    }

    public static <T> Result<T, Exception> tryDeserialize(String serialized, Class<T> toClass) {
        try {
            return Result.Ok(objectMapper.readValue(serialized, toClass));
        }
        catch (Exception ex) {
            return Result.Err(ex);
        }
    }

    public static <T> T deserializeJsonOrThrow(JSONObject serialized, Class<T> toClass) {
        return deserializeJsonOrThrow(serialized, toClass, ex -> new RuntimeException(String.format("Failed to deserialize JSON into object (%s)", ex.getMessage()), ex));
    }

    public static <T> T deserializeJsonOrThrow(JSONObject serialized, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        if (serialized == null) throw onError.apply(new IllegalArgumentException("Cannot deserialize null JSON object"));
        return deserializeOrThrow(serialized.toString(), toClass, onError);
    }

    public static <T> Result<T, Exception> tryDeserializeJson(JSONObject serialized, Class<T> toClass) {
        if (serialized == null) return Result.Err(new IllegalArgumentException("Cannot deserialize null JSON object"));
        return tryDeserialize(serialized.toString(), toClass);
    }

    public static <T> T deserializeBinaryOrThrow(byte[] serialized, Class<T> toClass) {
        return deserializeBinaryOrThrow(serialized, toClass, ex -> new RuntimeException(String.format("Failed to deserialize binary object (%s)", ex.getMessage()), ex));
    }

    public static <T> T deserializeBinaryOrThrow(byte[] serialized, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        return tryDeserializeBinary(serialized, toClass).orElseThrow(onError);
    }

    public static <T> Result<T, Exception> tryDeserializeBinary(byte[] serialized, Class<T> toClass) {
        try {
            return Result.Ok(objectMapper.readValue(serialized, toClass));
        }
        catch (Exception ex) {
            return Result.Err(ex);
        }
    }

    public static <T> T convertOrThrow(Object obj, Class<T> toClass) {
        return convertOrThrow(obj, toClass, ex -> new RuntimeException(String.format("Failed to convert object (%s)", ex.getMessage()), ex));
    }

    public static <T> T convertOrThrow(Object obj, Class<T> toClass, Function<Exception, RuntimeException> onError) {
        return tryConvert(obj, toClass).orElseThrow(onError);
    }

    public static <T> Result<T, Exception> tryConvert(Object obj, Class<T> toClass) {
        try {
            return Result.Ok(objectMapper.convertValue(obj, toClass));
        }
        catch (Exception ex) {
            return Result.Err(ex);
        }
    }

    public static <T> Result<JSONObject, Exception> tryParseJson(String content) {
        try {
            if (content == null) return Result.Err(new IllegalArgumentException("Cannot parse null content into JSON"));
            return Result.Ok(new JSONObject(content));
        }
        catch (Exception ex) {
            return Result.Err(ex);
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

    public static String loadStringFromFile(File file) {
        return tryLoadStringFromFile(file)
                .orElseThrow(RuntimeException::new);
    }

    public static Result<String, String> tryLoadStringFromFile(File file) {
        if (file == null) return Result.Err("Cannot load data from null file");
        if (!file.exists()) return Result.Err(String.format("Cannot load data from file; file '%s' does not exist", file.getAbsolutePath()));

        try {
            return Result.Ok(FileUtils.readFileToString(file, Charset.defaultCharset()));
        }
        catch (IOException ex) {
            return Result.Err(String.format("Failed to load data from file '%s': %s", file.getAbsolutePath(), ex.getMessage()));
        }
    }

    public static int estimateTokenSize(String string) {
        if (StringUtils.isBlank(string)) return 0;

        return StringUtils.countMatches(string, ' ') + 1;
    }

    public static String displayStringWithMaxLength(String str, int maxLength) {
        if (str.length() > maxLength) {
            return str.substring(0, maxLength) + "...";
        }

        return str;
    }

    public static <T extends CloneableObject> T cloneObject(T object) {
        try {
            return (T)object.clone();
        }
        catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Clone is not supported by object");
        }
    }
}
