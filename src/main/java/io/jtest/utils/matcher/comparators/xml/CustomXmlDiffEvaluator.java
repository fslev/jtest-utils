package io.jtest.utils.matcher.comparators.xml;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.StringMatcher;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.DifferenceEvaluator;
import org.xmlunit.util.Nodes;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomXmlDiffEvaluator implements DifferenceEvaluator {

    private final Map<String, Object> generatedProperties = new HashMap<>();
    private final Set<MatchCondition> matchConditions;

    public CustomXmlDiffEvaluator(Set<MatchCondition> matchConditions) {
        this.matchConditions = matchConditions;
    }

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult comparisonResult) {
        ComparisonType comparisonType = comparison.getType();

        switch (comparisonType) {
            case CHILD_NODELIST_LENGTH:
                return matchConditions.contains(MatchCondition.XML_CHILD_NODELIST_LENGTH) ? comparisonResult : ComparisonResult.SIMILAR;
            case CHILD_NODELIST_SEQUENCE:
                return matchConditions.contains(MatchCondition.XML_CHILD_NODELIST_SEQUENCE)
                        ? (comparisonResult == ComparisonResult.SIMILAR ? ComparisonResult.DIFFERENT : comparisonResult)
                        : ComparisonResult.SIMILAR;
            case ELEMENT_NUM_ATTRIBUTES:
                return matchConditions.contains(MatchCondition.XML_ELEMENT_NUM_ATTRIBUTES) ? comparisonResult : ComparisonResult.SIMILAR;
            case XML_ENCODING:
            case XML_VERSION:
            case XML_STANDALONE:
            case NO_NAMESPACE_SCHEMA_LOCATION:
            case NAMESPACE_URI:
            case NAMESPACE_PREFIX:
            case SCHEMA_LOCATION:
                return ComparisonResult.SIMILAR;
            default:
                break;
        }

        Node expectedNode = comparison.getControlDetails().getTarget();
        if (expectedNode == null) {
            return ComparisonResult.SIMILAR;
        }
        Node actualNode = comparison.getTestDetails().getTarget();

        if (expectedNode instanceof Attr expectedAttr && actualNode instanceof Attr actualAttr) {
            return compare(expectedAttr.getValue(), actualAttr.getValue());
        }
        if (expectedNode instanceof Text expectedText && actualNode instanceof Text actualText) {
            return compare(expectedText.getData(), actualText.getData());
        }
        if (comparisonType == ComparisonType.ATTR_NAME_LOOKUP) {
            return compare(Nodes.getAttributes(expectedNode), Nodes.getAttributes(actualNode));
        }
        return comparisonResult;
    }

    private ComparisonResult compare(Map<QName, String> expectedAttributes, Map<QName, String> actualAttributes) {
        try {
            generatedProperties.putAll(match(expectedAttributes, actualAttributes));
        } catch (XmlMatchException e) {
            return ComparisonResult.DIFFERENT;
        }
        return ComparisonResult.SIMILAR;
    }

    private ComparisonResult compare(String expected, String actual) {
        try {
            generatedProperties.putAll(match(expected, actual));
        } catch (XmlMatchException e) {
            return ComparisonResult.DIFFERENT;
        }
        return ComparisonResult.SIMILAR;
    }

    public Map<String, Object> match(Map<QName, String> expectedAttributes, Map<QName, String> actualAttributes) throws XmlMatchException {
        Map<String, Object> generatedProps = new HashMap<>();
        for (Map.Entry<QName, String> expAttr : expectedAttributes.entrySet()) {
            String actualAttrVal = actualAttributes.get(expAttr.getKey());
            if (actualAttrVal == null) {
                throw new XmlMatchException();
            }
            try {
                generatedProps.putAll(match(expAttr.getValue(), actualAttrVal));
            } catch (XmlMatchException e) {
                throw new XmlMatchException();
            }
        }
        return generatedProps;
    }

    public Map<String, Object> match(String expected, String actual) throws XmlMatchException {
        try {
            return new StringMatcher(null, expected, actual, matchConditions).match();
        } catch (AssertionError | InvalidTypeException e) {
            throw new XmlMatchException();
        }
    }

    public Map<String, Object> getGeneratedProperties() {
        return generatedProperties;
    }
}
