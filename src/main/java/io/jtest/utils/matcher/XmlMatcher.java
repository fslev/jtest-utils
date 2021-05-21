package io.jtest.utils.matcher;

import io.jtest.utils.common.RegexUtils;
import io.jtest.utils.common.XmlUtils;
import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.comparators.xml.CustomXmlDiffEvaluator;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.DifferenceEvaluators;
import org.xmlunit.diff.ElementSelector;
import ro.skyah.util.MessageUtil;

import javax.xml.transform.TransformerException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.jtest.utils.common.XmlUtils.toNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

public class XmlMatcher extends AbstractObjectMatcher<Node> {

    private final CustomXmlDiffEvaluator diffEvaluator;

    public XmlMatcher(String message, Object expected, Object actual, Set<MatchCondition> matchConditions) throws InvalidTypeException {
        super(message, expected, actual, matchConditions);
        this.message += "\n\nEXPECTED:\n" + MessageUtil.cropXXL(toString(this.expected))
                + "\n\nBUT GOT ACTUAL:\n" + MessageUtil.cropXXL(toString(this.actual)) + "\n";
        this.diffEvaluator = new CustomXmlDiffEvaluator(this.matchConditions);
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
        try {
            return toNode(value);
        } catch (Exception e) {
            throw new InvalidTypeException("Invalid XML");
        }
    }

    @Override
    public Map<String, Object> match() {
        if (matchConditions.contains(MatchCondition.DO_NOT_MATCH)) {
            matchConditions.remove(MatchCondition.DO_NOT_MATCH);
            try {
                positiveMatch();
            } catch (AssertionError e) {
                return new HashMap<>();
            }
            fail(negativeMatchMessage);
        }
        return positiveMatch();
    }

    private Map<String, Object> positiveMatch() {
        try {
            assertThat(message, actual, isSimilarTo(expected).ignoreWhitespace()
                    .withNodeMatcher(new DefaultNodeMatcher(new ByNameAndSiblingOfSameNameAndTypeSelector()))
                    .withDifferenceEvaluator(
                            DifferenceEvaluators.chain(diffEvaluator)));
        } catch (AssertionError e) {
            debugIfXmlContainsUnintentionalRegexChars(expected);
            throw e;
        }
        return diffEvaluator.getGeneratedProperties();
    }

    private class ByNameAndSiblingOfSameNameAndTypeSelector implements ElementSelector {
        @Override
        public boolean canBeCompared(Element controlElement, Element testElement) {
            if (controlElement == null || testElement == null || !controlElement.getNodeName().equals(testElement.getNodeName())) {
                return false;
            }
            if (hasSiblingsWithSameNameAndType(testElement)) {
                try {
                    new XmlMatcher(message, controlElement, testElement, matchConditions).match();
                } catch (AssertionError | InvalidTypeException e) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean hasSiblingsWithSameNameAndType(Element element) {
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

    private static void debugIfXmlContainsUnintentionalRegexChars(Node xml) {
        if (LOG.isDebugEnabled()) {
            try {
                Map<String, List<String>> specialRegexChars = XmlUtils.walkXmlAndProcessNodes(xml, nodeValue -> {
                    List<String> regexChars = RegexUtils.getRegexCharsFromString(nodeValue);
                    return regexChars.isEmpty() ? null : regexChars;
                });
                if (!specialRegexChars.isEmpty()) {
                    String prettyResult = specialRegexChars.entrySet().stream().map(e -> e.getKey() + " contains: " + e.getValue().toString())
                            .collect(Collectors.joining("\n"));
                    LOG.debug(" \n\n Comparison mechanism failed while comparing XMLs." +
                                    " \n One reason for this, might be that XML may have unintentional regex special characters. " +
                                    "\n If so, try to quote them by using \\Q and \\E or simply \\" +
                                    "\n Found the following list of special regex characters inside expected:\n\n{}\n\nExpected:\n{}\n",
                            prettyResult, xml);
                }
            } catch (Exception e) {
                LOG.debug("Cannot extract special regex characters from xml");
            }
        }
    }
}
