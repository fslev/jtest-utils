package io.jtest.utils.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JsonUtils {

    public static JsonNode toJson(String content) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        return mapper.readTree(content);
    }

    public static String prettyPrint(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(toJson(content));
        } catch (IOException e) {
            return content;
        }
    }

    public static <R> Map<String, R> walkJsonAndProcessNodes(String json, Function<String, R> processFunction) throws IOException {
        Map<String, R> resultsMap = new HashMap<>();
        JsonNode jsonNode = toJson(json);
        Json.walkAndProcessJson(jsonNode, processFunction, "", resultsMap);
        return resultsMap;
    }
}
