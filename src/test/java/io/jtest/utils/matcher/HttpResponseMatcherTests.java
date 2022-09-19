package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HttpResponseMatcherTests {


    @Test
    public void convertHttpResponse_negative() {
        String expected = "{\"statuses\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        assertThrows(InvalidTypeException.class, () -> new HttpResponseMatcher(null, expected, actual, null).match());
    }

    @Test
    public void matchSimpleHttpResponses() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual, null).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void matchSimpleHttpResponses_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "2");
        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual, null).match());
    }

    @Test
    public void doNotMatchHttpResponsesByStatus() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 201, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void doNotMatchHttpResponsesByStatus_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        assertTrue(assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match())
                .getMessage().contains("HTTP Response statuses match"));
    }

    @Test
    public void doNotMatchHttpResponsesByBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem other\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, expected, actual,
                        new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByBody_negative1() throws UnsupportedEncodingException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem other\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByStatusAndBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem other\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY,
                        MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByStatusAndBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem other\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY,
                        MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByReason() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":1}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "ipsum"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON))).match();
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void doNotMatchHttpResponsesByReason_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 400, \"reason\":\"ipsum\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "ipsum"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON))).match())
                .getMessage().contains("HTTP Response reasons match"));
    }

    @Test
    public void doNotMatchHttpResponsesByReason_negative1() throws UnsupportedEncodingException {
        String expected = "{\"status\": 400, \"reason\":\"bad request\",\"body\":{\"a\":\"~[val1] lorem\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "ipsum"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON))).match())
                .getMessage().matches("(?s).*HTTP Response bodies do not match.*JSONs do not match.*"));
    }

    @Test
    public void doNotMatchHttpResponsesByReason_negative2() throws UnsupportedEncodingException {
        String expected = "{\"status\": 400, \"reason\":\"ipsum\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "bad request"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual, null).match())
                .getMessage().contains("HTTP Response reasons do not match"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 400, \"headers\":[{\"auth\":2}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "lorem"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS))).match();
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 400, \"headers\":[{\"x-auth\":2}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "lorem"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS))).match())
                .getMessage().contains("HTTP Response headers match!"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders_negative1() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"x-auth\":3}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "lorem"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS))).match())
                .getMessage().contains("HTTP Response statuses do not match"));
    }

    @Test
    public void doNotMatchHttpResponsesByHeaders_negative2() throws UnsupportedEncodingException {
        String expected = "{\"status\": 400, \"headers\":[{\"x-auth\":2}], \"reason\":\"lorem\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "lorem"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "3");
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual, null).match())
                .getMessage().contains("HTTP Response headers do not match!"));
    }

    @Test
    public void matchHttpResponsesByJsonNonExtensibleObjectBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT))).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }


    @Test
    public void matchHttpResponsesByJsonNonExtensibleObjectBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\", \"b\":0}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        assertThrows(AssertionError.class, () ->
                new HttpResponseMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByJsonNonExtensibleObjectBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\", \"b\":0}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT,
                MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByJsonNonExtensibleObjectBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\":{\"a\":\"~[val1] ipsum\"}}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("{\"a\":\"lorem ipsum\"}"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_OBJECT,
                MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match()).getMessage().contains("HTTP Response bodies match!"));
    }

    @Test
    public void matchHttpResponsesByStringBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\": \"~[val1] ipsum\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("lorem ipsum"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual, null).match();
        assertEquals(1, props.size());
        assertEquals("lorem", props.get("val1"));
    }

    @Test
    public void matchHttpResponsesWithDuplicatedHeaderNames() throws InvalidTypeException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"auth\":2}]}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.addHeader("auth", "1");
        actual.addHeader("auth", "2");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual, null).match();
    }

    @Test
    public void matchHttpResponsesByStringBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"status\": 200, \"headers\":[{\"auth\":1},{\"x-auth\":2}], \"reason\":\"test\",\"body\": \"~[val1] ipsum\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("lorem other"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        AssertionError error = assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual, null).match());
        assertTrue(error.getMessage().matches(("(?s).*HTTP Response bodies do not match!.*Strings do not match.*\\Q~[val1]\\E ipsum.*lorem other.*")));
    }

    @Test
    public void matchHttpResponsesByXMLBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\">ipsum</a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual, null).match();
        assertEquals(2, props.size());
        assertEquals("lorem", props.get("attrVal"));
        assertEquals("ipsum", props.get("val"));
    }

    @Test
    public void matchHttpResponsesByXMLBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a2 attr=\"lorem\" type=\"text\">ipsum</a2><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        assertTrue(assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual, null).match())
                .getMessage().matches("(?s).*HTTP Response bodies do not match.*XMLs do not match.*"));
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\"><a2>0</a2></a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody_negative() throws UnsupportedEncodingException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\">0</a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }

    @Test
    public void matchHttpResponsesByXMLBody_with_ChildNodesLength() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1><b>null</b></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\">ipsum</a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match();
        assertEquals(2, props.size());
        assertEquals("lorem", props.get("attrVal"));
        assertEquals("ipsum", props.get("val"));
    }

    @Test
    public void matchHttpResponsesByXMLBody_with_ChildNodesLength_negative() throws UnsupportedEncodingException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\">ipsum</a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match());
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody_with_ChildNodesLength() throws UnsupportedEncodingException, InvalidTypeException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\">ipsum</a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        Map<String, Object> props = new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH, MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match();
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchHttpResponsesByXMLBody_with_ChildNodesLength_negative() throws UnsupportedEncodingException {
        String expected = "{\"headers\":[{\"auth\":1},{\"x-auth\":2}],\"body\": \"<a><a1 attr=\\\"~[attrVal]\\\">~[val]</a1><b>null</b></a>\"}";
        HttpResponse actual = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "test"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        actual.setEntity(new StringEntity("<a><a1 attr=\"lorem\" type=\"text\">ipsum</a1><b>null</b></a>"));
        actual.addHeader("auth", "1");
        actual.addHeader("x-auth", "2");
        actual.addHeader("Authorization", "Bzasuiofrz====");
        assertThrows(AssertionError.class, () -> new HttpResponseMatcher(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH, MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY))).match());
    }
}
