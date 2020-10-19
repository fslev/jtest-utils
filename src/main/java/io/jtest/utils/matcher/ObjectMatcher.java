package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.poller.MethodPoller;
import org.apache.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectMatcher {
    /**
     * Compares objects as Json, Xml or String in that order
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> match(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        return new FlowMatcher().match(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions)));
    }

    /**
     * Compares objects as Json, Xml or String in that order
     * Expected is compared against a supplier value until values match or timeout is reached
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> pollAndMatch(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollDurationInSeconds,
                                                   Long pollIntervalInMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return pollAndMatch(actual -> match(message, expected, actual, matchConditions), actualObjectSupplier, pollDurationInSeconds, pollIntervalInMillis, exponentialBackOff);
    }

    public static Map<String, Object> matchJson(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new JsonMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> pollAndMatchJson(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollDurationInSeconds,
                                                       Long pollIntervalInMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return pollAndMatch(actual -> matchJson(message, expected, actual, matchConditions), actualObjectSupplier, pollDurationInSeconds, pollIntervalInMillis, exponentialBackOff);
    }

    public static Map<String, Object> matchXml(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new XmlMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> pollAndMatchXml(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollDurationInSeconds,
                                                      Long pollIntervalInMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return pollAndMatch(actual -> matchXml(message, expected, actual, matchConditions), actualObjectSupplier, pollDurationInSeconds, pollIntervalInMillis, exponentialBackOff);
    }

    /**
     * Compares two objects as strings<br>
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
     * Compares an expected object with a supplier value as strings until matching passes or timeout is reached<br>
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> pollAndMatchString(String message, Object expected, Supplier<Object> actualObjectSupplier, Integer pollDurationInSeconds,
                                                         Long pollIntervalInMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return pollAndMatch(actual -> matchString(message, expected, actual, matchConditions), actualObjectSupplier, pollDurationInSeconds, pollIntervalInMillis, exponentialBackOff);
    }

    public static <T extends HttpResponse> Map<String, Object> matchHttpResponse(String message, Object expected, T actual, MatchCondition... matchConditions) {
        try {
            return new HttpResponseMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends HttpResponse> Map<String, Object> pollAndMatchHttpResponse(String message, Object expected, Supplier<T> actualObjectSupplier, Integer pollDurationInSeconds,
                                                                                        Long pollIntervalInMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        return pollAndMatch(actual -> matchHttpResponse(message, expected, actual, matchConditions), actualObjectSupplier, pollDurationInSeconds, pollIntervalInMillis, exponentialBackOff);
    }

    private static <T> Map<String, Object> pollAndMatch(Function<T, Map<String, Object>> matchFunction, Supplier<T> actualObjectSupplier, Integer pollDurationInSeconds,
                                                        Long pollIntervalInMillis, Double exponentialBackOff) {
        Map<String, Object> props = new HashMap<>();
        AtomicReference<AssertionError> error = new AtomicReference<>();
        new MethodPoller<T>()
                .duration(pollDurationInSeconds, pollIntervalInMillis)
                .exponentialBackOff(exponentialBackOff)
                .method(actualObjectSupplier)
                .until(p -> {
                    try {
                        props.putAll(matchFunction.apply(p));
                        error.set(null);
                        return true;
                    } catch (AssertionError e) {
                        error.set(e);
                        return false;
                    }
                }).poll();
        if (error.get() != null) {
            throw error.get();
        }
        return props;
    }
}