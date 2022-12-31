package io.jtest.utils.common;

import org.apache.commons.text.StringSubstitutor;

import java.util.Map;

public class StringFormat {

    private StringFormat() {

    }

    public static final String REPLACE_PREFIX = "#[";
    public static final String REPLACE_SUFFIX = "]";

    public static <V> String replaceProps(Object source, Map<String, V> values) {
        return StringSubstitutor.replace(source, values, REPLACE_PREFIX, REPLACE_SUFFIX);
    }

    public static <V> String replaceProps(Object source, Map<String, V> values, String prefix, String suffix) {
        return StringSubstitutor.replace(source, values, prefix, suffix);
    }
}