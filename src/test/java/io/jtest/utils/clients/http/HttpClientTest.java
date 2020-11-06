package io.jtest.utils.clients.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpClientTest {
    @Test
    public void testNonEmptyHeader() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.addNonEmptyHeader("test1", "1");
        builder.addNonEmptyHeader("test2", "");
        builder.addNonEmptyHeader("test3", null);
        assertEquals("1", builder.build().getHeaders().get("test1"));
        assertFalse(builder.build().getHeaders().containsKey("test2"));
        assertFalse(builder.build().getHeaders().containsKey("test3"));
    }

    @Test
    public void testNonEmptyQueryParam() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.addQueryParam("test1", "1");
        builder.addNonEmptyQueryParam("test2", "");
        builder.addNonEmptyQueryParam("test3", null);
        builder.setNonEmptyQueryParam("test4", "1");
        builder.setNonEmptyQueryParam("test5", "");
        builder.setNonEmptyQueryParam("test6", null);
        assertTrue(builder.build().getUri().contains("test1=1"));
        assertFalse(builder.build().getUri().contains("test2"));
        assertFalse(builder.build().getUri().contains("test3"));
        assertTrue(builder.build().getUri().contains("test4=1"));
        assertFalse(builder.build().getUri().contains("test5"));
        assertFalse(builder.build().getUri().contains("test6"));
    }
}
