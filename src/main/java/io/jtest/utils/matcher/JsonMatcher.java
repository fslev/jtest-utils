package io.jtest.utils.matcher;

import com.fasterxml.jackson.databind.JsonNode;
import io.json.compare.CompareMode;
import io.json.compare.JSONCompare;
import io.json.compare.util.JsonUtils;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.comparators.json.CustomJsonComparator;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.AssertionFailureBuilder;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.json.compare.JSONCompare.prettyPrint;


public class JsonMatcher extends AbstractObjectMatcher<JsonNode> {
    private final CustomJsonComparator comparator;

    public JsonMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.comparator = new CustomJsonComparator(matchConditions);
    }

    @Override
    JsonNode convert(Object value) throws InvalidTypeException {
        try {
            return JsonUtils.toJson(value);
        } catch (Exception e) {
            throw new InvalidTypeException("Invalid JSON NODE", e);
        }
    }

    @Override
    protected String matchTypeSuffix() {
        return "JSONs do not match" + System.lineSeparator() + System.lineSeparator() + ASSERTION_ERROR_HINT_MESSAGE +
                System.lineSeparator() + System.lineSeparator();
    }

    @Override
    protected String negativeMatchMessage() {
        return System.lineSeparator() + "JSONs match!" + System.lineSeparator() + ASSERTION_ERROR_HINT_MESSAGE +
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
            AssertionFailureBuilder.assertionFailure().message(negativeMatchMessage).includeValuesInMessage(false)
                    .expected(prettyPrint(expected)).actual(prettyPrint(actual)).buildAndThrow();
        }
        return positiveMatch();
    }

    private Map<String, Object> positiveMatch() {
        try {
            JSONCompare.assertMatches(expected, actual, comparator, jsonCompareModes(), message);
        } catch (AssertionError firstFailure) {
            if (comparator.getFieldProperties().isEmpty()) {
                throw firstFailure;
            }
            retryUntilFieldPropertiesDepleted();
        }
        return comparator.getValueProperties();
    }

    private void retryUntilFieldPropertiesDepleted() {
        while (true) {
            comparator.getDepletedFieldPropertyList().add(new HashMap<>(comparator.getFieldProperties()));
            comparator.getFieldProperties().clear();
            try {
                JSONCompare.assertMatches(expected, actual, comparator, jsonCompareModes(), message);
                return;
            } catch (AssertionError retryFailure) {
                if (comparator.getFieldProperties().isEmpty()) {
                    throw retryFailure;
                }
            }
        }
    }

    private Set<CompareMode> jsonCompareModes() {
        Set<CompareMode> modes = EnumSet.noneOf(CompareMode.class);
        for (MatchCondition condition : matchConditions) {
            switch (condition) {
                case JSON_NON_EXTENSIBLE_OBJECT -> modes.add(CompareMode.JSON_OBJECT_NON_EXTENSIBLE);
                case JSON_NON_EXTENSIBLE_ARRAY -> modes.add(CompareMode.JSON_ARRAY_NON_EXTENSIBLE);
                case JSON_STRICT_ORDER_ARRAY -> modes.add(CompareMode.JSON_ARRAY_STRICT_ORDER);
                default -> { /* unrelated condition, ignore */ }
            }
        }
        return modes;
    }
}
