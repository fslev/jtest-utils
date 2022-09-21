package io.jtest.utils.matcher;

import com.fasterxml.jackson.databind.JsonNode;
import io.json.compare.CompareMode;
import io.json.compare.JSONCompare;
import io.json.compare.util.JsonUtils;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.comparators.json.CustomJsonComparator;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.AssertionFailureBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.json.compare.JSONCompare.prettyPrint;


public class JsonMatcher extends AbstractObjectMatcher<JsonNode> {
    private final CustomJsonComparator comparator;

    public JsonMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.message += "JSONs do not match\n\n" + ASSERTION_ERROR_HINT_MESSAGE + "\n\n";
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
    protected String negativeMatchMessage() {
        return "\nJSONs match!\n" + ASSERTION_ERROR_HINT_MESSAGE + "\n\n";
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
        } catch (AssertionError e) {
            if (!comparator.getFieldProperties().isEmpty()) {
                while (true) {
                    comparator.getDepletedFieldPropertyList().add(new HashMap<>(comparator.getFieldProperties()));
                    comparator.getFieldProperties().clear();
                    try {
                        JSONCompare.assertMatches(expected, actual, comparator, jsonCompareModes(), message);
                    } catch (AssertionError e1) {
                        if (!comparator.getFieldProperties().isEmpty()) {
                            continue;
                        }
                        throw e1;
                    }
                    break;
                }
            } else {
                throw e;
            }
        }
        return comparator.getValueProperties();
    }

    private Set<CompareMode> jsonCompareModes() {
        Set<CompareMode> jsonCompareModes = new HashSet<>();
        for (MatchCondition condition : matchConditions) {
            if (MatchCondition.JSON_NON_EXTENSIBLE_OBJECT.equals(condition)) {
                jsonCompareModes.add(CompareMode.JSON_OBJECT_NON_EXTENSIBLE);
            } else if (MatchCondition.JSON_NON_EXTENSIBLE_ARRAY.equals(condition)) {
                jsonCompareModes.add(CompareMode.JSON_ARRAY_NON_EXTENSIBLE);
            } else if (MatchCondition.JSON_STRICT_ORDER_ARRAY.equals(condition)) {
                jsonCompareModes.add(CompareMode.JSON_ARRAY_STRICT_ORDER);
            }
        }
        return jsonCompareModes;
    }
}
