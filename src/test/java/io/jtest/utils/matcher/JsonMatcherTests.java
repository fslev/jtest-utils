package io.jtest.utils.matcher;

import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.jtest.utils.common.ResourceUtils;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JsonMatcherTests {

    @Test
    public void compareMalformedJson() {
        String expected = "{\"!b\":val1\",\"a\":\"val2\"}";
        String actual = "{\"a\":\"val2\",\"c\":\"val1\"}";
        assertThrows(InvalidTypeException.class, () -> new JsonMatcher(null, expected, actual, null));
    }

    @Test
    public void compareJsonWithNull() {
        String expected = "{\"b\":\"val1\",\"a\":\"val2\"}";
        String actual = null;
        assertThrows(AssertionError.class, () -> new JsonMatcher(null, expected, actual, null).match());
    }

    @Test
    public void compareJsonWithString() {
        String expected = "{\"a\":\"lorem ipsum\"}";
        String actual = "string";
        assertThrows(InvalidTypeException.class, () -> new JsonMatcher(null, expected, actual, null));
    }

    @Test
    public void matchTextNodeWithQuotedString() throws InvalidTypeException {
        TextNode expected = new TextNode("some val");
        String actual = "\"some val\"";
        new JsonMatcher(null, expected, actual, null).match();
    }

    @Test
    public void matchIntNodeWithInt() throws InvalidTypeException {
        IntNode expected = new IntNode(1000);
        int actual = 1000;
        new JsonMatcher(null, expected, actual, null).match();
    }

    @Test
    public void matchTextNodeWithUnQuotedString() {
        TextNode expected = new TextNode("some val");
        String actual = "some val";
        assertThrows(InvalidTypeException.class, () -> new JsonMatcher(null, expected, actual, null).match());
    }

    @Test
    public void compareJsonWithAssignSymbolsAndInvalidRegex() throws InvalidTypeException {
        String expected = "{\"b\":\"(~[sym1]\"}";
        String actual = "{\"a\":\"val2\",\"b\":\"(val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("val1", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void compareSimpleJson() throws InvalidTypeException {
        String expected = "{\"!b\":\"val1\",\"a\":\"val2\"}";
        String actual = "{\"a\":\"val2\",\"c\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFields_in_depth() throws InvalidTypeException {
        String expected = "{\"a\":{\"abc-~[sym1]\":{\"o\":\"2\"},\"abc-~[sym2]\":{\"o\":\"0\"}}}";
        String actual = "{\"a\":{\"abc-X\":{\"o\":\"1\"},\"abc-Y\":{\"o\":\"0\"},\"abc-X\":{\"o\":\"2\"}}}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("X", symbols.get("sym1"));
        assertEquals("Y", symbols.get("sym2"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFields_in_depth_negative() {
        String expected = "{\"a\":{\"abc-~[sym1]\":{\"o\":\"2\"},\"abc-~[sym2]\":{\"o\":\"U\"}}}";
        String actual = "{\"a\":{\"abc-X\":{\"o\":\"1\"},\"abc-Y\":{\"o\":\"0\"},\"abc-X\":{\"o\":\"2\"}}}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, null).match());
    }

    @Test
    public void compareSimpleJson_checkNoExtraFieldsExist() throws InvalidTypeException {
        String expected = "{\"a\":\"val2\",\"c\":\"val1\",\"!.*\":\".*\"}";
        String actual = "{\"a\":\"val2\",\"c\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void compareSimpleJson_checkNoExtraFieldsExistNegative() throws InvalidTypeException {
        String expected = "{\"a\":\"val2\",\"c\":\"val1\",\"!.*\":\".*\"}";
        String actual = "{\"a\":\"val2\",\"c\":\"val1\",\"d\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        assertThrows(AssertionError.class, matcher::match);
    }

    @Test
    public void compareSimpleJsonWithAssignSymbols() throws InvalidTypeException {
        String expected = "{\"!b\":\"~[sym1]\",\"a\":\"~[sym2]\",\"c\":\"~[sym3]\"}";
        String actual = "{\"a\":\"val2\",\"d\":\"val3\",\"c\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("val2", symbols.get("sym2"));
        assertEquals("val1", symbols.get("sym3"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareJsonArray() throws InvalidTypeException {
        String expected = "{\"b\":\"val1\",\"a\":[1,2,3,4]}";
        String actual = "{\"a\":[5,4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void compareJsonArray_checkNoExtraElementsExist() throws InvalidTypeException {
        String expected = "{\"b\":\"val1\",\"a\":[1,2,3,4,\"!.*\"]}";
        String actual = "{\"a\":[4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void compareJsonArray_checkNoExtraElementsExist_negative() throws InvalidTypeException {
        String expected = "{\"b\":\"val1\",\"a\":[1,2,3,4,\"!.*\"]}";
        String actual = "{\"a\":[5,4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        assertThrows(AssertionError.class, matcher::match);
    }

    @Test
    public void compareJsonArrayWithAssignSymbols() throws InvalidTypeException {
        String expected = "{\"b\":\"val1\",\"a\":[2,\"~[sym1]\",4,\"~[sym2]\"]}";
        String actual = "{\"a\":[5,4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("5", symbols.get("sym1"));
        assertEquals("3", symbols.get("sym2"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareJsonArrayWithAssignSymbols_negative() throws InvalidTypeException {
        String expected = "{\"b\":\"val1\",\"a\":[\"~[sym1]\",2,3,5]}";
        String actual = "{\"a\":[5,4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        assertThrows(AssertionError.class, matcher::match);
    }

    @Test
    public void compareJsonWithAssignSymbolsAndDoNotFind() throws InvalidTypeException {
        String expected = "{\"b\":\"!t~[sym1]1\"}";
        String actual = "{\"a\":[5,4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void compareJsonWithAssignSymbolsAndDoNotFind_negative() throws InvalidTypeException {
        String expected = "{\"b\":\"!v~[sym1]1\"}";
        String actual = "{\"a\":[5,4,3,2,1],\"b\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        assertThrows(AssertionError.class, matcher::match);
    }

    @Test
    public void compareBigJsonWithAssignSymbols() throws InvalidTypeException {
        String expected = "[\n" + "  {\n" + "    \"_id\": \"5b4fa3f8c2741fde34e4d5c8\",\n"
                + "    \"index\": 0,\n" + "    \"latitude\": -73.952152,\n"
                + "    \"longitude\": \"~[longitude]\",\n" + "    \"tags\": [\n"
                + "      \"irure\",\n" + "      \"et\",\n" + "      \"ex\",\n"
                + "      \"fugiat\",\n" + "      \"aute\",\n" + "      \"laboris\",\n"
                + "      \"sit\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n"
                + "        \"id\": \"~[friendId]\",\n" + "        \"name\": \"Meagan Martinez\"\n"
                + "      },\n" + "      {\n" + "        \"id\": 2,\n"
                + "        \"name\": \"Sloan Yang\"\n" + "      }\n" + "    ],\n"
                + "    \"greeting\": \"Hello, Holly Hawkins! You have 1 unread messages.\",\n"
                + "    \"favoriteFruit\": \"banana\"\n" + "  }\n" + "]";
        String actual = "[\n" + "  {\n" + "    \"_id\": \"5b4fa3f8c2741fde34e4d5c8\",\n"
                + "    \"index\": 0,\n"
                + "    \"guid\": \"6bf5b919-ec09-444b-b9ec-fff820b9c591\",\n"
                + "    \"isActive\": false,\n" + "    \"balance\": \"$3,756.68\",\n"
                + "    \"picture\": \"http://placehold.it/32x32\",\n" + "    \"age\": 27,\n"
                + "    \"eyeColor\": \"brown\",\n" + "    \"name\": \"Holly Hawkins\",\n"
                + "    \"gender\": \"female\",\n" + "    \"company\": \"FUELTON\",\n"
                + "    \"email\": \"hollyhawkins@fuelton.com\",\n"
                + "    \"phone\": \"+1 (997) 554-3416\",\n"
                + "    \"address\": \"825 Powers Street, Noxen, Iowa, 7981\",\n"
                + "    \"about\": \"Ullamco sunt ex reprehenderit velit tempor nulla exercitation laborum consectetur ullamco veniam. Veniam est aliqua deserunt excepteur. Veniam fugiat laboris esse dolor deserunt. Reprehenderit sit velit anim laborum fugiat veniam occaecat exercitation occaecat commodo in quis sunt. Tempor mollit excepteur nulla voluptate aliqua sunt velit pariatur deserunt.\\r\\n\",\n"
                + "    \"registered\": \"2015-05-28T07:50:30 -03:00\",\n"
                + "    \"latitude\": -73.952152,\n" + "    \"longitude\": -90.447286,\n"
                + "    \"tags\": [\n" + "      \"irure\",\n" + "      \"et\",\n" + "      \"ex\",\n"
                + "      \"fugiat\",\n" + "      \"aute\",\n" + "      \"laboris\",\n"
                + "      \"sit\"\n" + "    ],\n" + "    \"friends\": [\n" + "      {\n"
                + "        \"id\": 0,\n" + "        \"name\": \"Jodie Gaines\"\n" + "      },\n"
                + "      {\n" + "        \"id\": 1,\n" + "        \"name\": \"Meagan Martinez\"\n"
                + "      },\n" + "      {\n" + "        \"id\": 2,\n"
                + "        \"name\": \"Sloan Yang\"\n" + "      }\n" + "    ],\n"
                + "    \"greeting\": \"Hello, Holly Hawkins! You have 1 unread messages.\",\n"
                + "    \"favoriteFruit\": \"banana\"\n" + "  }\n" + "]";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("1", symbols.get("friendId"));
        assertEquals("-90.447286", symbols.get("longitude"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareVeryBigJsonWithAssignSymbols() throws IOException, InvalidTypeException {
        String expected = ResourceUtils.read("props/bigJsons/expectedLargeJson.json");
        String actual = ResourceUtils.read("props/bigJsons/actualLargeJson.json");
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("67360d7500f9e9df142af8d0ad645d15afff84ab63d0e3517476a6873feca9e1_1582022731479", symbols.get("proof_of_token"));
    }

    @Test
    public void compareVeryBigJsonWithAssignSymbols_negative() throws IOException, InvalidTypeException {
        String expected = "{\"status\":200,\"body\":{\"events\":[{\"payloadInvalid\":{}}]}}";
        String actual = ResourceUtils.read("props/bigJsons/actualLargeJson.json");
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        AssertionError assertionError = assertThrows(AssertionError.class, matcher::match);
        assertTrue(assertionError.getMessage().matches("(?s).*\"payloadInvalid\".*JSONs do not match.*"));
    }

    @Test
    public void checkMessageFromSimpleJsonCompare() throws InvalidTypeException {
        String expected = "{\"a\":\"val2\",\"c\":\"val1\",\"!.*\":\".*\"}";
        String actual = "{\"a\":\"val2\",\"c\":\"val1\",\"d\":\"val1\"}";
        JsonMatcher matcher = new JsonMatcher("some msg", expected, actual, null);
        assertThrows(AssertionError.class, () -> {
            try {
                matcher.match();
            } catch (AssertionError e) {
                assertTrue(e.getMessage().contains("some msg") && e.getMessage().contains("Expected:"));
                throw e;
            }
        });
    }

    @Test
    public void testPlaceholderFillFromJsonCompareWithRegexSymbols() throws InvalidTypeException {
        String expected = "{\n" +
                "  \"/processes/running/ote_company/test-create-1551172176725.com/emailverification.*\": {\n" +
                "    \"businessKey\": \"tes.*reate-~[businessKey]\",\n" +
                "    \"type\": \"EMAIL_VERIFICATION\"\n" +
                "  }\n" +
                "}";
        String actual = "{\n" +
                "  \"/processes/running/ote_company/test-create-1551172176725.com/emailverification/3edb8eeb-b4e2-4b57-a6af-927fc1807b8e\": {\n" +
                "    \"businessKey\": \"test-create-1551172176725.com|email-verif|1d55b4f3-6ec1-4d89-ba58-2ba2a3eaa80e\",\n" +
                "    \"type\": \"EMAIL_VERIFICATION\"\n" +
                "  }\n" +
                "}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual, null);
        Map<String, Object> symbols = matcher.match();
        assertEquals("1551172176725.com|email-verif|1d55b4f3-6ec1-4d89-ba58-2ba2a3eaa80e", symbols.get("businessKey"),
                "Actual symbol result: " + symbols.get("businessKey"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void doNotMatchJsons() throws InvalidTypeException {
        String expected = "{\"a\":1}";
        String actual = "{\"a\":2}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void doNotMatchJsons_negative() {
        String expected = "{\"a\":1}";
        String actual = "{\"a\":1, \"b\":2}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))).match());
    }

    @Test
    public void doNotMatchJsons_with_assign_symbols() throws InvalidTypeException {
        String expected = "{\"a\":\"~[val]\"}";
        String actual = "{\"b\":2}";
        Map<String, Object> props = new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchNonExtensibleJsons() throws InvalidTypeException {
        String expected = "{\"a\":1}";
        String actual = "{\"a\":1, \"b\":2}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void doNotMatchNonExtensibleJsons_negative() {
        String expected = "{\"b\":2, \"a\":1}";
        String actual = "{\"a\":1, \"b\":2}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.DO_NOT_MATCH))).match());
    }

    @Test
    public void matchNonExtensibleJsonArrays() throws InvalidTypeException {
        String expected = "{\"a\":{\"b\":[1, true]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true]}}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY))).match();
    }

    @Test
    public void matchNonExtensibleJsonArrays_negative() {
        String expected = "{\"a\":{\"b\":[1]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true]}}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY))).match());
    }

    @Test
    public void doNotMatchNonExtensibleJsonArrays() throws InvalidTypeException {
        String expected = "{\"a\":{\"b\":[1]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true]}}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY, MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void doNotMatchNonExtensibleJsonArrays_negative() {
        String expected = "{\"a\":{\"b\":[1, true]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true]}}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY, MatchCondition.DO_NOT_MATCH))).match());
    }

    @Test
    public void matchJsonArraysStrictOrder() throws InvalidTypeException {
        String expected = "{\"a\":{\"b\":[1, true]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true, false]}}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY))).match();
    }

    @Test
    public void matchJsonArraysStrictOrder_negative() {
        String expected = "{\"a\":{\"b\":[1, false]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true, false]}}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY))).match());
    }

    @Test
    public void doNotMatchJsonArraysStrictOrder() throws InvalidTypeException {
        String expected = "{\"a\":{\"b\":[1, false]}}";
        String actual = "{\"a\":{\"c\":0, \"b\":[1, true, false]}}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY,
                MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void matchJsonNonExtensibleAndArraysStrictOrder() throws InvalidTypeException {
        String expected = "{\"a\":{\"d\":null, \"b\":[1, true]}, \"c\":0}";
        String actual = "{\"c\":0, \"a\":{\"b\":[1, true], \"d\":null}}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY,
                MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_NON_EXTENSIBLE_ARRAY))).match();
    }

    @Test
    public void matchJsonNonExtensibleAndArraysStrictOrder_negative() {
        String expected = "{\"a\":{\"d\":null, \"b\":[1, true]}, \"c\":0}";
        String actual = "{\"c\":0, \"a\":{\"b\":[true, 1], \"d\":null}}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY,
                        MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_NON_EXTENSIBLE_ARRAY))).match());
    }

    @Test
    public void doNotMatchJsonNonExtensibleAndArraysStrictOrder() throws InvalidTypeException {
        String expected = "{\"a\":{\"d\":null, \"b\":[1, true]}, \"c\":0}";
        String actual = "{\"c\":0, \"a\":{\"b\":[1, true], \"d\":null, \"e\":null}}";
        new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY,
                MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_NON_EXTENSIBLE_ARRAY, MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void doNotMatchJsonNonExtensibleAndArraysStrictOrder_negative() {
        String expected = "{\"a\":{\"d\":null, \"b\":[1, true]}, \"c\":0}";
        String actual = "{\"c\":0, \"a\":{\"b\":[1, true], \"d\":null}}";
        assertThrows(AssertionError.class, () ->
                new JsonMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_STRICT_ORDER_ARRAY,
                        MatchCondition.JSON_NON_EXTENSIBLE_OBJECT, MatchCondition.JSON_NON_EXTENSIBLE_ARRAY, MatchCondition.DO_NOT_MATCH))).match());
    }

    @Test
    public void matchHttpResponsesInJsonFormat() throws InvalidTypeException {
        String expected = "{\n" +
                "  \"status\": 202,\n" +
                "  \"body\": {\n" +
                "    \"id\": \"~[ipBlockId]\",\n" +
                "    \"properties\": {\n" +
                "      \"ips\": [\n" +
                "        \"~[ipAddress1]\",\n" +
                "        \"~[ipAddress2]\"\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"headers\": {\n" +
                "    \"Location\": \".*requests/~[requestId]/status.*\"\n" +
                "  }\n" +
                "}";
        String actual = "{\n" +
                "  \"status\": 202,\n" +
                "  \"body\": {\n" +
                "    \"id\": \"ef0698ab-28d4-47b7-bc39-3f07a3452671\",\n" +
                "    \"type\": \"ipblock\",\n" +
                "    \"metadata\": {\n" +
                "      \"etag\": \"46ec48c011c4e48ffe3c1f9e78bdd30f\",\n" +
                "      \"createdDate\": \"2021-04-14T08:20:55Z\",\n" +
                "      \"lastModifiedDate\": \"2021-04-14T08:20:55Z\",\n" +
                "      \"lastModifiedByUserId\": \"a5e7e4e2-d788-4a23-861a-63f144518063\",\n" +
                "      \"state\": \"BUSY\"\n" +
                "    },\n" +
                "    \"properties\": {\n" +
                "      \"ips\": [\n" +
                "        \"87.106.0.246\",\n" +
                "        \"87.106.0.247\"\n" +
                "      ],\n" +
                "      \"location\": \"de/fkb\",\n" +
                "      \"size\": 2,\n" +
                "      \"name\": \"IP Block Cucumber\",\n" +
                "      \"ipConsumers\": []\n" +
                "    }\n" +
                "  },\n" +
                "  \"headers\": {\n" +
                "    \"Location\": \".*requests/someRequestId1000/status.*\"\n" +
                "  }\n" +
                "}";
        Map<String, Object> props = new JsonMatcher(null, expected, actual, null).match();
        assertEquals("ef0698ab-28d4-47b7-bc39-3f07a3452671", props.get("ipBlockId"));
        assertEquals("87.106.0.246", props.get("ipAddress1"));
        assertEquals("87.106.0.247", props.get("ipAddress2"));
        assertEquals("someRequestId1000", props.get("requestId"));
    }

    @Test
    public void compareJsonWithDisabledRegex() throws InvalidTypeException {
        String expected = "{\"b\":\"(?=test) ~[sym1]\"}";
        String actual = "{\"a\":1, \"b\":\"(?=test) me\"}";
        JsonMatcher matcher = new JsonMatcher(null, expected, actual,
                new HashSet<>(Collections.singletonList(MatchCondition.REGEX_DISABLED)));
        Map<String, Object> symbols = matcher.match();
        assertEquals("me", symbols.get("sym1"));
        assertEquals(1, symbols.size());

        assertThrows(AssertionError.class, () -> new JsonMatcher(null, expected, actual, null).match());
    }
}
