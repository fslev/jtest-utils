package io.jtest.utils.matcher;


import io.jtest.utils.clients.http.wrappers.HttpResponseWrapper;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import ro.skyah.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.fail;

class HttpResponseMatcher extends AbstractObjectMatcher<HttpResponseWrapper> {

    private final String expectedStatus;
    private final String expectedReason;
    private final Set<Map.Entry<String, String>> expectedHeaders;
    private final Object expectedEntity;

    public HttpResponseMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.expectedStatus = this.expected.getStatus();
        this.expectedReason = this.expected.getReasonPhrase();
        this.expectedHeaders = this.expected.getHeaders();
        this.expectedEntity = this.expected.getEntity();
        String defaultMessage = "\nEXPECTED HTTP Response:\n" + MessageUtil.cropL(toString(this.expected))
                + "\n\nBUT GOT HTTP Response:\n" + MessageUtil.cropL(toString(this.actual)) + "\n";
        this.message = this.message != null ? this.message + defaultMessage : defaultMessage;
    }

    @Override
    HttpResponseWrapper convert(Object value) throws InvalidTypeException {
        if (!(value instanceof HttpResponseWrapper)) {
            try {
                return new HttpResponseWrapper(value);
            } catch (Exception e) {
                throw new InvalidTypeException("Cannot convert HTTP Response", e);
            }
        }
        return (HttpResponseWrapper) value;
    }

    @Override
    public Map<String, Object> match() {
        Map<String, Object> properties = new HashMap<>();
        matchConditions.remove(MatchCondition.DO_NOT_MATCH);
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
                        fail(negativeMatchMessage);
                    }
                } else {
                    properties.putAll(new StringMatcher("\nHTTP Response statuses do not match!\n" + message,
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
                        fail(negativeMatchMessage);
                    }
                } else {
                    properties.putAll(new StringMatcher("\nHTTP Response reasons do not match!\n" + message,
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
                        fail(negativeMatchMessage);
                    }
                } else {
                    properties.putAll(new JsonMatcher("\nHTTP Response headers do not match!\n" + message,
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
                        fail(negativeMatchMessage);
                    }
                } else {
                    properties.putAll(new FlowMatcher().match("\nHTTP Response bodies do not match!\n" + message,
                            expectedEntity, actual.getEntity(), matchConditions));
                }
            }
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
