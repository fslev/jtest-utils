package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.skyah.util.MessageUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractObjectMatcher<T> {

    protected static final Logger LOG = LogManager.getLogger();

    protected String message;
    protected String negativeMatchMessage;
    protected T expected;
    protected T actual;
    protected Set<MatchCondition> matchConditions;

    protected AbstractObjectMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        this.expected = convert(expected);
        this.actual = convert(actual);
        this.matchConditions = matchConditions != null ? matchConditions : new HashSet<>();
        this.message = message;
        this.negativeMatchMessage = message == null ? negativeMatchMessage() : message + "\n" + negativeMatchMessage();
    }

    private String negativeMatchMessage() {
        return "\nObjects match!\nEXPECTED:\n" + MessageUtil.cropL(toString(this.expected))
                + "\n\nACTUAL:\n" + MessageUtil.cropL(toString(this.actual)) + "\n";
    }

    protected String toString(T value) {
        return value != null ? value.toString() : null;
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
