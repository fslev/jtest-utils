package io.jtest.utils.matcher.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record PlainHttpResponse(
        @JsonProperty("status") Object status,
        @JsonProperty("reason") String reasonPhrase,
        @JsonProperty("body") Object entity,
        @JsonProperty("headers") List<Map.Entry<String, Object>> headers) {

    public PlainHttpResponse {
        headers = headers == null ? null : List.copyOf(headers);
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

    public static final class Builder {
        private Object status;
        private String reasonPhrase;
        private Object entity;
        private List<Map.Entry<String, Object>> headers;

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

        public Builder headers(List<Map.Entry<String, Object>> headers) {
            this.headers = headers;
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
