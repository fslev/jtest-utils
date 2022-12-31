package io.jtest.utils.matcher.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlainHttpResponse {
    @JsonProperty(value = "status")
    private Object status;
    @JsonProperty(value = "reason")
    private String reasonPhrase;
    @JsonProperty(value = "body")
    private Object entity;
    @JsonProperty(value = "headers")
    private List<Map.Entry<String, String>> headers;

    public PlainHttpResponse() {
    }

    private PlainHttpResponse(Object status, String reasonPhrase, Object entity, List<Map.Entry<String, String>> headers) {
        this.status = status;
        this.reasonPhrase = reasonPhrase;
        this.entity = entity;
        this.headers = headers;
    }

    public Object getStatus() {
        return status;
    }

    public Object getEntity() {
        return entity;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public List<Map.Entry<String, String>> getHeaders() {
        return headers;
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

    public static class Builder {
        private Object status;
        private String reasonPhrase;
        private Object entity;
        private List<Map.Entry<String, String>> headers;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public Builder status(Object status) {
            this.status = status;
            return this;
        }

        public Builder reasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
            return this;
        }

        public Builder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        public Builder headers(List<Map.Entry<String, String>> headers) {
            this.headers = Collections.unmodifiableList(headers);
            return this;
        }

        public PlainHttpResponse build() {
            return new PlainHttpResponse(status, reasonPhrase, entity, headers);
        }
    }

    public static class ParseException extends RuntimeException {
        public static final String EXPECTED_FORMAT = "{" + System.lineSeparator() +
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
