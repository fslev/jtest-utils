package io.jtest.utils.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
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
        assertNull(JsonUtils.prettyPrint(null));
        assertEquals("invalid", JsonUtils.prettyPrint("invalid"));
        assertEquals("", JsonUtils.prettyPrint(""));
    }

    @Test
    public void testSimpleJsonSpecialRegexCharacters() throws IOException {
        String json = ResourceUtils.read("json/regex_chars/json1.json");
        Map<String, List<String>> results = JsonUtils.walkJsonAndProcessNodes(json, extractSpecialRegexCharsFct);
        assertEquals(Arrays.asList("."), results.get("foo.bar/{key}"));
        assertTrue(Arrays.asList("?", "+").containsAll(results.get("foo.bar/a^b/{value}")));
        assertTrue(Arrays.asList("^").containsAll(results.get("foo.bar/a^b/{key}")));
        assertTrue(Arrays.asList(".").containsAll(results.get("foo.bar/array[2]/a/{value}")));
        assertTrue(Arrays.asList(".").containsAll(results.get("foo.bar/array[2]/.a/{key}")));
        assertTrue(Arrays.asList("+").containsAll(results.get("foo.bar/array[2]/b/array[1]/{value}")));
        assertTrue(Arrays.asList("+").containsAll(results.get("foo.bar/array[2]/b/array[2]/ips+um/{key}")));
        assertTrue(Arrays.asList("[").containsAll(results.get("foo.bar/array[2]/b/array[2]/ips+um/{value}")));
        assertTrue(Arrays.asList(".", "$").containsAll(results.get("foo.bar/array[2]/b/array[6]/{value}")));
        assertEquals(9, results.size());
    }

    @Test
    public void testSimpleJsonSpecialRegexCharactersFromArray() throws IOException {
        String json = "[\"te?st\", {\"^a..\":\"?\"}, false]";
        Map<String, List<String>> results = JsonUtils.walkJsonAndProcessNodes(json, extractSpecialRegexCharsFct);
        assertEquals(Arrays.asList("?"), results.get("[1]/{value}"));
        assertTrue(Arrays.asList(".", "^").containsAll(results.get("[2]/^a../{key}")));
        assertTrue(Arrays.asList("?").containsAll(results.get("[2]/^a../{value}")));
        assertEquals(3, results.size());
    }
}
