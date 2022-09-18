package io.jtest.utils.matcher;

import io.jtest.utils.common.XmlUtils;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.comparators.xml.CustomXmlDiffEvaluator;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.XPathContext;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.jtest.utils.common.XmlUtils.toNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class XmlMatcher extends AbstractObjectMatcher<Node> {

    private final CustomXmlDiffEvaluator diffEvaluator;

    public XmlMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.message += "XMLs do NOT match\n\n" + ASSERTION_ERROR_HINT_MESSAGE + "\n";
        this.diffEvaluator = new CustomXmlDiffEvaluator(this.matchConditions);
    }

    @Override
    protected String negativeMatchMessage() {
        return "\nXMLs match!\n" + ASSERTION_ERROR_HINT_MESSAGE + "\n";
    }

    @Override
    protected String toString(Node value) {
        try {
            return XmlUtils.toString(value);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    Node convert(Object value) throws InvalidTypeException {
        if (value instanceof String) {
            try {
                return toNode(value.toString());
            } catch (Exception e) {
                throw new InvalidTypeException("Invalid XML String format", e);
            }
        } else if (!(value instanceof Node)) {
            throw new InvalidTypeException("XML object must be of type String or org.w3c.dom.Node");
        }
        return (Node) value;
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
        assertThat(message, actual, isSimilarTo(expected).ignoreWhitespace()
                .withNodeMatcher(new DefaultNodeMatcher(new ByNameAndSiblingOfSameNameAndTypeSelector()))
                .withDifferenceEvaluator(
                        DifferenceEvaluators.chain(diffEvaluator)));
        return diffEvaluator.getGeneratedProperties();
    }

    private class ByNameAndSiblingOfSameNameAndTypeSelector implements ElementSelector {
        @Override
        public boolean canBeCompared(Element controlElement, Element testElement) {
            if (controlElement == null || testElement == null || !controlElement.getNodeName().equals(testElement.getNodeName())) {
                return false;
            }
            if (hasNextSiblingWithSameNameAndType(testElement)) {
                try {
                    new XmlMatcher(message, controlElement, testElement, matchConditions).match();
                    return true;
                } catch (AssertionError | InvalidTypeException e) {
                    return false;
                }
            } else {
                if (hasPreviousSiblingWithSameNameAndType(testElement)) {
                    try {
                        new XmlMatcher(message, controlElement, testElement, matchConditions).match();
                        return true;
                    } catch (AssertionError | InvalidTypeException e) {
                        throw new AssertionError("Expected element " + new XPathContext(controlElement).getXPath() + " doesn't match any actual element");
                    }
                }
            }
            return true;
        }
    }

    private static boolean hasNextSiblingWithSameNameAndType(Element element) {
        Node sibling = element.getNextSibling();
        while (sibling != null) {
            if (element.getNodeType() == sibling.getNodeType() &&
                    element.getNodeName().equals(sibling.getNodeName())) {
                return true;
            }
            sibling = sibling.getNextSibling();
        }
        return false;
    }

    private static boolean hasPreviousSiblingWithSameNameAndType(Element element) {
        Node sibling = element.getPreviousSibling();
        while (sibling != null) {
            if (element.getNodeType() == sibling.getNodeType() &&
                    element.getNodeName().equals(sibling.getNodeName())) {
                return true;
            }
            sibling = sibling.getPreviousSibling();
        }
        return false;
    }
}
