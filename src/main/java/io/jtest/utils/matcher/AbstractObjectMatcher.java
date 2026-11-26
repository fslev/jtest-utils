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
     * Throws an assertion failure that ends with "EXPECTED: ..." and, on the next line, " BUT WAS: ...".
     * JUnit's own " ==> expected: &lt;...&gt; but was: &lt;...&gt;" suffix is turned off, as &lt; and &gt; are
     * Markdown syntax.
     * <p>
     * IDEs that run Cucumber 7+ get their diff link from Cucumber's TeamCity formatter, which reads only the message
     * text and matches {@code expected: (.*)(?:\r\n|\r|\n) but was: (.*)$} on it (case-insensitive, message
     * trimmed). So each value starts on its label's line, exactly one line break and one space come before
     * "BUT WAS", and the values come last. The final line break keeps both values ending the same way for
     * IntelliJ's own patterns.
     */
    static void fail(String message, Object expected, Object actual) {
        String ls = System.lineSeparator();
        AssertionFailureBuilder.assertionFailure()
                .message(message + "EXPECTED: " + expected + ls + " BUT WAS: " + actual + ls)
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
