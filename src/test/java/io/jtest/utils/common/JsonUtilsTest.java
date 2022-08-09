package io.jtest.utils.common;

import com.fasterxml.jackson.core.JsonParseException;
import io.json.compare.util.JsonUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class JsonUtilsTest {

    private final Function<String, List<String>> extractSpecialRegexCharsFct = s -> {
        List<String> regexChars = RegexUtils.getRegexCharsFromString(s);
        return regexChars.isEmpty() ? null : regexChars;
    };

    @Test
    public void convertStringToJson() throws IOException {
        assertEquals(2, JsonUtils.toJson("{\"a\":2}").get("a").asInt());
    }

    @Test
    public void convertObjectToJson() throws IOException {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 2);
        assertEquals(2, JsonUtils.toJson(map).get("a").asInt());
    }

    @Test
    public void testBigJsonPrettyPrint() throws IOException {
        String json = ResourceUtils.read("props/bigJsons/actualLargeJson.json");
        assertNotNull(JsonUtils.prettyPrint(json));
    }

    @Test
    public void testEmptyJsonPrettyPrint() throws IOException {
        assertEquals("{ }", JsonUtils.prettyPrint("{}"));
        assertEquals("[ ]", JsonUtils.prettyPrint("[]"));
        assertEquals("null", JsonUtils.prettyPrint("null"));
        assertEquals("null", JsonUtils.prettyPrint(null));
        assertThrows(JsonParseException.class, () -> JsonUtils.prettyPrint("invalid"));
        assertNull(null, JsonUtils.prettyPrint(""));
    }
}
