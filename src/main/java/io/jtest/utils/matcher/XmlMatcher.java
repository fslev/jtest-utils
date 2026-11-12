package io.jtest.utils.matcher;

import io.jtest.utils.common.XmlUtils;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.comparators.xml.CustomXmlDiffEvaluator;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.DefaultComparisonFormatter;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.XPathContext;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.jtest.utils.common.XmlUtils.toNode;

public class XmlMatcher extends AbstractObjectMatcher<Node> {

    private final CustomXmlDiffEvaluator diffEvaluator;

    public XmlMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.diffEvaluator = new CustomXmlDiffEvaluator(this.matchConditions);
    }

    @Override
    protected String matchTypeSuffix() {
        return "XMLs do not match" + System.lineSeparator() + System.lineSeparator() + ASSERTION_ERROR_HINT_MESSAGE +
                System.lineSeparator() + System.lineSeparator();
    }

    @Override
    protected String negativeMatchMessage() {
        return System.lineSeparator() + "XMLs match!" + System.lineSeparator() + ASSERTION_ERROR_HINT_MESSAGE +
                System.lineSeparator() + System.lineSeparator();
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
        Diff diff = DiffBuilder.compare(expected).withTest(actual).ignoreWhitespace().checkForSimilar()
                .withNodeMatcher(new DefaultNodeMatcher(new ByNameAndSiblingOfSameNameAndTypeSelector()))
                .withDifferenceEvaluator(DifferenceEvaluators.chain(diffEvaluator))
                .withComparisonController(ComparisonControllers.StopWhenDifferent)
                .build();
        if (diff.hasDifferences()) {
            fail(message + describe(diff.getDifferences().iterator().next().getComparison()),
                    toString(expected).strip(), toString(actual).strip());
        }
        return diffEvaluator.getGeneratedProperties();
    }

    // Plain text description: XMLUnit's own ends with " - comparing <a...> at /x to <b...> at /y", which is Markdown syntax
    private static String describe(Comparison comparison) {
        Node control = comparison.getControlDetails().getTarget();
        Node test = comparison.getTestDetails().getTarget();
        String description;
        if ((control instanceof Text || control instanceof Attr) && test != null && test.getNodeType() == control.getNodeType()) {
            description = "Expected " + (control instanceof Text ? "text" : "attribute") + " value '"
                    + control.getNodeValue() + "' but was '" + test.getNodeValue() + "'";
        } else {
            description = new DefaultComparisonFormatter().getDescription(comparison);
            int tail = description.indexOf(" - comparing ");
            description = tail < 0 ? description : description.substring(0, tail);
        }
        String ls = System.lineSeparator();
        return description + ls
                + "Expected XPath: " + comparison.getControlDetails().getXPath() + ls
                + "Actual XPath: " + comparison.getTestDetails().getXPath() + ls + ls;
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
