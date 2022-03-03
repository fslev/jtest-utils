package io.jtest.utils.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SpELParserTest {

    @Test
    public void emptySourceTest() {
        assertEquals(SpELParser.parse(""), "");
    }

    @Test
    public void testSimpleParse() {
        String s = "#{T(java.net.IDN).toASCII('testá.com')}";
        assertEquals("xn--test-8na.com", SpELParser.parse(s));
    }

    @Test
    public void invalidSpelExpression() {
        String s = "#{(java.net.IDN).toASCII('testá.com')}";
        assertEquals("#{(java.net.IDN).toASCII('testá.com')}", SpELParser.parse(s));
    }

    @Test
    public void spELGeneratesNull() {
        assertNull(SpELParser.parse("#{T(io.jtest.utils.common.SpELParserTest).returnsNull()}"));
    }

    @Test
    public void testSpelParsingOfMultipleExpressions() {
        String s = "#{T(java.net.IDN).toASCII('testá.com')}#{T(java.net.IDN).toASCII('testá.com')}";
        assertEquals("xn--test-8na.comxn--test-8na.com", SpELParser.parse(s));
    }

    @Test
    public void testSpelParsingOfExpressionContainingEscapedBraces() {
        String s = "#{'abcD\\}EF'.toLowerCase()} and #{'abcD\\}EF'.toLowerCase()}";
        assertEquals("abcd}ef and abcd}ef", SpELParser.parse(s));
    }

    public static Object returnsNull() {
        return null;
    }
}
