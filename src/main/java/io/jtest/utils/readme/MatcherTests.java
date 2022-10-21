package io.jtest.utils.readme;

import io.jtest.utils.matcher.ObjectMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MatcherTests {

    @Test
    public void testJsonMatcher() {
        String expected = "{\n" +
                "  \"copper\": [\n" +
                "    {\n" +
                "      \"beneath\": \"heard\",\n" +
                "      \"jack\": false,\n" +
                "      \"men\": -1365455482\n" +
                "    },\n" +
                "    \"equipment\",\n" +
                "    false\n" +
                "  ],\n" +
                "  \"speak\": -263355062.75097084,\n" +
                "  \"basis\": 1670107599\n" +
                "}";
        String actual = "{\n" +
                "  \"copper\": [\n" +
                "    {\n" +
                "      \"beneath\": \"heard\",\n" +
                "      \"men\": -1365455482\n" +
                "    },\n" +
                "    \"equipment\",\n" +
                "    false\n" +
                "  ],\n" +
                "  \"speak\": -263355062.750\n" +
                "}";
        assertThrows(AssertionError.class, () -> ObjectMatcher.matchJson("Seems that objects do not match", expected, actual));
    }
}
