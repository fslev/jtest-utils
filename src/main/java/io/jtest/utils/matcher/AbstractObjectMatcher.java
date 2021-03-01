package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractObjectMatcher<T> {

    protected static final Logger LOG = LogManager.getLogger();

    protected String message;
    protected final String negativeMatchMessage;
    protected T expected;
    protected T actual;
    protected Set<MatchCondition> matchConditions;

    protected AbstractObjectMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        this.expected = convert(expected);
        this.actual = convert(actual);
        this.matchConditions = matchConditions != null ? matchConditions : new HashSet<>();
        this.message = message;
        String defaultNegativeMessage = "\nObjects match!\nEXPECTED:\n" + this.expected + "\n\nACTUAL:\n" + this.actual + "\n";
        this.negativeMatchMessage = message == null ? defaultNegativeMessage : message + "\n" + defaultNegativeMessage;
    }

    abstract T convert(Object value) throws InvalidTypeException;

    /**
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object
     */
    abstract Map<String, Object> match();


    Set<MatchCondition> filteredMatchConditions(Set<MatchCondition> matchConditions, Predicate<MatchCondition> filter) {
        return matchConditions.stream().filter(filter).collect(Collectors.toSet());
    }
}
