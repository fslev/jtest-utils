package io.jtest.utils.readme;

import io.jtest.utils.common.SpELParser;
import io.jtest.utils.matcher.ObjectMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpELTests {

    @Test
    public void parseSpEL() {
        String text = "Current time is: #{new java.util.Date()}";
        assertTrue(SpELParser.parseQuietly(text).toString().matches("Current time is:.*"));
    }

    @Test
    public void parseSpELAndMatch() {
        String expected = SpELParser.parseQuietly("{\"name\": \"#{'David Jones'.toLowerCase()}\"}").toString();
        String actual = "{\"name\": \"david jones\"}";
        ObjectMatcher.match(null, expected, actual);  // successful matching
    }
}
