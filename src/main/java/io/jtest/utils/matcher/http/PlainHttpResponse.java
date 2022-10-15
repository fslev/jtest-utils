package io.jtest.utils.matcher.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class PlainHttpResponse {
    @JsonProperty(value = "status")
    private Object status;
    @JsonProperty(value = "body")
    private Object entity;
    @JsonProperty(value = "reason")
    private String reasonPhrase;
    @JsonProperty(value = "headers")
    private List<Map.Entry<String, String>> headers;

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public List<Map.Entry<String, String>> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Map.Entry<String, String>> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "{" +
                (status != null ? "status=" + status : "") +
                (reasonPhrase != null ? ", reason='" + reasonPhrase + '\'' : "") +
                (entity != null ? ", body='" + entity + '\'' : "") +
                (headers != null ? ", headers=" + headers : "") +
                '}';
    }

    public static class ParseException extends RuntimeException {
        public final static String EXPECTED_FORMAT = "{" + System.lineSeparator() +
                "  \"status\": <number> | \"<text>\"," + System.lineSeparator() +
                "  \"body\": <object>," + System.lineSeparator() +
                "  \"headers\": [{\"<name>\":<value>}, ...]," + System.lineSeparator() +
                "  \"reason\": \"<text>\"" + System.lineSeparator() +
                "}";

        public ParseException(String msg) {
            this(msg, null);
        }

        public ParseException(String msg, Throwable t) {
            super(msg + System.lineSeparator() + "Expected format:" + System.lineSeparator() + EXPECTED_FORMAT, t);
        }
    }
}
