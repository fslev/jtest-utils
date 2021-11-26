package io.jtest.utils.clients.http.httpresponsewrapper;

import io.jtest.utils.clients.http.wrappers.HttpResponseWrapper;
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

public class HttpResponseWrapperTest {

    @Test
    public void testWrapperInitFromString() throws Exception {
        String content = "{\"status\":200,\"reason\":\"some thing\",\"body\":{\"wa\":[1,2,3,4]}}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(content);
        assertEquals("200", wrapper.getStatus());
        assertEquals("some thing", wrapper.getReasonPhrase());
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("wa", Arrays.asList(1, 2, 3, 4));
        assertEquals(expectedMap, wrapper.getEntity());
        assertNull(wrapper.getHeaders());
    }

    @Test
    public void testWrapperInitFromStringStatus() throws Exception {
        String content = "{\"status\":\"200\"}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(content);
        assertEquals("200", wrapper.getStatus());
        assertNull(wrapper.getHeaders());
    }

    @Test
    public void testWrapperInitFromEmptyString() throws Exception {
        String content = "";
        assertThrows(Exception.class, () -> new HttpResponseWrapper(content));
    }

    @Test
    public void testWrapperInitFromEmptyJsonString() throws Exception {
        String content = "{}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(content);
        assertNull(wrapper.getStatus());
        assertNull(wrapper.getReasonPhrase());
        assertNull(wrapper.getEntity());
        assertNull(wrapper.getHeaders());
    }

    @Test
    public void testWrapperInitFromOtherJsonString() throws Exception {
        String content = "{\"reasonPhrase\":\"test\"}";
        assertThrows(Exception.class, () -> new HttpResponseWrapper(content));
    }

    @Test
    public void testWrapperInitFromOtherJsonStringMessage() {
        String content = "{\"reasonPhrase\":\"test\"}";
        try {
            new HttpResponseWrapper(content);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Object should be convertible to io.jtest.utils.clients.http.wrappers.HttpResponseWrapper type"),
                    e.getMessage());
        }
    }

    @Test
    public void testWrapperInitFromMap() throws Exception {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("status", 200);
        expectedMap.put("reason", "some reason");
        expectedMap.put("body", new int[]{2, 3, 4});
        HttpResponseWrapper wrapper = new HttpResponseWrapper(expectedMap);
        assertEquals("200", wrapper.getStatus());
        assertEquals("some reason", wrapper.getReasonPhrase());
        assertEquals(Arrays.asList(2, 3, 4), wrapper.getEntity());
        assertNull(wrapper.getHeaders());
    }

    @Test
    public void testWrapperInitFromHttpResponseWrapper() throws Exception {
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        wrapper.setHeaders(null);
        wrapper.setEntity("test");
        wrapper.setStatus("200");
        HttpResponseWrapper wrapper1 = new HttpResponseWrapper(wrapper);
        assertEquals("200", wrapper1.getStatus());
        assertNull(wrapper1.getReasonPhrase());
        assertEquals("test", wrapper1.getEntity());
        assertNull(wrapper1.getHeaders());
    }

    @Test
    public void testWrapperInitFromHttpResponse() throws Exception {
        HttpResponse mock = new DefaultHttpResponseFactory()
                .newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "some reason"),
                        HttpClientContext.adapt(new BasicHttpContext()));
        mock.setEntity(new StringEntity("{\"a\":100}"));
        mock.setHeader(new BasicHeader("Content-Type", "application/json"));
        mock.setHeader(new BasicHeader("Accept", "application/json"));
        HttpResponseWrapper wrapper = new HttpResponseWrapper(mock);
        assertEquals("200", wrapper.getStatus());
        assertEquals("some reason", wrapper.getReasonPhrase());
        assertEquals("{\"a\":100}", wrapper.getEntity());
        Set<Map.Entry<String, Object>> entrySet = new HashSet<>();
        entrySet.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        entrySet.add(new AbstractMap.SimpleEntry<>("Accept", "application/json"));
        assertEquals(entrySet, wrapper.getHeaders());
    }
}
