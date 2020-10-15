package io.jtest.utils.common;

import io.jtest.utils.common.JsonUtils;
import io.jtest.utils.common.RegexUtils;
import io.jtest.utils.common.ResourceUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

public class JsonUtilsTest {

    private final Function<String, List<String>> extractSpecialRegexCharsFct = s -> {
        List<String> regexChars = RegexUtils.getRegexCharsFromString(s);
        return regexChars.isEmpty() ? null : regexChars;
    };

    @Test
    public void testBigJsonBeautification() throws IOException {
        String json = ResourceUtils.read("props/bigJsons/actualLargeJson.json");
        assertNotNull(JsonUtils.prettyPrint(json));
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
