package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ObjectMatcherTest {

    @Test
    public void compareNulls() {
        Map<String, Object> symbols = ObjectMatcher.match(null, null, null);
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void doNotMatchWithNull() {
        ObjectMatcher.match(null, null, "val", MatchCondition.DO_NOT_MATCH);
    }

    @Test
    public void compareJsonWithAssignSymbols() {
        String expected = "{\"b\":\"(~[sym1]\"}";
        String actual = "{\"a\":\"val2\",\"b\":\"(val1\"}";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, actual);
        assertEquals("val1", symbols.get("sym1"));
        assertEquals(1, symbols.size());
        symbols = ObjectMatcher.matchJson(null, expected, actual);
        assertEquals("val1", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void matchXmlWithAssignSymbolsWithChildNodeSequence() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<int a=\"(attrValue1\">some text here</int><boolean a=\"boolAttrValue\">false</boolean><str a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, actual, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
        symbols = ObjectMatcher.matchXml(null, expected, actual, MatchCondition.XML_CHILD_NODELIST_SEQUENCE);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
    }

    @Test
    public void compareStringWithManyAssignSymbolsBetweenNewLines() throws InvalidTypeException {
        String expected = "~[prop1],\n This is a ~[prop2]\n ~[prop3]!";
        String actual = "Hello,\n This is a world of many nations \n And 7 continents...!";
        Map<String, Object> symbols = ObjectMatcher.match(null, expected, actual);
        assertEquals("Hello", symbols.get("prop1"));
        assertEquals("world of many nations ", symbols.get("prop2"));
        assertEquals("And 7 continents...", symbols.get("prop3"));
        symbols = ObjectMatcher.matchString(null, expected, actual);
        assertEquals("Hello", symbols.get("prop1"));
        assertEquals("world of many nations ", symbols.get("prop2"));
        assertEquals("And 7 continents...", symbols.get("prop3"));

    }
}
