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
import java.util.function.Supplier;

public class Matcher {
    /**
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object
     */
    public static Map<String, Object> match(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        return new ObjectMatcher().match(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions)));
    }

    public static Map<String, Object> matchJson(String message, Object expected, Object actual, MatchCondition... matchConditions) throws InvalidTypeException {
        return new JsonMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
    }

    public static Map<String, Object> matchXml(String message, Object expected, Object actual, MatchCondition... matchConditions) throws InvalidTypeException {
        return new XmlMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
    }

    public static Map<String, Object> matchString(String message, Object expected, Object actual, MatchCondition... matchConditions) throws InvalidTypeException {
        return new StringMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
    }

    public static <T extends HttpResponse> Map<String, Object> matchHttpResponse(String message, Object expected, T actual, MatchCondition... matchConditions) throws InvalidTypeException {
        return new HttpResponseMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
    }

    public static Map<String, Object> pollAndMatch(String message, Object expected, Supplier<Object> supplier, Integer pollDurationInSeconds,
                                                   Long pollIntervalInMillis, Double exponentialBackOff, MatchCondition... matchConditions) {
        Map<String, Object> props = new HashMap<>();
        AtomicReference<AssertionError> error = new AtomicReference<>();
        new MethodPoller<>()
                .duration(pollDurationInSeconds, pollIntervalInMillis)
                .exponentialBackOff(exponentialBackOff)
                .method(supplier)
                .until(p -> {
                    try {
                        props.putAll(match(message, expected, p, matchConditions));
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