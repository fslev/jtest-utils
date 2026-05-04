package io.jtest.utils.matcher;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jtest.utils.common.RegexUtils;
import io.jtest.utils.common.StringParser;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.apache.commons.lang3.ClassUtils;
import org.junit.jupiter.api.AssertionFailureBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Matches Objects as Strings
 */
public class StringMatcher extends AbstractObjectMatcher<Object> {
    public static final String CAPTURE_PLACEHOLDER_PREFIX = "~[";
    public static final String CAPTURE_PLACEHOLDER_SUFFIX = "]";
    private static final Pattern captureGroupPattern = Pattern.compile(Pattern.quote(CAPTURE_PLACEHOLDER_PREFIX) + "(.*?)" + Pattern.quote(CAPTURE_PLACEHOLDER_SUFFIX),
            Pattern.DOTALL | Pattern.MULTILINE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.getFactory().setStreamReadConstraints(StreamReadConstraints.builder()
                .maxNestingDepth(Integer.MAX_VALUE).maxNumberLength(Integer.MAX_VALUE).maxStringLength(Integer.MAX_VALUE).build());
    }

    public StringMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
    }

    @Override
    Object convert(Object value) {
        return value;
    }

    @Override
    protected String matchTypeSuffix() {
        return "Strings do not match" + System.lineSeparator() + System.lineSeparator() + ASSERTION_ERROR_HINT_MESSAGE +
                System.lineSeparator() + System.lineSeparator();
    }

    @Override
    protected String negativeMatchMessage() {
        return System.lineSeparator() + "Strings match!" + System.lineSeparator() + ASSERTION_ERROR_HINT_MESSAGE +
                System.lineSeparator() + System.lineSeparator();
    }

    @Override
    public Map<String, Object> match() {
        if (matchConditions.remove(MatchCondition.DO_NOT_MATCH)) {
            try {
                positiveMatch();
            } catch (AssertionError e) {
                return new HashMap<>();
            }
            AssertionFailureBuilder.assertionFailure().message(negativeMatchMessage).expected(expected).actual(actual)
                    .includeValuesInMessage(false).buildAndThrow();
        }
        return positiveMatch();
    }

    private Map<String, Object> positiveMatch() {
        if (matchesWithNull()) {
            return new HashMap<>();
        }

        String expectedString = convertToString(expected);
        String actualString = convertToString(actual);
        List<String> placeholderNames = StringParser.captureValues(expectedString, captureGroupPattern);

        if (isStandalonePlaceholder(expectedString, placeholderNames)) {
            return captureActualAsStandalonePlaceholder(placeholderNames.get(0));
        }
        if (actual == null) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(expected).actual(null).buildAndThrow();
        }
        if (!placeholderNames.isEmpty()) {
            return matchWithCaptureGroups(expectedString, actualString, placeholderNames);
        }
        return matchAsRegexOrLiteral(expectedString, actualString);
    }

    private static boolean isStandalonePlaceholder(String expectedString, List<String> placeholderNames) {
        return placeholderNames.size() == 1
                && expectedString.equals(CAPTURE_PLACEHOLDER_PREFIX + placeholderNames.get(0) + CAPTURE_PLACEHOLDER_SUFFIX);
    }

    private Map<String, Object> captureActualAsStandalonePlaceholder(String placeholder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(placeholder, actual);
        return properties;
    }

    private Map<String, Object> matchWithCaptureGroups(String expectedString, String actualString, List<String> placeholderNames) {
        Pattern pattern = patternWithPlaceholdersAsCaptureGroups(expectedString, placeholderNames,
                matchConditions.contains(MatchCondition.REGEX_DISABLED));
        List<String> capturedValues = StringParser.captureValues(actualString, pattern, true);
        if (capturedValues.isEmpty()) {
            AssertionFailureBuilder.assertionFailure().message(message).expected(expected).actual(actual).buildAndThrow();
        }
        Map<String, Object> properties = new HashMap<>();
        int limit = Math.min(capturedValues.size(), placeholderNames.size());
        for (int i = 0; i < limit; i++) {
            properties.put(placeholderNames.get(i), capturedValues.get(i));
        }
        return properties;
    }

    private Map<String, Object> matchAsRegexOrLiteral(String expectedString, String actualString) {
        if (matchConditions.contains(MatchCondition.REGEX_DISABLED)) {
            if (!expectedString.equals(actualString)) {
                AssertionFailureBuilder.assertionFailure().message(message).expected(expected).actual(actual).buildAndThrow();
            }
            return new HashMap<>();
        }
        try {
            Pattern pattern = Pattern.compile(expectedString, Pattern.DOTALL | Pattern.MULTILINE);
            if (!pattern.matcher(actualString).matches()) {
                AssertionFailureBuilder.assertionFailure().message(message).expected(expected).actual(actual).buildAndThrow();
            }
        } catch (PatternSyntaxException e) {
            if (!expectedString.equals(actual)) {
                AssertionFailureBuilder.assertionFailure().message(message).expected(expected).actual(actual).buildAndThrow();
            }
        }
        return new HashMap<>();
    }

    private boolean matchesWithNull() {
        if (expected == null) {
            if (actual != null) {
                AssertionFailureBuilder.assertionFailure().message(message).expected(null).actual(actual).buildAndThrow();
            } else {
                return true;
            }
        }
        return false;
    }

    private static String convertToString(Object value) {
        if (!(value instanceof String) || !ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
            try {
                return MAPPER.convertValue(value, String.class);
            } catch (IllegalArgumentException ignored) {
            }
        }
        return value.toString();
    }

    private static Pattern patternWithPlaceholdersAsCaptureGroups(String source, List<String> placeholderNames, boolean regexDisabled) {
        String s = source;
        boolean allowOtherRegexes = RegexUtils.isRegex(source) && !regexDisabled;
        for (String key : placeholderNames) {
            s = s.replace(CAPTURE_PLACEHOLDER_PREFIX + key + CAPTURE_PLACEHOLDER_SUFFIX, allowOtherRegexes ? "(.*)" : "\\E(.*)\\Q");
        }
        return Pattern.compile(allowOtherRegexes ? s : "\\Q" + s + "\\E", Pattern.DOTALL | Pattern.MULTILINE);
    }
}
