package io.jtest.utils.clients.http;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


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
    public void testHeaderOverride() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.header("test1", "0", true);
        builder.header("test1", "1");
        HttpClient client = builder.build();
        assertEquals(2, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            try {
                assertEquals("0", header.getValue());
            } catch (AssertionError e) {
                assertEquals("1", header.getValue());
            }
        });

        builder.header("test1", "2", true);
        client = builder.build();
        assertEquals(1, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            assertEquals("2", header.getValue());
        });
    }

    @Test
    public void testNonEmptyHeaderOverride() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.nonEmptyHeader("test1", "0", true);
        builder.nonEmptyHeader("test1", "1");
        HttpClient client = builder.build();
        assertEquals(2, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            try {
                assertEquals("0", header.getValue());
            } catch (AssertionError e) {
                assertEquals("1", header.getValue());
            }
        });

        builder.nonEmptyHeader("test1", "2", true);
        client = builder.build();
        assertEquals(1, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            assertEquals("2", header.getValue());
        });
    }

    @Test
    public void testHeadersOverride() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.GET);
        builder.header("test1", "0");
        Map<String, String> headers = new HashMap<>();
        headers.put("test1", "1");
        builder.headers(headers);
        HttpClient client = builder.build();
        assertEquals(2, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            try {
                assertEquals("0", header.getValue());
            } catch (AssertionError e) {
                assertEquals("1", header.getValue());
            }
        });

        headers.clear();
        headers.put("test1", "2");
        builder.headers(headers, true);
        client = builder.build();
        assertEquals(1, client.getHeaders().size());
        builder.build().getHeaders().forEach(header -> {
            assertEquals("test1", header.getName());
            assertEquals("2", header.getValue());
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

    @Test
    public void testUnencodedUriPath() {
        HttpClient.Builder builder = new HttpClient.Builder().address("https://some-address.io").path("/%2F/test?a=12")
                .method(Method.GET).queryParam("b", "%2Ftest1").queryParam("c", "test2%2F");
        HttpClient client = builder.build();
        assertEquals("https://some-address.io/%2F/test?a=12&b=%252Ftest1&c=test2%252F", client.getUri());
    }
}
