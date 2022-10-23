package io.jtest.utils.readme;

import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
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
                "  \"speak\": -263355062.750,\n" +
                "  \"nr1\": 62.750,\n" +
                "  \"nr2\": 60.750\n" +
                "}";
        assertThrows(AssertionError.class, () -> ObjectMatcher.matchJson("Seems that JSONs do not match", expected, actual,
                MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_STRICT_ORDER_ARRAY));
    }

    @Test
    public void testXmlMatcher() {
        String expected = "<a id=\"1\"> <lorem>ipsum</lorem> </a>";
        String actual = "<a id=\"2\"> <lorem>ipsum</lorem> </a>";
        assertThrows(AssertionError.class, () -> ObjectMatcher.matchXml("Seems that XMLs do not match",
                expected, actual, MatchCondition.XML_CHILD_NODELIST_LENGTH));
    }

    @Test
    public void testTextMatcher() {
        String expected = "lo.*sum \\Q(test)\\E";
        String actual = "lorem \n ipsum (test)";
        ObjectMatcher.matchString("Texts do not match", expected, actual); // assertion passes
        assertThrows(AssertionError.class, () -> ObjectMatcher.matchString("Texts do not match",
                expected, actual, MatchCondition.DO_NOT_MATCH)); // assertion fails
    }

    @Test
    public void testObjectMatcher() {
        final String expected = "{\"a\":1}";
        final String actual = "{\"a\":2}";
        assertThrows(AssertionError.class, () ->
                ObjectMatcher.match("Objects were converted to JSONs but they do not match", expected, actual));

        final String expected1 = "<a>1</a>";
        final String actual1 = "<a>2</a>";
        assertThrows(AssertionError.class, () ->
                ObjectMatcher.match("Objects were converted to XMLs but they do not match", expected1, actual1));

        final String expected2 = "{a:1}";
        final String actual2 = "{a:1}";
        ObjectMatcher.match("Objects were matched as texts", expected2, actual2);
    }
}
