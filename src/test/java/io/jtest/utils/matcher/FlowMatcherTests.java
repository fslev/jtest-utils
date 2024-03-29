package io.jtest.utils.matcher;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FlowMatcherTests {

    @Test
    public void compareNulls() {
        Map<String, Object> symbols = new FlowMatcher().match(null, null, null, null);
        assertTrue(symbols.isEmpty());
    }

    @Test
    public void compareNullWithAssignSymbol() {
        Map<String, Object> symbols = new FlowMatcher().match(null, "~[val]", null, null);
        assertEquals(1, symbols.size());
        assertNull(symbols.get("val"));
    }

    @Test
    public void compareNulls_negative() {
        String expected = "text";
        String actual = null;
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual, null));
    }

    @Test
    public void compareNulls_negative1() {
        String expected = null;
        String actual = "text";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual, null));
    }

    @Test
    public void doNotMatchWithNull() {
        new FlowMatcher().match(null, null, "val", new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH)));
    }

    @Test
    public void doNotMatchWithNull_negative() {
        assertThrows(AssertionError.class, () ->
                new FlowMatcher().match(null, null, null, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))));
    }

    @Test
    public void compareEmptyStrings() {
        new FlowMatcher().match(null, "", "", null);
    }

    @Test
    public void doNotMatchEmptyStrings() {
        new FlowMatcher().match(null, "", "val", new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH)));
    }

    @Test
    public void doNotMatchEmptyStrings_negative() {
        assertThrows(AssertionError.class, () ->
                new FlowMatcher().match(null, "", "", new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))));
    }

    @Test
    public void comparePrimitives() {
        new FlowMatcher().match("", 200, 200, new HashSet<>(Collections.singletonList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY)));
    }

    @Test
    public void comparePrimitivesOfDifferentTypes() {
        new FlowMatcher().match("", 200L, 200, new HashSet<>(Collections.singletonList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY)));
    }

    @Test
    public void comparePrimitives_negative() {
        assertThrows(AssertionError.class, () ->
                new FlowMatcher().match("", 200.0, 200, new HashSet<>(Collections.singletonList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY))));
    }

    @Test
    public void matchPrimitiveWithValueNode() {
        String expectedString = "some value";
        TextNode actualTextNode = new TextNode("some value");
        new FlowMatcher().match("", expectedString, actualTextNode, null);
        int expectedInt = 129;
        IntNode actualIntNode = new IntNode(129);
        new FlowMatcher().match("", expectedInt, actualIntNode, null);
        boolean expectedBoolean = false;
        BooleanNode actualBooleanNode = BooleanNode.getFalse();
        new FlowMatcher().match("", expectedBoolean, actualBooleanNode, null);
    }

    @Test
    public void matchValueNodeWithPrimitive() {
        TextNode expectedTextNode = new TextNode("some value");
        String actualString = "some value";
        new FlowMatcher().match("", expectedTextNode, actualString, null);
        IntNode expectedIntNode = new IntNode(129);
        int actualInt = 129;
        new FlowMatcher().match("", expectedIntNode, actualInt, null);
        BooleanNode expectedBooleanNode = BooleanNode.getFalse();
        boolean actualBoolean = false;
        new FlowMatcher().match("", expectedBooleanNode, actualBoolean, null);
    }

    @Test
    public void compareJsonWithAssignSymbols() {
        String expected = "{\"b\":\"(~[sym1]\"}";
        String actual = "{\"a\":\"val2\",\"b\":\"(val1\"}";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("val1", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFields() {
        String expected = "{\"~[sym1]\":\"2\"}";
        String actual = "{\"a\":\"2\"}";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("a", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFields_in_depth() {
        String expected = "{\"a\":{\"abc-~[sym1]\":{\"o\":\"0\"}}}";
        String actual = "{\"a\":{\"abc-X\":{\"o\":\"1\"},\"abc-Y\":{\"o\":\"0\"},\"abc-X\":{\"o\":\"2\"}}}";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("Y", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFields_in_depth_negative() {
        String expected = "{\"a\":{\"abc-~[sym1]\":{\"o\":\"does not exists\"}}}";
        String actual = "{\"a\":{\"abc-X\":{\"o\":\"1\"},\"abc-Y\":{\"o\":\"0\"},\"abc-X\":{\"o\":\"2\"}}}";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual, null));
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFieldsAndValues() {
        String expected = "{\"~[sym1]\":\"~[sym2]\"}";
        String actual = "{\"a\":\"3\",\"b\":\"100\"}";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("a", symbols.get("sym1"));
        assertEquals("3", symbols.get("sym2"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFieldsWhichHasValuesThatMatch() {
        String expected = "{\"~[sym1]\":\"100\"}";
        String actual = "{\"a\":\"3\",\"x\":\"o\",\"b\":\"100\",\"c\":\"90\"}";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("b", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void compareJsonWithAssignSymbolsOnFieldsWhichHasValuesThatMatch_negative() {
        String expected = "{\"~[sym1]\":\"101\"}";
        String actual = "{\"a\":\"3\",\"x\":\"o\",\"b\":\"100\",\"c\":\"90\"}";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual, null));
    }

    @Test
    public void compareJsonWithMultipleAssignSymbolsOnFieldsAndValues() {
        String expected = "{\"~[sym1]\":\"~[val1]\",\"~[sym2]\":\"~[val2]\"}";
        String actual = "{\"a\":\"3\",\"b\":\"100\"}";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("a", symbols.get("sym1"));
        assertEquals("3", symbols.get("val1"));
        assertEquals("b", symbols.get("sym2"));
        assertEquals("100", symbols.get("val2"));
        assertEquals(4, symbols.size());
    }

    @Test
    public void compareJsonArrayWithAssignSymbolsOnFieldsAndValues() {
        String expected = "[{\"~[sym1]\":\"~[sym2]\"},{\"x\":false}]";
        String actual = "[{\"c\":0},{\"x\":false}]";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("c", symbols.get("sym1"));
        assertEquals("0", symbols.get("sym2"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareJsonNonExtensibleArrayWithAssignSymbolsOnFieldsAndValues() {
        String expected = "[{\"~[sym1]\":\"~[sym2]\"},{\"x\":false}]";
        String actual = "[{\"c\":0},{\"x\":false}]";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY)));
        assertEquals("c", symbols.get("sym1"));
        assertEquals("0", symbols.get("sym2"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void doNotMatchJsonNonExtensibleArrayWithAssignSymbolsOnFieldsAndValues_negative() {
        String expected = "[{\"~[sym1]\":\"~[sym2]\"},{\"x\":false}]";
        String actual = "[{\"c\":0},{\"x\":false}]";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY, MatchCondition.DO_NOT_MATCH))));
    }

    @Test
    public void doNotMatchJsonNonExtensibleArrayWithAssignSymbolsOnFieldsAndValues() {
        String expected = "[{\"~[sym1]\":\"~[sym2]\"},{\"x\":false}]";
        String actual = "[{\"c\":0},{\"x\":false}, 0]";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.JSON_NON_EXTENSIBLE_ARRAY, MatchCondition.DO_NOT_MATCH)));
        assertEquals(0, symbols.size());
    }

    @Test
    public void compareJsonArrayWithAssignSymbolsOnFieldsAndValues1() {
        String expected = "[{\"~[sym1]\":false}]";
        String actual = "[{\"c\":0},{\"x\":false}]";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("x", symbols.get("sym1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void compareXmlWithAssignSymbols() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct><boolean a=\"boolAttrValue\">false</boolean>"
                + "<int a=\"(attrValue1\">some text here</int><str a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
    }

    @Test
    public void compareXmlWithAssignSymbols1() {
        String expected =
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><bookingResponse><bookingId>dlc:~[var1]</bookingId></bookingResponse>";
        String actual = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><bookingResponse><bookingId>dlc:booking:4663740</bookingId></bookingResponse>";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("booking:4663740", symbols.get("var1"));
        assertEquals(1, symbols.size());
    }

    @Test
    public void doNotMatchXmlWithAssignSymbols() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct><boolean a=\"boolAttrValue\">false</boolean>"
                + "<int a=\"(attrValue1\">some text here</int><str1 a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH)));
        assertEquals(0, symbols.size());
    }

    @Test
    public void doNotMatchXmlWithAssignSymbols_negative() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct><boolean a=\"boolAttrValue\">false</boolean>"
                + "<int a=\"(attrValue1\">some text here</int><str a=\"some result\"><a>sub text</a></str></struct>";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))));
    }

    @Test
    public void matchXmlWithAssignSymbolsWithChildNodeSequence() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<int a=\"(attrValue1\">some text here</int><boolean a=\"boolAttrValue\">false</boolean><str a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_SEQUENCE)));
        assertEquals("(attrValue1", symbols.get("sym1"));
        assertEquals("olAttrVal", symbols.get("sym2"));
        assertEquals("text", symbols.get("sym3"));
        assertEquals(3, symbols.size());
    }

    @Test
    public void matchXmlWithAssignSymbolsWithChildNodeSequence_negative() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<boolean a=\"boolAttrValue\">false</boolean><int a=\"(attrValue1\">some text here</int><str a=\"some result\"><a>sub text</a></str></struct>";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_SEQUENCE))));
    }


    @Test
    public void doNotMatchXmlWithAssignSymbolsWithChildNodeSequence() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<boolean a=\"boolAttrValue\">false</boolean><int a=\"(attrValue1\">some text here</int><str a=\"some result\"><a>sub text</a></str></struct>";
        Map<String, Object> props = new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_SEQUENCE, MatchCondition.DO_NOT_MATCH)));
        assertEquals(0, props.size());
    }

    @Test
    public void doNotMatchXmlWithAssignSymbolsWithChildNodeSequence_negative() {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct>"
                + "<int a=\"(attrValue1\">some text here</int><boolean a=\"boolAttrValue\">false</boolean><str a=\"some result\"><a>sub text</a></str></struct>";
        assertThrows(AssertionError.class, () -> new FlowMatcher().match(null, expected, actual,
                new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_SEQUENCE, MatchCondition.DO_NOT_MATCH))));
    }

    @Test
    public void compareString() {
        String expected =
                "<struct<int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual =
                "<struct<int a=\"val1\">some val3 here</int><boolean a=\"boval2ue\">false</boolean></struct>";
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("val1", symbols.get("sym1"));
        assertEquals("val2", symbols.get("sym2"));
        assertEquals("val3", symbols.get("sym3"));
        assertEquals(3, symbols.size());
    }

    @Test
    public void compareJsonConvertibleWithAssignSymbols() {
        List<Map<String, Object>> expected = new ArrayList<>();
        List<Map<String, Object>> actual = new ArrayList<>();
        //Fill expected
        Map<String, Object> map1 = new HashMap<>();
        map1.put("firstName", "~[sym1]");
        map1.put("lastName", "Davids1");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("firstName", "Eric");
        map2.put("lastName", "~[sym2]");
        expected.add(map1);
        expected.add(map2);
        //Fill actual
        map1 = new HashMap<>();
        map1.put("firstName", "Eric");
        map1.put("lastName", "Davids");
        map2 = new HashMap<>();
        map2.put("firstName", "John1");
        map2.put("lastName", "Davids1");
        actual.add(map1);
        actual.add(map2);
        Map<String, Object> symbols = new FlowMatcher().match(null, expected, actual, null);
        assertEquals("John1", symbols.get("sym1"));
        assertEquals("Davids", symbols.get("sym2"));
        assertEquals(2, symbols.size());
    }

    @Test
    public void compareJsonConvertible_extensible() {
        List<Map<String, Object>> expected = new ArrayList<>();
        List<Map<String, Object>> actual = new ArrayList<>();
        //Fill expected
        Map<String, Object> map1 = new HashMap<>();
        map1.put("firstName", "John1");
        map1.put("lastName", "Davids1");
        expected.add(map1);
        //Fill actual
        map1 = new HashMap<>();
        map1.put("firstName", "Eric");
        map1.put("lastName", "Davids");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("firstName", "John1");
        map2.put("lastName", "Davids1");
        actual.add(map1);
        actual.add(map2);
        new FlowMatcher().match(null, expected, actual, null);
    }
}
