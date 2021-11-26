package io.jtest.utils.common;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringFormatTest {

    @Test
    public void testPropertiesReplace() {
        String source = "{\"a\":\"some #[var 1]\",\"b\":#[var2]}";
        Map<String, Object> props = new HashMap<>();
        props.put("var 1", "value here");
        props.put("var2", "\"test\"");
        String expected = "{\"a\":\"some value here\",\"b\":\"test\"}";
        assertEquals(expected, StringFormat.replaceProps(source, props));
    }

    @Test
    public void testPropertiesReplaceWithNulls() {
        String source = "{\"a\":\"some #[var 1]\",\"b\":#[var2]}";
        Map<String, Object> props = new HashMap<>();
        props.put("var 1", null);
        props.put("var2", true);
        String expected = "{\"a\":\"some #[var 1]\",\"b\":true}";
        assertEquals(expected, StringFormat.replaceProps(source, props));
    }
}
