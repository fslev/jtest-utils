package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * Static facade for matching expected against actual values in tests, with built-in support
 * for JSON, XML, plain strings, and HTTP responses.
 *
 * <p>This is the recommended entry point for callers — each {@code matchXxx} method is a thin
 * wrapper around the corresponding internal matcher and never throws checked exceptions.
 *
 * <h2>Capture placeholders</h2>
 * The expected value may contain capture placeholders of the form
 * {@code ~[name]}. On a successful match, every placeholder is captured from the actual
 * value and returned in the result map keyed by the placeholder name:
 * <pre>{@code
 *   Map<String, Object> captured = ObjectMatcher.matchString(
 *       null, "Hello, ~[who]!", "Hello, world!");
 *   captured.get("who"); // "world"
 * }</pre>
 *
 * <h2>Regex matching</h2>
 * Expected scalar values are interpreted as Java regular expressions
 * (with {@code DOTALL | MULTILINE}). Patterns that fail to compile fall back to literal
 * equality. Use {@link MatchCondition#REGEX_DISABLED} to disable regex globally for a call,
 * or quote unintentional regex characters with {@code \Q…\E}.
 *
 * <h2>Tweaking the match</h2>
 * All methods accept a varargs of {@link MatchCondition} flags that tighten or invert the
 * default lenient behavior — for example {@link MatchCondition#JSON_NON_EXTENSIBLE_OBJECT}
 * to require the actual JSON to have no extra fields, or {@link MatchCondition#DO_NOT_MATCH}
 * to assert the values do <em>not</em> match.
 *
 * <p>On mismatch, every method throws an {@code AssertionError} (specifically an
 * {@code org.opentest4j.AssertionFailedError}) describing all detected differences.
 */
public class ObjectMatcher {

    private ObjectMatcher() {

    }

    /**
     * Matches {@code expected} against {@code actual} by trying, in order, JSON, XML, and
     * finally plain-string comparison. The first matcher whose input the values can be
     * converted to is used.
     *
     * <p>Use this when the value's content type is dynamic (e.g. a service might return
     * either JSON or plain text). For known content types prefer
     * {@link #matchJson}, {@link #matchXml}, or {@link #matchString} — they fail fast on
     * type mismatches and produce more focused error messages.
     *
     * @param message         optional prefix prepended to assertion failure output;
     *                        may be {@code null}
     * @param expected        the expected value; may contain {@code ~[name]} capture
     *                        placeholders and (for JSON/XML/string scalars) Java regex
     * @param actual          the actual value to compare against
     * @param matchConditions zero or more {@link MatchCondition} flags
     * @return a map of placeholder names to captured values, or empty if no captures were defined
     * @throws AssertionError if the values do not match
     */
    public static Map<String, Object> match(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        return new FlowMatcher().match(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions)));
    }

    /**
     * Matches {@code expected} and {@code actual} as JSON.
     *
     * <p>Either side may be any JSON-convertible object: a {@code String} containing JSON,
     * a Jackson {@code JsonNode}, a {@code Map}, a {@code List}, or any POJO. Strings that
     * cannot be parsed as JSON cause a {@link RuntimeException} wrapping an
     * {@link InvalidTypeException}.
     *
     * <p>By default the match is lenient: extra fields and extra/out-of-order array
     * elements in {@code actual} are tolerated. Tighten with
     * {@link MatchCondition#JSON_NON_EXTENSIBLE_OBJECT},
     * {@link MatchCondition#JSON_NON_EXTENSIBLE_ARRAY}, or
     * {@link MatchCondition#JSON_STRICT_ORDER_ARRAY}.
     *
     * @param message         optional prefix prepended to assertion failure output;
     *                        may be {@code null}
     * @param expected        the expected JSON; may contain {@code ~[name]} placeholders
     *                        and regex in scalar values
     * @param actual          the actual JSON to compare against
     * @param matchConditions zero or more {@link MatchCondition} flags
     * @return a map of captured placeholder values
     * @throws AssertionError if the JSONs do not match
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
     * Matches {@code expected} and {@code actual} as XML.
     *
     * <p>Either side may be a {@code String} containing XML or an {@code org.w3c.dom.Node}.
     * Strings that cannot be parsed as XML cause a {@link RuntimeException} wrapping an
     * {@link InvalidTypeException}.
     *
     * <p>Whitespace is ignored when comparing element content. Tighten with
     * {@link MatchCondition#XML_CHILD_NODELIST_LENGTH} (require equal child counts),
     * {@link MatchCondition#XML_CHILD_NODELIST_SEQUENCE} (require child order to match),
     * or {@link MatchCondition#XML_ELEMENT_NUM_ATTRIBUTES} (require equal attribute counts).
     *
     * @param message         optional prefix prepended to assertion failure output;
     *                        may be {@code null}
     * @param expected        the expected XML; element text and attribute values may
     *                        contain {@code ~[name]} placeholders and regex
     * @param actual          the actual XML to compare against
     * @param matchConditions zero or more {@link MatchCondition} flags
     * @return a map of captured placeholder values
     * @throws AssertionError if the XMLs do not match
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
     * Matches {@code expected} and {@code actual} as strings.
     *
     * <p>Non-{@code String} arguments are converted to their string representation via
     * Jackson (or {@code toString()} as a fallback) before matching.
     *
     * <p>{@code expected} is interpreted as a Java regular expression with
     * {@code DOTALL | MULTILINE} semantics. Patterns that fail to compile fall back to
     * literal equality. To match special regex characters as literals quote them with
     * {@code \Q…\E}, or pass {@link MatchCondition#REGEX_DISABLED} to disable regex
     * globally.
     *
     * @param message         optional prefix prepended to assertion failure output;
     *                        may be {@code null}
     * @param expected        the expected string (or any object); may contain
     *                        {@code ~[name]} placeholders and regex
     * @param actual          the actual value to compare against
     * @param matchConditions zero or more {@link MatchCondition} flags
     * @return a map of captured placeholder values
     * @throws AssertionError if the strings do not match
     */
    public static Map<String, Object> matchString(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new StringMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches two HTTP responses by status, reason, headers, and body.
     *
     * <p>Each component is matched independently and only if {@code expected} declares a
     * non-null value for it — so a {@code PlainHttpResponse} that only sets {@code status}
     * asserts only on the status. Body content is dispatched through the same JSON / XML /
     * string flow as {@link #match(String, Object, Object, MatchCondition...)}.
     *
     * <p>{@link MatchCondition#DO_NOT_MATCH} is ambiguous for HTTP responses; use the
     * targeted {@code DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS} /
     * {@code …_BY_REASON} / {@code …_BY_HEADERS} / {@code …_BY_BODY} flags to assert
     * non-match on a specific component.
     *
     * <p>The {@code expected} response can be constructed via
     * {@link PlainHttpResponse.Builder#create()} or deserialized from a JSON document with
     * the schema:
     * <pre>{@code
     *   {
     *     "status": <number> | "<text>",
     *     "body":   <json> | "<xml-or-text>",
     *     "headers": [{"<name>": <value>}, ...],
     *     "reason": "<text>"
     *   }
     * }</pre>
     *
     * @param message         optional prefix prepended to assertion failure output;
     *                        may be {@code null}
     * @param expected        the expected response; only non-null components are asserted on
     * @param actual          the actual response to compare against
     * @param matchConditions zero or more {@link MatchCondition} flags
     * @return a map of placeholder values captured from any of the matched components
     * @throws AssertionError if any asserted component does not match
     */
    public static Map<String, Object> matchHttpResponse(String message, PlainHttpResponse expected, PlainHttpResponse actual, MatchCondition... matchConditions) {
        try {
            return new HttpResponseMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
