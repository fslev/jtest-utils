package io.jtest.utils.clients.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class HttpClientTest {

    private final static Logger LOG = LogManager.getLogger(HttpClientTest.class);

    @Test
    public void testNonEmptyHeaderConstruct() {
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
    public void testDuplicatedHeadersConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.POST);
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
    public void testHeaderOverrideConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.PUT);
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
    public void testNonEmptyHeaderOverrideConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.DELETE);
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
    public void testHeadersOverrideConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.PATCH);
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
    public void testNonEmptyQueryParamConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.OPTIONS);
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
    public void testNullQueryParamsAndNullHeadersConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("test").method(Method.TRACE);
        builder.queryParams(null);
        builder.headers(null);
        assertTrue(builder.build().getHeaders().isEmpty());
        assertFalse(builder.build().getUri().contains("?"));
    }

    @Test
    public void testUnencodedUriPathConstruct() {
        HttpClient.Builder builder = new HttpClient.Builder().address("https://some-address.io").path("/%2F/test?a=12")
                .method(Method.GET).queryParam("b", "%2Ftest1").queryParam("c", "test2%2F");
        HttpClient client = builder.build();
        assertEquals("https://some-address.io/%2F/test?a=12&b=%252Ftest1&c=test2%252F", client.getUri());
    }

    @Test
    public void testTimeoutConstruct() {
        HttpClient client = new HttpClient.Builder().address("http://google.ro").method(Method.GET).timeout(11).build();
        assertEquals(11, client.getTimeout());
    }

    @Test
    public void testProxyConstruct() {
        HttpClient client = new HttpClient.Builder().address("http://google.ro")
                .method(Method.GET).proxy("localhost", 8000, "https").build();
        assertEquals("localhost", client.getProxyHost().getHostName());
        assertEquals(8000, client.getProxyHost().getPort());
        assertEquals("https", client.getProxyHost().getSchemeName());
    }

    @Test
    public void testServiceRetryStrategy() throws IOException {
        HttpClient.Builder builder = new HttpClient.Builder()
                .address("http://www.google.com")
                .method(Method.GET)
                .header("some header", "test")
                .serviceUnavailableRetryStrategy(new ServiceUnavailableRetryStrategy() {
                    @Override
                    public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
                        String content = null;
                        HttpEntity entity = response.getEntity();
                        if (entity == null) {
                            return true;
                        }
                        try {
                            LOG.info("SERVICE retry: {}", executionCount);
                            if (entity != null) {
                                content = EntityUtils.toString(entity);
                            }
                            return !content.equals("") && executionCount < 3;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } finally {
                            try {
                                EntityUtils.consume(entity);
                                if (content != null) {
                                    response.setEntity(new StringEntity(content));

                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }

                    @Override
                    public long getRetryInterval() {
                        return 3000;
                    }
                });
        HttpClient client = builder.build();
        LOG.info(EntityUtils.toString(client.execute().getEntity()));
        client.close();
    }

    @Test
    public void testMissingAddressConstruct() {
        assertTrue(assertThrows(IllegalStateException.class, () -> new HttpClient.Builder().method(Method.GET).build())
                .getMessage().contains("HTTP Address missing"));
    }

    @Test
    public void testMissingMethodConstruct() {
        assertTrue(assertThrows(IllegalStateException.class, () -> new HttpClient.Builder().address("http://localhost")
                .build())
                .getMessage().contains("HTTP Method missing"));
    }

    @Test
    public void testInvalidAddressConstruct() {
        assertTrue(assertThrows(RuntimeException.class, () -> new HttpClient.Builder()
                .address("https://test@#$%^&*()_+").method(Method.GET).build())
                .getMessage().contains("URISyntaxException"));
    }

    @Test
    public void testDeleteMethodWithEntityConstruct() {
        HttpClient client = new HttpClient.Builder().address("https://locahost").method(Method.DELETE)
                .entity("some content").build();
        assertEquals("some content", client.getRequestEntity());
    }
}
