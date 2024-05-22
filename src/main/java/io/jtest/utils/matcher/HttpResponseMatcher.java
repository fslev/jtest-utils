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
        this.expectedStatus = this.expected.getStatus();
        this.expectedReason = this.expected.getReasonPhrase();
        this.expectedHeaders = this.expected.getHeaders();
        this.expectedEntity = this.expected.getEntity();
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
                        properties.putAll(new StringMatcher(null, expectedStatus, actual.getStatus(), matchConditions).match());
                    } catch (AssertionError e) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response statuses match!" + System.lineSeparator())
                                .expected(expectedStatus).actual(actual.getStatus())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new StringMatcher("HTTP Response statuses do not match!" + System.lineSeparator() + message,
                            expectedStatus, actual.getStatus(), matchConditions).match());
                }
            }

            if (expectedReason != null) {
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON)) {
                    boolean error = false;
                    try {
                        new StringMatcher(null, expectedReason, actual.getReasonPhrase(), matchConditions).match();
                    } catch (AssertionError e) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response reasons match!" + System.lineSeparator())
                                .expected(expectedReason).actual(actual.getReasonPhrase())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new StringMatcher("HTTP Response reasons do not match!" + System.lineSeparator() + message,
                            expectedReason, actual.getReasonPhrase(), matchConditions).match());
                }
            }

            if (expectedHeaders != null) {
                Set<MatchCondition> headersMatchConditions = filteredMatchConditions(matchConditions, cond -> cond != MatchCondition.JSON_NON_EXTENSIBLE_ARRAY
                        && cond != MatchCondition.JSON_NON_EXTENSIBLE_OBJECT
                        && cond != MatchCondition.JSON_STRICT_ORDER_ARRAY);
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS)) {
                    boolean error = false;
                    try {
                        new JsonMatcher(null, expectedHeaders, actual.getHeaders(), headersMatchConditions).match();
                    } catch (AssertionError e) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response headers match!" + System.lineSeparator())
                                .expected(expectedHeaders).actual(actual.getHeaders())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new JsonMatcher("HTTP Response headers do not match!" + System.lineSeparator() + message,
                            expectedHeaders, actual.getHeaders(), headersMatchConditions).match());
                }
            }

            if (expectedEntity != null) {
                if (matchConditions.contains(MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY)) {
                    boolean error = false;
                    try {
                        new FlowMatcher().match(null, expectedEntity, actual.getEntity(), matchConditions);
                    } catch (AssertionError ignored) {
                        error = true;
                    }
                    if (!error) {
                        AssertionFailureBuilder.assertionFailure().message(this.message + System.lineSeparator() +
                                        "HTTP Response bodies match!" + System.lineSeparator())
                                .expected(expectedEntity).actual(actual.getEntity())
                                .includeValuesInMessage(false).buildAndThrow();
                    }
                } else {
                    properties.putAll(new FlowMatcher().match("HTTP Response bodies do not match!" + System.lineSeparator() + message,
                            expectedEntity, actual.getEntity(), matchConditions));
                }
            }
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
