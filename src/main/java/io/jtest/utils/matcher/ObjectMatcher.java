package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.exceptions.PollingTimeoutException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.polling.Polling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectMatcher {
    /**
     * Matches objects as Json, Xml or String in that order
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> match(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        return new FlowMatcher().match(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions)));
    }

    /**
     * Matches objects as Json, Xml or String in that order<br>
     * Expected is compared against a supplier value until values match or timeout is reached
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> match(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollingDurationSeconds,
                                            Long pollingIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return match(actual -> match(message, expected, actual, matchConditions), actualObjectSupplier, pollingDurationSeconds, pollingIntervalMillis, exponentialBackOff);
    }

    public static Map<String, Object> matchJson(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new JsonMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> matchJson(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollingDurationSeconds,
                                                Long pollingIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return match(actual -> matchJson(message, expected, actual, matchConditions), actualObjectSupplier, pollingDurationSeconds, pollingIntervalMillis, exponentialBackOff);
    }

    public static Map<String, Object> matchXml(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new XmlMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> matchXml(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollingDurationSeconds,
                                               Long pollingIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return match(actual -> matchXml(message, expected, actual, matchConditions), actualObjectSupplier, pollingDurationSeconds, pollingIntervalMillis, exponentialBackOff);
    }

    /**
     * Matches two objects as strings<br>
     * Expected could contain regular expressions.<br>
     * If expected contains special regex characters and you want to match them as simple characters, just quote the expression using \Q and \E.
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> matchString(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new StringMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches an expected object with a supplier value as strings until matching is successful or timeout is reached<br>
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> matchString(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollingDurationSeconds,
                                                  Long pollingIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return match(actual -> matchString(message, expected, actual, matchConditions), actualObjectSupplier, pollingDurationSeconds, pollingIntervalMillis, exponentialBackOff);
    }

    /**
     * Matches two objects representing HTTP responses<br>
     * MatchCondition.DO_NOT_MATCH is ambiguous in this case. Use MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS, ...BY_BODY, etc<br>
     *
     * @param expected a HttpResponseWrapper convertible object, such as org.apache.http.HttpPResponse or a String, Map or JsonNode with the following JSON format:<br>
     *                 <p>
     *                 {"status": <number> | "<text>", <br>
     *                 "body": {<jsonObject>} | [<jsonArray>] | "<text>", <br>
     *                 "headers": [{"name":"value"}, ...], <br>
     *                 "reason": "<text>" <br>
     *                 } <br>
     *                 All fields are optional <br>
     * @param actual   a HttpResponseWrapper convertible object
     * @return properties captured after the match <br>
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> matchHttpResponse(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new HttpResponseMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches two objects representing HTTP responses until matching is successful or timeout is reached<br>
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> matchHttpResponse(String message, Object expected, Supplier<Object> actualSupplier, Integer pollingDurationSeconds,
                                                        Long pollingIntervalMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return match(actual -> matchHttpResponse(message, expected, actual, matchConditions), actualSupplier, pollingDurationSeconds, pollingIntervalMillis, exponentialBackOff);
    }

    private static <T> Map<String, Object> match(Function<T, Map<String, Object>> matchFunction, Supplier<T> actualObjectSupplier, Integer pollingDurationSeconds,
                                                 Long pollingIntervalMillis, Double exponentialBackOff) {
        Map<String, Object> props = new HashMap<>();
        AtomicReference<AssertionError> error = new AtomicReference<>();
        Polling<T> polling = new Polling<T>()
                .duration(pollingDurationSeconds, pollingIntervalMillis)
                .exponentialBackOff(exponentialBackOff)
                .supplier(actualObjectSupplier)
                .until(actual -> {
                    try {
                        props.putAll(matchFunction.apply(actual));
                        error.set(null);
                        return true;
                    } catch (AssertionError e) {
                        error.set(e);
                        return false;
                    }
                });
        try {
            polling.get();
        } catch (PollingTimeoutException e) {
            throw error.get();
        }
        return props;
    }
}