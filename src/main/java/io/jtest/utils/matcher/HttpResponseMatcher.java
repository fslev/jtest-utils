package io.jtest.utils.matcher;


import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;
import org.junit.jupiter.api.AssertionFailureBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class HttpResponseMatcher extends AbstractObjectMatcher<PlainHttpResponse> {

    private final Object expectedStatus;
    private final String expectedReason;
    private final List<Map.Entry<String, Object>> expectedHeaders;
    private final Object expectedEntity;

    public HttpResponseMatcher(String message, PlainHttpResponse expected, PlainHttpResponse actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.expectedStatus = this.expected.status();
        this.expectedReason = this.expected.reasonPhrase();
        this.expectedHeaders = this.expected.headers();
        this.expectedEntity = this.expected.entity();
    }

    @Override
    protected String negativeMatchMessage() {
        return System.lineSeparator() + "HTTP responses match!" + System.lineSeparator();
    }

    @Override
    PlainHttpResponse convert(Object value) {
        return (PlainHttpResponse) value;
    }


    @Override
    public Map<String, Object> match() {
        if (matchConditions.remove(MatchCondition.DO_NOT_MATCH)) {
            try {
                positiveMatch();
            } catch (AssertionError e) {
                return new HashMap<>();
            }
            AssertionFailureBuilder.assertionFailure().message(negativeMatchMessage).includeValuesInMessage(false)
                    .expected(expected).actual(actual).buildAndThrow();
        }
        return positiveMatch();
    }

    public Map<String, Object> positiveMatch() {
        Map<String, Object> properties = new HashMap<>();
        Set<MatchCondition> headersConditions = filteredMatchConditions(matchConditions,
                cond -> cond != MatchCondition.JSON_NON_EXTENSIBLE_ARRAY
                        && cond != MatchCondition.JSON_NON_EXTENSIBLE_OBJECT
                        && cond != MatchCondition.JSON_STRICT_ORDER_ARRAY);
        try {
            matchComponent("statuses", expectedStatus, actual.status(),
                    MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS, matchConditions,
                    (m, e, a, c) -> new StringMatcher(m, e, a, c).match(), properties);
            matchComponent("reasons", expectedReason, actual.reasonPhrase(),
                    MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON, matchConditions,
                    (m, e, a, c) -> new StringMatcher(m, e, a, c).match(), properties);
            matchComponent("headers", expectedHeaders, actual.headers(),
                    MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS, headersConditions,
                    (m, e, a, c) -> new JsonMatcher(m, e, a, c).match(), properties);
            matchComponent("bodies", expectedEntity, actual.entity(),
                    MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY, matchConditions,
                    (m, e, a, c) -> new FlowMatcher().match(m, e, a, c), properties);
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private void matchComponent(String label, Object expected, Object actual,
                                MatchCondition negateCondition,
                                Set<MatchCondition> conditions,
                                ComponentMatcher matcher,
                                Map<String, Object> properties) throws InvalidTypeException {
        if (expected == null) {
            return;
        }
        if (matchConditions.contains(negateCondition)) {
            boolean matched;
            try {
                matcher.match(null, expected, actual, conditions);
                matched = true;
            } catch (AssertionError ignored) {
                matched = false;
            }
            if (matched) {
                AssertionFailureBuilder.assertionFailure()
                        .message(this.message + System.lineSeparator() + "HTTP Response " + label + " match!" + System.lineSeparator())
                        .expected(expected).actual(actual)
                        .includeValuesInMessage(false).buildAndThrow();
            }
        } else {
            properties.putAll(matcher.match(
                    "HTTP Response " + label + " do not match!" + System.lineSeparator() + message,
                    expected, actual, conditions));
        }
    }

    @FunctionalInterface
    private interface ComponentMatcher {
        Map<String, Object> match(String message, Object expected, Object actual, Set<MatchCondition> conditions) throws InvalidTypeException;
    }
}
