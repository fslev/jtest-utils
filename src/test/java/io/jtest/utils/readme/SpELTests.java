package io.jtest.utils.readme;

import io.jtest.utils.common.SpELParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SpELTests {

    @Test
    public void parseSpEL() {
        String text = "Current time is: #{new java.util.Date()}";
        assertTrue(SpELParser.parse(text).toString().matches("Current time is:.*"));
    }
}
