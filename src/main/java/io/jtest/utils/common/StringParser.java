package io.jtest.utils.common;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lower-level string-parsing helpers used by the matcher implementations to extract or
 * substitute placeholder values. Useful directly when implementing custom matchers or
 * working with the same {@code ~[name]}-style capture syntax outside the standard matcher
 * flow.
 */
public class StringParser {

    private StringParser() {

    }

    /**
     * Returns the contents of every capture group found in {@code source} that matches
     * {@code captureGroupPattern}, in document order.
     *
     * <p>Equivalent to {@link #captureValues(String, Pattern, boolean)} with
     * {@code matchEntirely = false} — multiple non-overlapping matches inside the source
     * each contribute their groups to the result.
     *
     * @param source              the string to search
     * @param captureGroupPattern a pattern with one or more capture groups
     * @return captured group values in the order they were found; empty if no match
     */
    public static List<String> captureValues(String source, Pattern captureGroupPattern) {
        return captureValues(source, captureGroupPattern, false);
    }

    /**
     * Returns the contents of every capture group found in {@code source} that matches
     * {@code captureGroupPattern}.
     *
     * <p>If {@code matchEntirely} is {@code true}, the pattern must match the entire
     * source (not just a substring); otherwise an empty list is returned. Useful when the
     * pattern represents a complete shape rather than a tokenisable fragment.
     *
     * @param source              the string to search
     * @param captureGroupPattern a pattern with one or more capture groups
     * @param matchEntirely       require the pattern to match the entire source string
     * @return captured group values in the order they were found; empty if no match (or
     *         the pattern does not span the whole source when {@code matchEntirely} is set)
     */
    public static List<String> captureValues(String source, Pattern captureGroupPattern, boolean matchEntirely) {
        List<String> values = new ArrayList<>();
        Matcher matcher = captureGroupPattern.matcher(source);
        while (matcher.find()) {
            if (matchEntirely && !matcher.group(0).equals(source)) {
                return values;
            }
            for (int i = 1; i <= matcher.groupCount(); i++) {
                values.add(matcher.group(i));
            }
        }
        return values;
    }

    /**
     * Replaces placeholders of the form {@code prefix + name + suffix} in {@code source}
     * with values supplied by callbacks. Placeholders whose name has no value
     * (per {@code placeholderHasValue}) are left intact.
     *
     * <p>Special case: if {@code source} consists of exactly one placeholder and nothing
     * else, the raw value is returned (not a string), preserving its type. This lets a
     * standalone {@code ~[name]} substitute a non-string object like an {@code Integer}
     * or {@code Map}.
     *
     * @param placeholderNames    names of the placeholders to look up (typically the
     *                            output of {@link #captureValues(String, Pattern)})
     * @param source              the string containing placeholders
     * @param prefix              placeholder opening delimiter (e.g. {@code "~["})
     * @param suffix              placeholder closing delimiter (e.g. {@code "]"})
     * @param placeholderValue    callback that returns the value for a placeholder name
     * @param placeholderHasValue callback that reports whether a placeholder has a value;
     *                            placeholders without a value are left in place
     * @return the substituted string, or — in the standalone-placeholder case — the raw value
     */
    public static Object replacePlaceholders(List<String> placeholderNames, String source, String prefix, String suffix,
                                             Function<String, Object> placeholderValue, Predicate<String> placeholderHasValue) {
        if (placeholderNames.size() == 1 && source.equals(prefix + placeholderNames.get(0) + suffix)) {
            String standalonePlaceholder = placeholderNames.get(0);
            if (!placeholderHasValue.test(standalonePlaceholder)) {
                return source;
            }
            return placeholderValue.apply(standalonePlaceholder);
        }
        String str = source;
        for (String placeholderName : placeholderNames) {
            if (placeholderHasValue.test(placeholderName)) {
                Object val = placeholderValue.apply(placeholderName);
                str = str.replaceFirst(Pattern.quote(prefix + placeholderName + suffix), val != null ? Matcher.quoteReplacement(val.toString()) : "null");
            }
        }
        return str;
    }
}
