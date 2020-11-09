package io.jtest.utils.clients.http;

import org.junit.Test;

import static org.junit.Assert.*;

public class HttpClientTest {
    @Test
    public void testNonEmptyHeader() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.nonEmptyHeader("test1", "1");
        builder.nonEmptyHeader("test2", "");
        builder.nonEmptyHeader("test3", null);
        assertEquals("1", builder.build().getHeaders().get("test1"));
        assertFalse(builder.build().getHeaders().containsKey("test2"));
        assertFalse(builder.build().getHeaders().containsKey("test3"));
    }

    @Test
    public void testNonEmptyQueryParam() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.queryParam("test1", "1");
        builder.nonEmptyQueryParam("test2", "");
        builder.nonEmptyQueryParam("test3", null);
        builder.nonEmptyQueryParam("test4", "1");
        builder.nonEmptyQueryParam("test5", "");
        builder.nonEmptyQueryParam("test6", null);
        assertTrue(builder.build().getUri().contains("test1=1"));
        assertFalse(builder.build().getUri().contains("test2"));
        assertFalse(builder.build().getUri().contains("test3"));
        assertTrue(builder.build().getUri().contains("test4=1"));
        assertFalse(builder.build().getUri().contains("test5"));
        assertFalse(builder.build().getUri().contains("test6"));
    }
}
