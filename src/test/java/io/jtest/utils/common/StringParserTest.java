package io.jtest.utils.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringParserTest {

    @Test
    public void testValueCaptureAndReplacePlaceholders() {
        String prefix = "#[";
        String suffix = "]";
        String source = "test #[var1] string parser #[var2]. Ok ? $ Good.";
        Pattern captureGroupPattern = Pattern.compile(Pattern.quote(prefix) + "(.*?)"
                + Pattern.quote(suffix), Pattern.DOTALL | Pattern.MULTILINE);
        List<String> placeHolders = StringParser.captureValues(source, captureGroupPattern);
        assertEquals("var1", placeHolders.get(0));
        assertEquals("var2", placeHolders.get(1));

        Object result = StringParser.replacePlaceholders(placeHolders, source, prefix, suffix, ph -> ph + "OK", ph -> true);
        assertEquals("test var1OK string parser var2OK. Ok ? $ Good.", result);
    }
}
