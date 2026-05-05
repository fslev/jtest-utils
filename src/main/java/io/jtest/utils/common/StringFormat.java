package io.jtest.utils.common;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

/**
 * Substitutes named placeholders inside a string with values from a map. Thin convenience
 * wrapper around Apache Commons Text's {@link StringSubstitutor}.
 *
 * <p>Default placeholder syntax is {@code #[name]}; use the overload with explicit prefix
 * and suffix to pick a different convention. Placeholders whose names are not in the map
 * are left untouched in the output.
 *
 * <pre>{@code
 *   String out = StringFormat.replaceProps(
 *       "Hello, #[who]!",
 *       Map.of("who", "world"));
 *   // out: "Hello, world!"
 * }</pre>
 */
public class StringFormat {

    private StringFormat() {

    }

    /** Default placeholder prefix used by {@link #replaceProps(Object, Map)}. */
    public static final String REPLACE_PREFIX = "#[";
    /** Default placeholder suffix used by {@link #replaceProps(Object, Map)}. */
    public static final String REPLACE_SUFFIX = "]";

    /**
     * Replaces every {@code #[name]} placeholder in {@code source} with the corresponding
     * value from {@code values}.
     *
     * @param source the input — typically a {@link String}; any object's {@link Object#toString()}
     *               is used otherwise
     * @param values placeholder name → replacement; values are stringified via {@code toString}
     * @return a new string with placeholders substituted; placeholders whose name is not
     *         in the map are left as-is
     */
    public static <V> String replaceProps(Object source, Map<String, V> values) {
        return StringSubstitutor.replace(source, values, REPLACE_PREFIX, REPLACE_SUFFIX);
    }

    /**
     * Replaces every {@code prefix + name + suffix} placeholder in {@code source} with the
     * corresponding value from {@code values}.
     *
     * @param source the input — typically a {@link String}; any object's {@link Object#toString()}
     *               is used otherwise
     * @param values placeholder name → replacement; values are stringified via {@code toString}
     * @param prefix placeholder opening delimiter
     * @param suffix placeholder closing delimiter
     * @return a new string with placeholders substituted; placeholders whose name is not
     *         in the map are left as-is
     */
    public static <V> String replaceProps(Object source, Map<String, V> values, String prefix, String suffix) {
        return StringSubstitutor.replace(source, values, prefix, suffix);
    }
}
