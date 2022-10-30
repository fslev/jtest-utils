package io.jtest.utils.readme;

import io.jtest.utils.matcher.ObjectMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.jtest.utils.PlainHttpResponseUtils.from;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    public void testJsonMatcherWithPlaceHolders() {
        String expected = "{\n" +
                "  \"copper\": [\n" +
                "    {\n" +
                "      \"beneath\": \"~[someValueForBeneath]\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"speak\": \"~[speakValue]\"\n" +
                "}";
        String actual = "{\n" +
                "  \"copper\": [\n" +
                "    {\n" +
                "      \"beneath\": \"heard\",\n" +
                "      \"men\": -1365455482\n" +
                "    }\n" +
                "  ],\n" +
                "  \"speak\": -263355062.750,\n" +
                "  \"nr2\": 60.750\n" +
                "}";
        Map<String, Object> capturedData = ObjectMatcher.matchJson(null, expected, actual);
        assertEquals("heard", capturedData.get("someValueForBeneath"));
        assertEquals("-263355062.750", capturedData.get("speakValue"));
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
        String expected = "{\"a\":1}";
        String actual = "{\"a\":1}";
        ObjectMatcher.match("Objects were converted and matched as JSONs", expected, actual); // assertion passes

        expected = "<a>1</a>";
        actual = "<a>1</a>";
        ObjectMatcher.match("Objects were converted and matched as XMLs", expected, actual); // assertion passes

        expected = "{\"a\":i am not a json}";
        actual = "{\"a\":i am not a json}";
        ObjectMatcher.match("Objects were matched as texts", expected, actual); // assertion passes
    }

    @Test
    public void testHttpResponseMatcher() {
        String expected = "{\"status\": 200, \"headers\":[{\"Content-Length\":\"157\"}], \"body\":{\"employee\":\"John Johnson\"}}";
        String actual = "{\"status\": 200, \"headers\":[{\"Content-Length\":\"157\"}], \"body\":{\"employee\":\"John Johnson\"}}";
        ObjectMatcher.matchHttpResponse("Matching failure", from(expected), from(actual));
    }
}
