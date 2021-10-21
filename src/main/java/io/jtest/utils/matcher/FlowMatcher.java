package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.commons.lang3.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * Matches JSONs, XMLs and Strings in that particular order
 */
class FlowMatcher {

    private static final Logger LOG = LogManager.getLogger();

    public Map<String, Object> match(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) {

        if (expected != null && !ClassUtils.isPrimitiveOrWrapper(expected.getClass())
                && actual != null && !ClassUtils.isPrimitiveOrWrapper(actual.getClass())) {
            try {
                LOG.debug("Match as JSONs");
                return new JsonMatcher(message, expected, actual, matchConditions).match();
            } catch (InvalidTypeException e1) {
                LOG.debug("Objects are NOT JSONs:\nEXPECTED:\n{}\nACTUAL:\n{}\n--> proceed to XML matching", expected, actual);
                try {
                    LOG.debug("Match as XMLs");
                    return new XmlMatcher(message, expected, actual, matchConditions).match();
                } catch (InvalidTypeException e2) {
                    LOG.debug("Objects are NOT XMLs:\nEXPECTED:\n{}\nACTUAL:\n{}\n--> proceed to string matching", expected, actual);
                }
            }
        }
        try {
            LOG.debug("Match as Strings");
            return new StringMatcher(message, expected, actual, matchConditions).match();
        } catch (InvalidTypeException e3) {
            throw new RuntimeException("Cannot match objects");
        }
    }
}
