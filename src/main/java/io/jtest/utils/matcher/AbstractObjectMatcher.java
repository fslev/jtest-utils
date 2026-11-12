package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.AssertionFailureBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class AbstractObjectMatcher<T> {
    protected static final String ASSERTION_ERROR_HINT_MESSAGE = """
            Matching is by default case-sensitive and uses regular expressions.
            If expected object contains any unintentional regexes, then quote them between \\Q and \\E delimiters.
            For disabling case-sensitivity, use (?i) and (?-i) modifiers.""";

    protected final String message;
    protected final String negativeMatchMessage;
    protected final T expected;
    protected final T actual;
    protected final Set<MatchCondition> matchConditions;

    protected AbstractObjectMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        this.expected = convert(expected);
        this.actual = convert(actual);
        this.matchConditions = matchConditions != null ? matchConditions : new HashSet<>();
        String userPrefix = message != null ? message + System.lineSeparator() : "";
        this.message = userPrefix + matchTypeSuffix();
        this.negativeMatchMessage = message == null ? negativeMatchMessage() : message + System.lineSeparator() + negativeMatchMessage();
    }

    /**
     * Suffix appended to the user-provided message describing the type of objects being matched.
     * Returns an empty string when no suffix is needed.
     */
    protected String matchTypeSuffix() {
        return "";
    }

    protected String negativeMatchMessage() {
        return System.lineSeparator() + "Objects match!" + System.lineSeparator();
    }

    /**
     * Throws an assertion failure that ends with the expected and actual values under plain labels.
     * JUnit's own " ==> expected: &lt;...&gt; but was: &lt;...&gt;" suffix is turned off, as &lt; and &gt; are
     * Markdown syntax. IDEs still get both values for their diff view.
     */
    static void fail(String message, Object expected, Object actual) {
        String ls = System.lineSeparator();
        AssertionFailureBuilder.assertionFailure()
                .message(message + "EXPECTED:" + ls + expected + ls + ls + "ACTUAL:" + ls + actual)
                .expected(expected).actual(actual).includeValuesInMessage(false).buildAndThrow();
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
