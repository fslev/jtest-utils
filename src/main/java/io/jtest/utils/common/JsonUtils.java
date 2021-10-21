package io.jtest.utils.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public static JsonNode toJson(Object obj) throws IOException {
        return obj instanceof JsonNode ? (JsonNode) obj :
                (obj instanceof String) ? MAPPER.readTree(obj.toString()) : MAPPER.convertValue(obj, JsonNode.class);
    }

    public static String prettyPrint(String content) {
        if (content != null && !content.isEmpty()) {
            try {
                return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(toJson(content));
            } catch (IOException ignored) {
            }
        }
        return content;
    }

    public static <R> Map<String, R> walkJsonAndProcessNodes(String json, Function<String, R> processFunction) throws IOException {
        Map<String, R> resultsMap = new HashMap<>();
        JsonNode jsonNode = toJson(json);
        Json.walkAndProcessJson(jsonNode, processFunction, "", resultsMap);
        return resultsMap;
    }
}
