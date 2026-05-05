package io.jtest.utils.matcher.http;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * An HTTP-response value object suitable for use with
 * {@link io.jtest.utils.matcher.ObjectMatcher#matchHttpResponse}. Every component is optional;
 * a response declares only the parts the test wants to assert on.
 *
 * <p>Typical usage in a test that captures a server's actual response and matches it
 * against an expected one:
 * <pre>{@code
 *   PlainHttpResponse expected = PlainHttpResponse.Builder.create()
 *           .status(200)
 *           .reasonPhrase("OK")
 *           .headers(List.of(Map.entry("Content-Type", "application/json")))
 *           .entity("{\"id\": \"~[id]\"}")
 *           .build();
 *
 *   PlainHttpResponse actual = adapt(httpClient.execute(request));
 *
 *   Map<String, Object> captured = ObjectMatcher.matchHttpResponse(null, expected, actual);
 *   String capturedId = (String) captured.get("id");
 * }</pre>
 *
 * <p>The same shape can also be deserialized from JSON via Jackson — the {@code @JsonProperty}
 * annotations on the record components map the wire field names ({@code reason},
 * {@code body}) to the record's accessors ({@code reasonPhrase()}, {@code entity()}).
 *
 * @param status       HTTP status code (e.g. {@code 200}, {@code "200"}, {@code "2\\d\\d"});
 *                     deserialized from the JSON {@code status} field
 * @param reasonPhrase HTTP reason phrase (e.g. {@code "OK"});
 *                     deserialized from the JSON {@code reason} field
 * @param entity       response body — may be a JSON document, an XML document, or plain text;
 *                     deserialized from the JSON {@code body} field
 * @param headers      ordered list of header name/value entries;
 *                     deserialized from the JSON {@code headers} field. The list passed in
 *                     is defensively copied to an unmodifiable {@link List#copyOf} on
 *                     construction.
 */
public record PlainHttpResponse(
        @JsonProperty("status") Object status,
        @JsonProperty("reason") String reasonPhrase,
        @JsonProperty("body") Object entity,
        @JsonProperty("headers") List<Map.Entry<String, Object>> headers) {

    /**
     * Compact constructor; defensively copies {@code headers} via {@link List#copyOf}
     * so subsequent mutation of the caller's list cannot affect this record.
     */
    public PlainHttpResponse {
        headers = headers == null ? null : List.copyOf(headers);
    }

    /**
     * Render only the non-null components, in the form
     * {@code {status=…, reason='…', body='…', headers=…}}. Used by assertion-failure
     * messages from {@link io.jtest.utils.matcher.ObjectMatcher#matchHttpResponse}.
     */
    @Override
    public String toString() {
        return "{" +
                (status != null ? "status=" + status : "") +
                (reasonPhrase != null ? ", reason='" + reasonPhrase + '\'' : "") +
                (entity != null ? ", body='" + entity + '\'' : "") +
                (headers != null ? ", headers=" + headers : "") +
                '}';
    }

    /**
     * Fluent builder for {@link PlainHttpResponse}. Every setter is optional — a response
     * with only some components set will assert only on those when used as an expected
     * value.
     *
     * <p>Obtain an instance with {@link #create()}, chain setters, then call
     * {@link #build()}.
     */
    public static final class Builder {
        private Object status;
        private String reasonPhrase;
        private Object entity;
        private List<Map.Entry<String, Object>> headers;

        private Builder() {
        }

        /** Returns a new, empty builder. */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Sets the HTTP status. May be a {@link Number}, a status-code string,
         * or a regex pattern (when used as an expected value).
         */
        public Builder status(Object status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the HTTP reason phrase (e.g. {@code "OK"}, {@code "Not Found"}).
         * May contain a regex pattern when used as an expected value.
         */
        public Builder reasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
            return this;
        }

        /**
         * Sets the response body. May be a JSON-convertible object (string, {@code Map},
         * {@code List}, POJO), an XML string, plain text, or a regex when used as an
         * expected value.
         */
        public Builder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        /**
         * Sets the headers as an ordered list of name/value entries — duplicate names
         * are allowed (HTTP permits it). Construct entries with {@link Map#entry}.
         */
        public Builder headers(List<Map.Entry<String, Object>> headers) {
            this.headers = headers;
            return this;
        }

        /** Returns an immutable {@link PlainHttpResponse} reflecting the configured components. */
        public PlainHttpResponse build() {
            return new PlainHttpResponse(status, reasonPhrase, entity, headers);
        }
    }

    /**
     * Thrown when raw content (typically a JSON string) cannot be parsed into a
     * {@link PlainHttpResponse}. The message includes a description of the expected
     * shape, so test failures point at how to fix the input.
     */
    public static class ParseException extends RuntimeException {
        /** The shape that {@link PlainHttpResponse} expects when deserialized from JSON. */
        public static final String EXPECTED_FORMAT = """
                {
                  "status": <number> | "<text>",
                  "body": <object>,
                  "headers": [{"<name>":<value>}, ...],
                  "reason": "<text>"
                }""";

        /** Creates a parse exception with no underlying cause. */
        public ParseException(String msg) {
            this(msg, null);
        }

        /**
         * Creates a parse exception that wraps an underlying cause (typically a
         * Jackson {@code JsonProcessingException}).
         */
        public ParseException(String msg, Throwable t) {
            super(msg + System.lineSeparator() + "Expected format:" + System.lineSeparator() + EXPECTED_FORMAT, t);
        }
    }
}
