package io.jtest.utils.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Tiny helpers for inspecting Java regular expressions — used by the matchers when they
 * need to decide whether an expected value should be treated as a regex or as a literal
 * string.
 */
public class RegexUtils {

    private RegexUtils() {

    }

    static final List<String> specialRegexCharacters = Arrays.asList("\\", "^", "$", ".", "|", "?", "*", "+", "(", ")", "[", "{");

    /**
     * Returns {@code true} if {@code str} compiles as a Java regular expression,
     * {@code false} if it raises {@link PatternSyntaxException}.
     *
     * <p>Note this returns {@code true} even for trivially literal strings (which
     * compile fine as patterns); it is purely a syntax-validity check, not a "contains
     * regex metacharacters" check. For the latter use
     * {@link #getRegexCharsFromString(String)} and check whether the result is empty.
     *
     * @param str the string to test
     * @return {@code true} if {@code str} is a syntactically valid regex
     */
    public static boolean isRegex(String str) {
        try {
            Pattern.compile(str);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * Returns every regex metacharacter that appears in {@code str}, preserving the order
     * of the static character list (backslash, caret, dollar, dot, pipe, question mark,
     * star, plus, and the opening parenthesis, bracket, and brace). Useful for detecting
     * whether a string is "interesting" as a regex.
     *
     * @param str the string to scan; may be {@code null} or empty
     * @return list of metacharacters present in {@code str}; empty list if {@code str}
     *         is {@code null}, empty, or contains no metacharacters
     */
    public static List<String> getRegexCharsFromString(String str) {
        if (str == null || str.isEmpty()) {
            return Collections.emptyList();
        } else {
            return specialRegexCharacters.stream().filter(str::contains).collect(Collectors.toList());
        }
    }
}
