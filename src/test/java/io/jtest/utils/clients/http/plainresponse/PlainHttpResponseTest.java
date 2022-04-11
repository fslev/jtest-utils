package io.jtest.utils.clients.http.plainresponse;

import io.jtest.utils.clients.http.PlainHttpResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PlainHttpResponseTest {

    @Test
    public void testPlainResponseInitFromString() throws Exception {
        String content = "{\"status\":200,\"reason\":\"some thing\",\"body\":{\"wa\":[1,2,3,4]}}";
        PlainHttpResponse response = PlainHttpResponse.from(content);
        assertEquals("200", response.getStatus());
        assertEquals("some thing", response.getReasonPhrase());
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("wa", Arrays.asList(1, 2, 3, 4));
        assertEquals(expectedMap, response.getEntity());
        assertNull(response.getHeaders());
    }

    @Test
    public void testPlainResponseInitFromStringStatus() throws Exception {
        String content = "{\"status\":\"200\"}";
        PlainHttpResponse response = PlainHttpResponse.from(content);
        assertEquals("200", response.getStatus());
        assertNull(response.getHeaders());
    }

    @Test
    public void testPlainResponseInitFromEmptyString() {
        String content = "";
        assertThrows(Exception.class, () -> PlainHttpResponse.from(content));
    }

    @Test
    public void testPlainResponseInitFromEmptyJsonString() throws Exception {
        String content = "{}";
        PlainHttpResponse response = PlainHttpResponse.from(content);
        assertNull(response.getStatus());
        assertNull(response.getReasonPhrase());
        assertNull(response.getEntity());
        assertNull(response.getHeaders());
    }

    @Test
    public void testPlainResponseInitFromOtherJsonString() {
        String content = "{\"reasonPhrase\":\"test\"}";
        assertThrows(Exception.class, () -> PlainHttpResponse.from(content));
    }

    @Test
    public void testPlainResponseInitFromOtherJsonStringMessage() {
        String content = "{\"reasonPhrase\":\"test\"}";
        try {
            PlainHttpResponse.from(content);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Object should either be of type org.apache.http.HttpPResponse" +
                            " or of any other JSON convertible types with the format:"),
                    e.getMessage());
        }
    }

    @Test
    public void testPlainResponseInitFromMap() throws Exception {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("status", 200);
        expectedMap.put("reason", "some reason");
        expectedMap.put("body", new int[]{2, 3, 4});
        PlainHttpResponse response = PlainHttpResponse.from(expectedMap);
        assertEquals("200", response.getStatus());
        assertEquals("some reason", response.getReasonPhrase());
        assertEquals(Arrays.asList(2, 3, 4), response.getEntity());
        assertNull(response.getHeaders());
    }

    @Test
    public void testPlainResponseInitFromPlainHttpResponse() throws Exception {
        PlainHttpResponse response1 = PlainHttpResponse.from("{\"status\":200,\"body\":\"test\"}");
        PlainHttpResponse response2 = PlainHttpResponse.from(response1);
        assertEquals("200", response2.getStatus());
        assertNull(response2.getReasonPhrase());
        assertEquals("test", response2.getEntity());
        assertNull(response2.getHeaders());
    }

    @Test
    public void testPlainResponseInitFromHttpResponse() throws Exception {
        HttpResponse mock = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "some reason"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        mock.setEntity(new StringEntity("{\"a\":100}"));
        mock.setHeader(new BasicHeader("Content-Type", "application/json"));
        mock.setHeader(new BasicHeader("Accept", "application/json"));
        PlainHttpResponse response = PlainHttpResponse.from(mock);
        assertEquals("200", response.getStatus());
        assertEquals("some reason", response.getReasonPhrase());
        assertEquals("{\"a\":100}", response.getEntity());
        Set<Map.Entry<String, Object>> entrySet = new HashSet<>();
        entrySet.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        entrySet.add(new AbstractMap.SimpleEntry<>("Accept", "application/json"));
        assertEquals(entrySet, response.getHeaders());
    }
}
