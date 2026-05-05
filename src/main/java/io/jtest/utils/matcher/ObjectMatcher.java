package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * Asserts that an actual value matches an expected one — as JSON, XML, plain string, or
 * HTTP response. The expected value can use {@code ~[name]} placeholders to capture
 * parts of the actual value, returned in the result map.
 *
 * <p>Scalar values are matched as Java regex by default. Pass
 * {@link MatchCondition#REGEX_DISABLED} to compare literally, or other
 * {@link MatchCondition} flags to tighten/invert the default lenient behavior.
 * Mismatches throw {@code AssertionError}.
 *
 * <pre>{@code
 *   Map<String, Object> captured = ObjectMatcher.matchString(
 *           null, "Hello, ~[who]!", "Hello, world!");
 *   captured.get("who"); // "world"
 * }</pre>
 */
public class ObjectMatcher {

    private ObjectMatcher() {

    }

    /**
     * Matches by trying JSON, then XML, then plain string. Use this when the content
     * type is not known up front; otherwise prefer the typed methods below.
     *
     * @return placeholders captured from {@code actual}
     * @throws AssertionError on mismatch
     */
    public static Map<String, Object> match(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        return new FlowMatcher().match(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions)));
    }

    /**
     * Matches both sides as JSON. Either side can be a JSON string, {@code JsonNode},
     * {@code Map}, {@code List}, or POJO.
     *
     * <pre>{@code
     *   ObjectMatcher.matchJson(null, "{\"id\":\"~[id]\"}", "{\"id\":\"abc\"}");
     * }</pre>
     *
     * @return placeholders captured from {@code actual}
     * @throws AssertionError on mismatch
     * @throws RuntimeException if either side cannot be parsed as JSON
     */
    public static Map<String, Object> matchJson(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new JsonMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches both sides as XML. Either side can be an XML string or a {@code Node}.
     * Whitespace differences in element content are ignored.
     *
     * <pre>{@code
     *   ObjectMatcher.matchXml(null, "<a id=\"~[id]\"/>", "<a id=\"42\"/>");
     * }</pre>
     *
     * @return placeholders captured from {@code actual}
     * @throws AssertionError on mismatch
     * @throws RuntimeException if either side cannot be parsed as XML
     */
    public static Map<String, Object> matchXml(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new XmlMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches both sides as strings. Non-strings are stringified first.
     * {@code expected} is treated as a Java regex by default; quote literals with
     * {@code \Q…\E} or pass {@link MatchCondition#REGEX_DISABLED}.
     *
     * <pre>{@code
     *   ObjectMatcher.matchString(null, "code: \\d+", "code: 200");
     * }</pre>
     *
     * @return placeholders captured from {@code actual}
     * @throws AssertionError on mismatch
     */
    public static Map<String, Object> matchString(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new StringMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches two HTTP responses by status, reason, headers, and body. Only the
     * components set on {@code expected} are asserted on — leave a component {@code null}
     * to skip it. The body is matched as JSON, XML, or string depending on its content.
     *
     * <p>For per-component negation use {@code DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS} /
     * {@code …_BY_REASON} / {@code …_BY_HEADERS} / {@code …_BY_BODY}.
     *
     * <pre>{@code
     *   PlainHttpResponse expected = PlainHttpResponse.Builder.create()
     *           .status(200)
     *           .entity("{\"id\":\"~[id]\"}")
     *           .build();
     *   ObjectMatcher.matchHttpResponse(null, expected, actual);
     * }</pre>
     *
     * @return placeholders captured from {@code actual}
     * @throws AssertionError on mismatch
     */
    public static Map<String, Object> matchHttpResponse(String message, PlainHttpResponse expected, PlainHttpResponse actual, MatchCondition... matchConditions) {
        try {
            return new HttpResponseMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
