package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class XmlMatcherTests {

    @Test
    public void compareMalformedXml() throws InvalidTypeException {
        String expected = "<struct><int a=2>3da</int><boolean>false</boolean></struct>";
        String actual = "<struct><int a=2>3da</int><boolean>false</boolean></struct>";
        assertThrows(InvalidTypeException.class, () -> new XmlMatcher(null, expected, actual, null));
    }

    @Test
    public void compareXmlWithAssignpropsAndInvalidRegex() throws InvalidTypeException {
        String expected =
                "<struct><int a=\"~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct><boolean a=\"boolAttrValue\">false</boolean>"
                + "<int a=\"(attrValue1\">some text here</int><str a=\"some result\"><a>sub text</a></str></struct>";
        XmlMatcher matcher = new XmlMatcher(null, expected, actual, null);
        Map<String, Object> props = matcher.match();
        assertEquals("(attrValue1", props.get("sym1"));
        assertEquals("olAttrVal", props.get("sym2"));
        assertEquals("text", props.get("sym3"));
        assertEquals(3, props.size());
    }

    @Test
    public void compareXmlWithAssignpropsAndInvalidRegex_negative() throws InvalidTypeException {
        String expected =
                "<struct><int a=\"X~[sym1]\">some ~[sym3] here</int><boolean a=\"bo~[sym2]ue\">false</boolean></struct>";
        String actual = "<struct><boolean a=\"boolAttrValue\">false</boolean>"
                + "<int a=\"(attrValue1\">some text here</int><str a=\"some result\"><a>sub text</a></str></struct>";
        XmlMatcher matcher = new XmlMatcher(null, expected, actual, null);
        assertThrows(AssertionError.class, matcher::match);
    }

    @Test
    public void compareEmptyXml() throws InvalidTypeException {
        String expected = "<struct></struct>";
        String actual = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><struct></struct>";
        XmlMatcher matcher = new XmlMatcher(null, expected, actual, null);
        Map<String, Object> props = matcher.match();
        assertTrue(props.isEmpty());
    }

    @Test
    public void compareSimpleXml() throws InvalidTypeException {
        String expected = "<struct><int>test</int><boolean>false</boolean></struct>";
        String actual =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><struct><boolean>false</boolean><int>test</int></struct>";
        XmlMatcher matcher = new XmlMatcher(null, expected, actual, null);
        Map<String, Object> props = matcher.match();
        assertTrue(props.isEmpty());
    }

    @Test
    public void doNotMatchSimpleXml() throws InvalidTypeException {
        String expected = "<struct><int>test</int><boolean>true</boolean></struct>";
        String actual =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><struct><boolean>false</boolean><int>test</int></struct>";
        XmlMatcher matcher = new XmlMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH)));
        Map<String, Object> props = matcher.match();
        assertTrue(props.isEmpty());
    }

    @Test
    //also tests assertion error message
    public void doNotMatchSimpleXml_negative() throws InvalidTypeException {
        String expected = "<struct><int>test</int><boolean>false</boolean></struct>";
        String actual =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><struct><boolean>false</boolean><int>test</int></struct>";
        try {
            new XmlMatcher(null, expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))).match();
        } catch (AssertionError e) {
            assertEquals("\nXMLs match!\n" +
                    "Matching is by default done using regular expressions.\n" +
                    "If expected object contains any unintentional regexes, then quote them between \\Q and \\E delimiters.\n\n", e.getMessage());
            return;
        }
        fail("Negative test failed");
    }

    @Test
    //also tests assertion error message
    public void doNotMatchSimpleXml_negative1() throws InvalidTypeException {
        String expected = "<struct><int>test</int><boolean>false</boolean></struct>";
        String actual =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><struct><boolean>false</boolean><int>test</int></struct>";
        try {
            new XmlMatcher("Should not match", expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))).match();
        } catch (AssertionError e) {
            assertEquals("Should not match\n" +
                    "\n" +
                    "XMLs match!\n" +
                    "Matching is by default done using regular expressions.\n" +
                    "If expected object contains any unintentional regexes, then quote them between \\Q and \\E delimiters.\n\n", e.getMessage());
            return;
        }
        fail("Negative test failed");
    }

    @Test
    public void compareComplexXmlWithAssignprops() throws InvalidTypeException {
        String expected = "<bookstore>\n"
                + "    <book price=\"730.54\" ISBN=\"string\" publicationdate=\"~[pubDate]\">\n"
                + "        <author>\n" + "            <last-name>test~[lastName]</last-name>"
                + "        </author>\n" + "        <genre>string</genre>\n" + "    </book>\n"
                + "    <book price=\"~[price]\" ISBN=\"string\">\n"
                + "        <title>string</title>\n" + "        <author>\n"
                + "            <first-name>string</first-name>\n"
                + "            <last-name>string</last-name>\n" + "        </author>\n"
                + "    </book>\n" + "</bookstore>";
        String actual = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<!-- Created with Liquid Studio -->\n"
                + "<bookstore xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "           xsi:noNamespaceSchemaLocation=\"BookStore.xsd\">\n"
                + "    <book price=\"730.54\" ISBN=\"string\" publicationdate=\"2016-02-27\">\n"
                + "        <title>string</title>\n" + "        <author>\n"
                + "            <first-name>string</first-name>\n"
                + "            <last-name>teststring</last-name>\n" + "        </author>\n"
                + "        <genre>string</genre>\n" + "    </book>\n"
                + "    <book price=\"6738.774\" ISBN=\"string\">\n"
                + "        <title>string</title>\n" + "        <author>\n"
                + "            <first-name>string</first-name>\n"
                + "            <last-name>string</last-name>\n" + "        </author>\n"
                + "    </book>\n" + "</bookstore>";
        XmlMatcher matcher = new XmlMatcher(null, expected, actual, null);
        Map<String, Object> props = matcher.match();
        assertEquals("2016-02-27", props.get("pubDate"));
        assertEquals("string", props.get("lastName"));
        assertEquals("6738.774", props.get("price"));
        assertEquals(3, props.size());
    }

    @Test
    public void checkMessageFromXmlCompare() {
        String expected = "<struct>test</struct>";
        String actual = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><struct></struct>";
        assertThrows(AssertionError.class, () -> {
            try {
                new XmlMatcher("Some message", expected, actual, null).match();
            } catch (AssertionError e) {
                assertTrue(e.getMessage().contains("Some message") && e.getMessage().contains("Expected:"));
                throw e;
            }
        });
    }

    @Test
    public void compareXmlChildLength() throws InvalidTypeException {
        String expected = "<struct><int a=\"2\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><int a=\"2\">3da</int><boolean>false</boolean></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Collections.singletonList(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match();
    }

    @Test
    public void doNotMatchXmlChildLength() throws InvalidTypeException {
        String expected = "<struct><int a=\"2\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><int a=\"2\">3da</int><boolean>false</boolean><x>test</x></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_LENGTH, MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void compareXmlChildLength_negative() {
        String expected = "<struct><int a=\"2\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><int a=\"2\">3da</int><boolean>false</boolean><x>test</x></struct>";
        assertThrows(AssertionError.class, () ->
                new XmlMatcher("", expected, actual, new HashSet<>(Collections.singletonList(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match());
    }

    @Test
    public void compareXmlChildOrder() throws InvalidTypeException {
        String expected = "<struct><int a=\"2\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><int a=\"2\">3da</int><boolean>false</boolean></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Collections.singletonList(MatchCondition.XML_CHILD_NODELIST_SEQUENCE))).match();
    }

    @Test
    public void doNotMatchXmlChildOrder() throws InvalidTypeException {
        String expected = "<struct><list><int>3da</int><int>0da</int></list><boolean>.*</boolean></struct>";
        String actual = "<struct><list><int>0da</int><int>3da</int></list><boolean>false</boolean></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Arrays.asList(MatchCondition.XML_CHILD_NODELIST_SEQUENCE, MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void compareXmlChildOrder_negative() {
        String expected = "<struct><list><int>3da</int><int>0da</int></list><boolean>.*</boolean></struct>";
        String actual = "<struct><list><int>0da</int><int>3da</int></list><boolean>false</boolean></struct>";
        assertThrows(AssertionError.class, () ->
                new XmlMatcher("", expected, actual, new HashSet<>(Collections.singleton(MatchCondition.XML_CHILD_NODELIST_SEQUENCE))).match());
    }

    @Test
    public void compareXmlAttributesInclusion() throws InvalidTypeException {
        String expected = "<struct><int a=\"2\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        new XmlMatcher("", expected, actual, null).match();
    }

    @Test
    public void compareXmlAttributesInclusion_with_regex() throws InvalidTypeException {
        String expected = "<struct><int b=\"[0-9]*\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        new XmlMatcher("", expected, actual, null).match();
    }

    @Test
    public void compareXmlAttributesInclusion_negative() {
        String expected = "<struct><int a=\"[0-9]*\" c=\"2\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        assertThrows(AssertionError.class, () -> new XmlMatcher("", expected, actual, null).match());
    }

    @Test
    public void compareXmlAttributesWithLength() throws InvalidTypeException {
        String expected = "<struct><int a=\"[0-9]*\" b=\"3\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Collections.singleton(MatchCondition.XML_ELEMENT_NUM_ATTRIBUTES))).match();
    }

    @Test
    public void compareXmlAttributesFromSiblings() throws InvalidTypeException {
        String expected = "<struct><a attr1=\"x\">lorem</a></struct>";
        String actual = "<struct><a attr1=\"y\">ipsum</a><a attr1=\"x\">lorem</a></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Collections.singleton(MatchCondition.XML_ELEMENT_NUM_ATTRIBUTES))).match();
    }

    @Test
    public void doNotMatchXmlAttributesWithLength() throws InvalidTypeException {
        String expected = "<struct><int a=\"[0-9]*\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        new XmlMatcher("", expected, actual, new HashSet<>(Arrays.asList(MatchCondition.XML_ELEMENT_NUM_ATTRIBUTES, MatchCondition.DO_NOT_MATCH))).match();
    }

    @Test
    public void doNotMatchXmlAttributesWithLength_negative() {
        String expected = "<struct><int a=\"[0-9]*\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        assertThrows(AssertionError.class, () ->
                new XmlMatcher("", expected, actual, new HashSet<>(Arrays.asList(MatchCondition.DO_NOT_MATCH))).match());
    }

    @Test
    public void compareXmlAttributesWithLength_negative() {
        String expected = "<struct><int a=\"[0-9]*\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<struct><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        assertThrows(AssertionError.class, () ->
                new XmlMatcher("", expected, actual, new HashSet<>(Collections.singleton(MatchCondition.XML_ELEMENT_NUM_ATTRIBUTES))).match());
    }

    @Test
    public void matchWithNameSpaces() throws InvalidTypeException {
        String expected = "<struct><int a=\"[0-9]*\">3da</int><boolean>.*</boolean></struct>";
        String actual = "<?xml version=\"1.0\"?>\n" +
                "<struct xmlns:opt=\"http://test.net/test.optional.xml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.test/config.xml\" xsi:schemaLocation=\"http://www.test/config.xml file:///usr/test/java/test/schema/config.xsd\"><boolean>false</boolean><int a=\"2\" b=\"3\">3da</int></struct>";
        new XmlMatcher("", expected, actual, null).match();
    }

    @Test
    public void matchInnerLists() throws InvalidTypeException {
        String expected = "<config>\n" +
                "    <protocols>\n" +
                "      <ldp>" +
                "           <a>1</a>" +
                "      </ldp>\n" +
                "    </protocols>\n" +
                "</config>";
        String actual = "<config>\n" +
                "   <protocols>\n" +
                "       <ldp>" +
                "           <a>2</a>" +
                "       </ldp>" +
                "       <ldp>" +
                "           <a>1</a>" +
                "       </ldp>\n" +
                "       <ldp>" +
                "           <a>3</a>" +
                "       </ldp>\n" +
                "   </protocols>\n" +
                "</config>\n";
        new XmlMatcher("Failed", expected, actual, null).match();
    }

    @Test
    public void matchInnerLists_strict_size() {
        String expected = "<config>\n" +
                "    <protocols>\n" +
                "      <ldp>" +
                "           <a>1</a>" +
                "      </ldp>\n" +
                "    </protocols>\n" +
                "</config>";
        String actual = "<config>\n" +
                "   <protocols>\n" +
                "       <ldp>" +
                "           <a>2</a>" +
                "       </ldp>" +
                "       <ldp>" +
                "           <a>1</a>" +
                "       </ldp>\n" +
                "       <ldp>" +
                "           <a>3</a>" +
                "       </ldp>\n" +
                "   </protocols>\n" +
                "</config>\n";
        assertThrows(AssertionError.class, () ->
                new XmlMatcher("Failed", expected, actual, new HashSet<>(Collections.singleton(MatchCondition.XML_CHILD_NODELIST_LENGTH))).match());
    }

    @Test
    public void matchInnerLists_strict_order() {
        String expected = "<config>\n" +
                "    <protocols>\n" +
                "      <ldp>" +
                "           <a>1</a>" +
                "      </ldp>\n" +
                "    </protocols>\n" +
                "</config>";
        String actual = "<config>\n" +
                "   <protocols>\n" +
                "       <ldp>" +
                "           <a>2</a>" +
                "       </ldp>" +
                "       <ldp>" +
                "           <a>1</a>" +
                "       </ldp>\n" +
                "       <ldp>" +
                "           <a>3</a>" +
                "       </ldp>\n" +
                "   </protocols>\n" +
                "</config>\n";
        assertThrows(AssertionError.class, () ->
                new XmlMatcher("Failed", expected, actual, new HashSet<>(Collections.singleton(MatchCondition.XML_CHILD_NODELIST_SEQUENCE))).match());
    }

    @Test
    public void matchInnerLists_negative() throws InvalidTypeException {
        try {
            String expected = "<config>\n" +
                    "    <protocols>\n" +
                    "      <ldp>" +
                    "           <a attr1=\"test\">1</a>" +
                    "      </ldp>\n" +
                    "    </protocols>\n" +
                    "</config>";
            String actual = "<config>\n" +
                    "   <protocols>\n" +
                    "       <ldp>" +
                    "           <a>2</a>" +
                    "       </ldp>" +
                    "       <ldp>" +
                    "           <a attr1=\"test\">01</a>" +
                    "       </ldp>\n" +
                    "       <ldp>" +
                    "           <a attr2=\"test\" attr1=\"test\">3</a>" +
                    "       </ldp>\n" +
                    "   </protocols>\n" +
                    "</config>\n";
            new XmlMatcher("Failed", expected, actual, null).match();
            fail("XMLs match");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("doesn't match any actual element"));
        }
    }
}
