package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * Matches Objects with Strings
 */
public class FlowMatcher {

    protected static final Logger LOG = LogManager.getLogger();

    public Map<String, Object> match(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) {
        AbstractObjectMatcher<?> matcher;
        try {
            LOG.debug("Compare as JSONs");
            matcher = new JsonMatcher(message, expected, actual, matchConditions);
        } catch (InvalidTypeException e1) {
            LOG.debug("Objects are NOT JSONs:\nEXPECTED:\n{}\nACTUAL:\n{}\n--> proceed to XML compare", expected, actual);
            try {
                LOG.debug("Compare as XMLs");
                matcher = new XmlMatcher(message, expected, actual, matchConditions);
            } catch (InvalidTypeException e2) {
                LOG.debug("Objects are NOT XMLs:\nEXPECTED:\n{}\nACTUAL:\n{}\n--> proceed to string REGEX compare", expected, actual);
                try {
                    matcher = new StringMatcher(message, expected, actual, matchConditions);
                } catch (InvalidTypeException e3) {
                    throw new RuntimeException("Cannot compare objects");
                }
            }
        }
        return matcher.match();
    }
}
