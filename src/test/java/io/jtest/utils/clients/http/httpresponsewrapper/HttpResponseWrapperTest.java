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
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertTrue;

public class HttpResponseWrapperTest {

    @Test
    public void testWrapperInitFromString() throws Exception {
        String content = "{\"status\":200,\"reason\":\"some thing\",\"body\":{\"wa\":[1,2,3,4]}}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(content);
        Assert.assertEquals("200", wrapper.getStatus());
        Assert.assertEquals("some thing", wrapper.getReasonPhrase());
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("wa", Arrays.asList(1, 2, 3, 4));
        Assert.assertEquals(expectedMap, wrapper.getEntity());
        Assert.assertNull(wrapper.getHeaders());
    }

    @Test
    public void testWrapperInitFromStringStatus() throws Exception {
        String content = "{\"status\":\"200\"}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(content);
        Assert.assertEquals("200", wrapper.getStatus());
        Assert.assertNull(wrapper.getHeaders());
    }

    @Test(expected = Exception.class)
    public void testWrapperInitFromEmptyString() throws Exception {
        String content = "";
        new HttpResponseWrapper(content);
    }

    @Test
    public void testWrapperInitFromEmptyJsonString() throws Exception {
        String content = "{}";
        HttpResponseWrapper wrapper = new HttpResponseWrapper(content);
        Assert.assertNull(wrapper.getStatus());
        Assert.assertNull(wrapper.getReasonPhrase());
        Assert.assertNull(wrapper.getEntity());
        Assert.assertNull(wrapper.getHeaders());
    }

    @Test(expected = Exception.class)
    public void testWrapperInitFromOtherJsonString() throws Exception {
        String content = "{\"reasonPhrase\":\"test\"}";
        new HttpResponseWrapper(content);
    }

    @Test
    public void testWrapperInitFromOtherJsonStringMessage() {
        String content = "{\"reasonPhrase\":\"test\"}";
        try {
            new HttpResponseWrapper(content);
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Object should be convertible to HttpResponseWrapper type"));
        }
    }

    @Test
    public void testWrapperInitFromMap() throws Exception {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("status", 200);
        expectedMap.put("reason", "some reason");
        expectedMap.put("body", new int[]{2, 3, 4});
        HttpResponseWrapper wrapper = new HttpResponseWrapper(expectedMap);
        Assert.assertEquals("200", wrapper.getStatus());
        Assert.assertEquals("some reason", wrapper.getReasonPhrase());
        Assert.assertEquals(Arrays.asList(2, 3, 4), wrapper.getEntity());
        Assert.assertNull(wrapper.getHeaders());
    }

    @Test
    public void testWrapperInitFromHttpResponseWrapper() throws Exception {
        HttpResponseWrapper wrapper = new HttpResponseWrapper();
        wrapper.setHeaders(null);
        wrapper.setEntity("test");
        wrapper.setStatus("200");
        HttpResponseWrapper wrapper1 = new HttpResponseWrapper(wrapper);
        Assert.assertEquals("200", wrapper1.getStatus());
        Assert.assertNull(wrapper1.getReasonPhrase());
        Assert.assertEquals("test", wrapper1.getEntity());
        Assert.assertNull(wrapper1.getHeaders());
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
        Assert.assertEquals("200", wrapper.getStatus());
        Assert.assertEquals("some reason", wrapper.getReasonPhrase());
        Assert.assertEquals("{\"a\":100}", wrapper.getEntity());
        Set<Map.Entry<String, Object>> entrySet = new HashSet<>();
        entrySet.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        entrySet.add(new AbstractMap.SimpleEntry<>("Accept", "application/json"));
        Assert.assertEquals(entrySet, wrapper.getHeaders());
    }
}
