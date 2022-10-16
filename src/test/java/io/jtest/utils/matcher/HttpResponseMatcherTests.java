package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.jtest.utils.PlainHttpResponseUtils.from;
import static org.junit.jupiter.api.Assertions.*;

public class HttpResponseMatcherTests {

    @Test
    public void checkPlainHttpResponseToString() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        assertTrue(from(expected).toString().matches("(?s).*status.*200.*reason.*test.*body.*ipsum.*headers.*auth.*"));
    }

    @Test
    public void matchInvalidHttpResponses() {
        String expected = "{\"status\": 200, \"headers\":{\"auth\":1}, \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";

        PlainHttpResponse actual = new PlainHttpResponse();
        actual.setStatus(200);
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("auth", "1"));
        headers.add(new AbstractMap.SimpleEntry<>("x-auth", "2"));
        actual.setHeaders(headers);
        actual.setEntity("{\"a\":\"lorem ipsum\"}");
        actual.setReasonPhrase("test");

        assertTrue(assertThrows(PlainHttpResponse.ParseException.class, () ->
                new HttpResponseMatcher(null, from(expected), actual, null).match())
                .getMessage().contains(PlainHttpResponse.ParseException.EXPECTED_FORMAT));
    }

    @Test
    public void matchSimpleHttpResponses() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";

        PlainHttpResponse actual = new PlainHttpResponse();
        actual.setStatus(200);
        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("auth", "1"));
        headers.add(new AbstractMap.SimpleEntry<>("x-auth", "2"));
        actual.setHeaders(headers);
        actual.setEntity("{\"a\":\"lorem ipsum\"}");
        actual.setReasonPhrase("test");

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), actual, null).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void matchSimpleHttpResponses_negative() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";

        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("auth", "2"));
        PlainHttpResponse actual = new PlainHttpResponse(200, "test", "{\"a\":\"lorem ipsum\"}", headers);

        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), actual, null).match());
    }

    @Test
    public void doNotMatchHttpResponsesByStatus() throws InvalidTypeException {
        String expected = "{\"status\": 201, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem ipsum\"}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void doNotMatchHttpResponsesByStatus_negative() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, from(expected), from(actual),
                        new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match())
                .getMessage().contains("HTTP Response statuses match"));
    }

    @Test
    public void doNotMatchHttpResponsesByBody() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem other\"}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByBody_negative() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem ipsum\"}}";

        assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, from(expected), from(actual),
                        new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByBody_negative1() {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem other\"}}";

        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByStatusAndBody() throws InvalidTypeException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem other\"}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY,
                        MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByStatusAndBody_negative() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem other\"}}";

        assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, from(expected), from(actual), new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY,
                        MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByReason() throws InvalidTypeException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":1}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"reason\":\"ipsum\", \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"body\":{\"a\":\"lorem ipsum\"}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON))).match();
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void doNotMatchHttpResponsesByReason_negative() {
        String expected = "{\"status\": 400, \"reason\":\"ipsum\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"reason\":\"ipsum\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON))).match())
                .getMessage().contains("HTTP Response reasons match"));
    }

    @Test
    public void doNotMatchHttpResponsesByReason_negative1() {
        String expected = "{\"status\": 400, \"reason\":\"bad request\",\"body\":{\"a\":\"~[val1] lorem\"}}";
        String actual = "{\"status\": 400, \"reason\":\"ipsum\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON))).match())
                .getMessage().matches("(?s).*HTTP Response bodies do not match.*JSONs do not match.*"));
    }

    @Test
    public void doNotMatchHttpResponsesByReason_negative2() {
        String expected = "{\"status\": 400, \"reason\":\"ipsum\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"reason\":\"bad request\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                null).match()).getMessage().contains("HTTP Response reasons do not match"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders() throws InvalidTypeException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":2}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"headers\":[{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"lorem\", \"body\":{\"a\":\"lorem ipsum\"}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS))).match();
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders_negative() {
        String expected = "{\"status\": 400, \"headers\":[{\"x-auth\":2}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"headers\":[{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"lorem\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS))).match())
                .getMessage().contains("HTTP Response headers match!"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders_negative1() {
        String expected = "{\"status\": 200, \"headers\":[{\"x-auth\":3}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"headers\":[{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"lorem\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS))).match())
                .getMessage().contains("HTTP Response statuses do not match"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders_negative2() {
        String expected = "{\"status\": 400, \"headers\":[{\"x-auth\":2}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 400, \"headers\":[{\"auth\":1}, {\"x-auth\":3}], \"reason\":\"lorem\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual), null).match())
                .getMessage().contains("HTTP Response headers do not match!"));
    }

    @Test
    public void matchHttpResponsesByJsonNonExtensibleObjectBody() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"test\", \"body\":{\"a\":\"lorem ipsum\"}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual), new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT))).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }


    @Test
    public void matchHttpResponsesByJsonNonExtensibleObjectBody_negative() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"test\", \"body\":{\"a\":\"lorem ipsum\", \"b\":0}}";

        assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, from(expected), from(actual),
                        new HashSet<>(Collections.singletonList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByJsonNonExtensibleObjectBody() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"test\", \"body\":{\"a\":\"lorem ipsum\", \"b\":0}}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT,
                        MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByJsonNonExtensibleObjectBody_negative() {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"test\", \"body\":{\"a\":\"lorem ipsum\"}}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT,
                        MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match()).getMessage().contains("HTTP Response bodies match!"));
    }

    @Test
    public void matchHttpResponsesByStringBody() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\": \"~[val1] ipsum\"}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"test\", \"body\":\"lorem ipsum\"}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual), null).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void matchHttpResponsesWithDuplicatedHeaderNames() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"auth\":2}]}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"auth\":2}], \"reason\":\"test\"}";

        new HttpResponseMatcher(null, from(expected), from(actual), null).match();
    }

    @Test
    public void matchHttpResponsesByStringBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\": \"~[val1] ipsum\"}";
        String actual = "{\"status\": 200, \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"reason\":\"test\", \"body\":\"lorem other\"}";

        AssertionError error = assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual), null).match());
        assertTrue(error.getMessage().matches(("(?s).*HTTP Response bodies do not match!.*Strings do not match.*\\Q~[val1]\\E ipsum.*lorem other.*")));
    }

    @Test
    public void matchHttpResponsesByXMLBody() throws InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\">ipsum</a1><b>null</b></a>\"}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual), null).match();
        assertEquals(2, props.size());
        assertEquals("lorem", props.get("attrVal"));
        assertEquals("ipsum", props.get("val"));
    }

    @Test
    public void matchHttpResponsesByXMLBody_negative() {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}], \"body\":\"<a><a2 attr=\\\"lorem\\\" type=\\\"text\\\">ipsum</a2><b>null</b></a>\"}";

        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual), null).match())
                .getMessage().matches("(?s).*HTTP Response bodies do not match.*XMLs do not match.*"));
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody() throws InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}]," +
                " \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\"><a2>0</a2></a1><b>null</b></a>\"}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody_negative() {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}]," +
                " \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\">0</a1><b>null</b></a>\"}";

        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }

    @Test
    public void matchHttpResponsesByXMLBody_with_ChildNodesLength() throws InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1><b>null</b></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}]," +
                " \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\">ipsum</a1><b>null</b></a>\"}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match();
        assertEquals(2, props.size());
        assertEquals("lorem", props.get("attrVal"));
        assertEquals("ipsum", props.get("val"));
    }

    @Test
    public void matchHttpResponsesByXMLBody_with_ChildNodesLength_negative() {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}]," +
                " \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\">ipsum</a1><b>null</b></a>\"}";

        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Collections.singletonList(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody_with_ChildNodesLength() throws InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}]," +
                " \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\">ipsum</a1><b>null</b></a>\"}";

        Map<String, Object> props = new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH, MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody_with_ChildNodesLength_negative() {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1><b>null</b></a>\"}";
        String actual = "{\"status\": 200, \"reason\":\"test\", \"headers\":[{\"Authorization\":\"Bzasuiofrz====\"},{\"auth\":1}, {\"x-auth\":2}]," +
                " \"body\":\"<a><a1 attr=\\\"lorem\\\" type=\\\"text\\\">ipsum</a1><b>null</b></a>\"}";

        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, from(expected), from(actual),
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH, MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }
}
