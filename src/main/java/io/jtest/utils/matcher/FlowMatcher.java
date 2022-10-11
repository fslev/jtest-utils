package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.commons.lang3.ClassUtils;

import java.util.Map;
import java.util.Set;

/**
 * Matches JSONs, XMLs and Strings in that particular order
 */
class FlowMatcher {

    public Map<String, Object> match(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) {

        if (expected != null && !ClassUtils.isPrimitiveOrWrapper(expected.getClass())
                && actual != null && !ClassUtils.isPrimitiveOrWrapper(actual.getClass())) {
            try {
                return new JsonMatcher(message, expected, actual, matchConditions).match();
            } catch (InvalidTypeException e1) {
                try {
                    return new XmlMatcher(message, expected, actual, matchConditions).match();
                } catch (InvalidTypeException ignored) {
                }
            }
        }
        try {
            return new StringMatcher(message, expected, actual, matchConditions).match();
        } catch (InvalidTypeException e3) {
            throw new RuntimeException("Cannot match objects");
        }
    }
}
