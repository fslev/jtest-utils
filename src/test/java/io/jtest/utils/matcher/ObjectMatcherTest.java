package io.jtest.utils.matcher;

import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectMatcherTest {

    @Test
    public void compareNulls() {
        Map<String, Object> symbols = ObjectMatcher.match(null, null, null);
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void matchWithPolling() {
        ObjectMatcher.match("Matching failed", "lorem", () -> "lorem", Duration.ofSeconds(1), 1000L, 1.5);
        ObjectMatcher.matchString("Matching failed", "lorem", () -> "lorem", Duration.ofSeconds(1), 1000L, 1.5);
    }

    @Test
    public void doNotMatchWithNull() {
        ObjectMatcher.match(null, null, "val", MatchCondition.DO_NOT_MATCH);
    }

    @Test
    public void matchJsonWithPolling() {
        // match as JSONs
        ObjectMatcher.matchJson("Matching failed", "{\"status\":\"UPDATE\"}", new Supplier<Object>() {
            private int retry = 0;

            @Override
            public Object get() {
                retry++;
                return retry < 2 ? "{\"status\":\"UPDATING\"}" : "{\"status\":\"UPDATE\"}";
            }
        }, Duration.ofSeconds(1), 100L, 1.0);
        // match as objects
        ObjectMatcher.match("Matching failed", "{\"status\":\"UPDATE\"}", new Supplier<Object>() {
            private int retry = 0;

            @Override
            public Object get() {
                retry++;
                return retry < 2 ? "{\"status\":\"UPDATING\"}" : "{\"status\":\"UPDATE\"}";
            }
        }, Duration.ofSeconds(1), 100L, 1.0);
    }

    @Test
    public void matchJsonWithPolling_timeout() {
        // match as JSONs
        assertThrows(AssertionError.class, () -> ObjectMatcher.matchJson("Matching failed", "{\"status\":\"UPDATE\"}", new Supplier<Object>() {
            private int retry = 0;

            @Override
            public Object get() {
                retry++;
                return retry < 200 ? "{\"status\":\"UPDATING\"}" : "{\"status\":\"UPDATE\"}";
            }
        }, Duration.ofSeconds(1), 100L, 1.0));

        // match as objects
        assertThrows(AssertionError.class, () -> ObjectMatcher.match("Matching failed", "{\"status\":\"UPDATE\"}", new Supplier<Object>() {
            private int retry = 0;

            @Override
            public Object get() {
                retry++;
                return retry < 200 ? "{\"status\":\"UPDATING\"}" : "{\"status\":\"UPDATE\"}";
            }
        }, Duration.ofSeconds(1), 100L, 1.0));
    }

    @Test
    public void matchWithInvalidJson() {
        assertTrue(assertThrows(RuntimeException.class,
                () -> ObjectMatcher.matchJson("match failed", "{\"a\":1}", "{a:1}"))
                .getMessage().contains("Invalid JSON"));
        assertTrue(assertThrows(RuntimeException.class,
                () -> ObjectMatcher.matchJson("match failed", "{\"a\":1}", () -> "{a:1}", Duration.ofSeconds(1), 1000L, 1.5))
                .getMessage().contains("Invalid JSON"));
        assertTrue(assertThrows(RuntimeException.class,
                () -> ObjectMatcher.matchJson("match failed", "{a:1}", "{\"a\":1}"))
                .getMessage().contains("Invalid JSON"));
    }

    @Test
    public void compareJsonWithAssignSymbols() {
        String expected = "{\"b\":\"(~[sym1]\"}";
        String actual = "{\"a\":\"val2\",\"b\":\"(val1\"}";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, actual);
        assertEquals("val1", symbols.get("sym1"));
        assertEquals(1, symbols.size());
        symbols = ObjectMatcher.matchJson(null, expected, actual);
        assertEquals("val1", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void matchXmlWithAssignSymbolsWithChildNodeSequence() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<int a=\"(attrValue1\">some text here</int><boolean a=\"boolAttrValue\">false</boolean><str a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, actual, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
        symbols = ObjectMatcher.matchXml(null, expected, actual, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
    }

    @Test
    public void matchInvalidXMLs() {
        assertTrue(assertThrows(RuntimeException.class, () ->
                ObjectMatcher.matchXml(null, "<struct>true</struct>", "<struct>false<struct>"))
                .getMessage().contains("Invalid XML"));
        assertTrue(assertThrows(RuntimeException.class, () ->
                ObjectMatcher.matchXml(null, "<struct>true<struct>", "<struct>false</struct>"))
                .getMessage().contains("Invalid XML"));
    }

    @Test
    public void matchXmlWithPolling() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<int a=\"(attrValue1\">some text here</int><boolean a=\"boolAttrValue\">false</boolean><str a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, () -> actual,
                Duration.ofSeconds(1), 100L, 1.0, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
        symbols = ObjectMatcher.matchXml(null, expected, actual, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
        // match again directly as XMLs
        ObjectMatcher.matchXml(null, expected, () -> actual,
                Duration.ofSeconds(1), 100L, 1.0, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
    }

    @Test
    public void compareStringWithManyAssignSymbolsBetweenNewLines() {
        String expected = "~[prop1],\n This is a ~[prop2]\n ~[prop3]!";
        String actual = "Hello,\n This is a world of many nations \n And 7 continents...!";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, actual);
        assertEquals("Hello", symbols.get("prop1"));
        assertEquals("world of many nations ", symbols.get("prop2"));
        assertEquals("And 7 continents...", symbols.get("prop3"));
        symbols = ObjectMatcher.matchString(null, expected, actual);
        assertEquals("Hello", symbols.get("prop1"));
        assertEquals("world of many nations ", symbols.get("prop2"));
        assertEquals("And 7 continents...", symbols.get("prop3"));
    }

    @Test
    public void matchJsonObjectsUsingJsonPath() {
        String expected = "{\"#($..book[?(@.price <= $['expensive'])])\":[" +
                "{\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Herman Melville\",\n" +
                "                \"title\": \"~[title1]\",\n" +
                "                \"isbn\": \"0-553-21311-3\",\n" +
                "                \"price\": 8.99\n" +
                "            },\n" +
                "{\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"~[author2]\",\n" +
                "                \"title\": \"S.*e Century\",\n" +
                "                \"price\": 8.95\n" +
                "            }\n" +
                "]}";
        String actual = "{\n" +
                "    \"store\": {\n" +
                "        \"book\": [\n" +
                "            {\n" +
                "                \"category\": \"reference\",\n" +
                "                \"author\": \"Nigel Rees\",\n" +
                "                \"title\": \"Sayings of the Century\",\n" +
                "                \"price\": 8.95\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Evelyn Waugh\",\n" +
                "                \"title\": \"Sword of Honour\",\n" +
                "                \"price\": 12.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"Herman Melville\",\n" +
                "                \"title\": \"Moby Dick\",\n" +
                "                \"isbn\": \"0-553-21311-3\",\n" +
                "                \"price\": 8.99\n" +
                "            },\n" +
                "            {\n" +
                "                \"category\": \"fiction\",\n" +
                "                \"author\": \"J. R. R. Tolkien\",\n" +
                "                \"title\": \"The Lord of the Rings\",\n" +
                "                \"isbn\": \"0-395-19395-8\",\n" +
                "                \"price\": 22.99\n" +
                "            }\n" +
                "        ],\n" +
                "        \"bicycle\": {\n" +
                "            \"color\": \"red\",\n" +
                "            \"price\": 19.95\n" +
                "        }\n" +
                "    },\n" +
                "    \"expensive\": 10\n" +
                "}";
        Map<String, Object> resultedVars = ObjectMatcher.match(null, expected, actual);
        assertEquals("Moby Dick", resultedVars.get("title1"));
        assertEquals("Nigel Rees", resultedVars.get("author2"));
    }

    @Test
    public void checkHintMessageAboutUnintentionalRegexes() {
        String expectedJson = "[{ \"name\" : \"someText (anotherText)\", \"code\" : \"oneMoreText\" }]";
        String actualJson = "[{ \"name\" : \"someText (anotherText)\", \"code\" : \"oneMoreText\" }]";
        try {
            ObjectMatcher.match(null, expectedJson, actualJson);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("unintentional regexes"));
        }

        String expectedXml = "<struct>test</struct>";
        String actualXml = "<struct></struct>";
        try {
            ObjectMatcher.match(null, expectedXml, actualXml);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("unintentional regexes"));
        }

        String expected = "some value";
        String actual = "some other value";
        try {
            ObjectMatcher.match("Strings do not match", expected, actual);
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("unintentional regexes"));
        }
    }

    @Test
    public void matchHttpResponses() {
        String expected = "{\"status\":\"\\\\d+\"}";
        String actual = "{\"status\":409}";
        ObjectMatcher.matchHttpResponse(null, expected, actual);
    }

    @Test
    public void matchHttpResponses_withPolling() {
        String expected = "{\"status\":\"\\\\d+\"}";
        String actual = "{\"status\":409}";
        ObjectMatcher.matchHttpResponse(null, expected, () -> actual, Duration.ofSeconds(1), 100L, 1.0);
    }

    @Test
    public void matchHttpResponses_withPolling_negative() {
        String expected = "{\"status\":\"\\\\d+\"}";
        String actual = "{\"status\":\"invalid\"}";
        assertThrows(AssertionError.class, () ->
                ObjectMatcher.matchHttpResponse(null, expected, () -> actual, Duration.ofSeconds(1), 100L, 1.0));
    }

    @Test
    public void matchNullHttpResponse() {
        String expected = "{\"status\":\"\\\\d+\"}";
        assertThrows(Exception.class, () -> ObjectMatcher.matchHttpResponse(null, expected, null));
    }
}
