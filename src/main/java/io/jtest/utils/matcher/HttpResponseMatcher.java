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
        try {
            if (expectedStatus != null) {
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS)) {
                    boolean error = false;
                    try {
                        properties.putAll(new StringMatcher(null, expectedStatus, actual.status(), matchConditions).match());
                    } catch (AssertionError e) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response statuses match!" + System.lineSeparator())
                                .expected(expectedStatus).actual(actual.status())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new StringMatcher("HTTP Response statuses do not match!" + System.lineSeparator() + message,
                            expectedStatus, actual.status(), matchConditions).match());
                }
            }

            if (expectedReason != null) {
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON)) {
                    boolean error = false;
                    try {
                        new StringMatcher(null, expectedReason, actual.reasonPhrase(), matchConditions).match();
                    } catch (AssertionError e) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response reasons match!" + System.lineSeparator())
                                .expected(expectedReason).actual(actual.reasonPhrase())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new StringMatcher("HTTP Response reasons do not match!" + System.lineSeparator() + message,
                            expectedReason, actual.reasonPhrase(), matchConditions).match());
                }
            }

            if (expectedHeaders != null) {
                Set<MatchCondition> headersMatchConditions = filteredMatchConditions(matchConditions, cond -> cond != MatchCondition.JSON_NON_EXTENSIBLE_ARRAY
                        && cond != MatchCondition.JSON_NON_EXTENSIBLE_OBJECT
                        && cond != MatchCondition.JSON_STRICT_ORDER_ARRAY);
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS)) {
                    boolean error = false;
                    try {
                        new JsonMatcher(null, expectedHeaders, actual.headers(), headersMatchConditions).match();
                    } catch (AssertionError e) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response headers match!" + System.lineSeparator())
                                .expected(expectedHeaders).actual(actual.headers())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new JsonMatcher("HTTP Response headers do not match!" + System.lineSeparator() + message,
                            expectedHeaders, actual.headers(), headersMatchConditions).match());
                }
            }

            if (expectedEntity != null) {
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY)) {
                    boolean error = false;
                    try {
                        new FlowMatcher().match(null, expectedEntity, actual.entity(), matchConditions);
                    } catch (AssertionError ignored) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response bodies match!" + System.lineSeparator())
                                .expected(expectedEntity).actual(actual.entity())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new FlowMatcher().match("HTTP Response bodies do not match!" + System.lineSeparator() + message,
                            expectedEntity, actual.entity(), matchConditions));
                }
            }
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
