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
        HttpClient client = builder.build();
        assertEquals(1, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            assertEquals("1", header.getValue());
        });
    }

    @Test
    public void testDuplicatedHeaders() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.nonEmptyHeader("test1", "1");
        builder.header("test1", "2");
        HttpClient client = builder.build();
        assertEquals(2, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            try {
                assertEquals("1", header.getValue());
            } catch (AssertionError e) {
                assertEquals("2", header.getValue());
            }
        });
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

    @Test
    public void testNullQueryParamsAndNullHeaders() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.queryParams(null);
        builder.headers(null);
        assertTrue(builder.build().getHeaders().isEmpty());
        assertFalse(builder.build().getUri().contains("?"));
    }
}
