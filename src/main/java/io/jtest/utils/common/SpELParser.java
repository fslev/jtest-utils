package io.jtest.utils.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;
import java.util.regex.Pattern;

public class SpELParser {

    private static final Logger LOG = LogManager.getLogger();
    public static final String PREFIX = "#{";
    public static final String SUFFIX = "}";

    public static final Pattern captureGroupPattern = Pattern.compile(Pattern.quote(PREFIX) + "(.*?)" + Pattern.quote(SUFFIX),
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    public static Object parse(String source) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        List<String> placeholderNames = StringParser.captureValues(source, captureGroupPattern);
        if (placeholderNames.isEmpty()) {
            return source;
        }
        return StringParser.replacePlaceholders(placeholderNames, source, PREFIX, SUFFIX, SpELParser::parseExpression,
                SpELParser::isExpressionValid);
    }

    private static Object parseExpression(String expression) {
        try {
            Expression exp = new SpelExpressionParser().parseExpression(expression);
            return exp.getValue(Object.class);
        } catch (Exception e) {
            LOG.warn("Could not parse SpEL expression: {}", e.getMessage());
            return expression;
        }
    }

    private static Boolean isExpressionValid(String expression) {
        try {
            new SpelExpressionParser().parseExpression(expression).getValue(Object.class);
            return true;
        } catch (Exception e) {
            LOG.warn("SpEL expression '{}' is not valid:\n{}", expression, e.getMessage());
            return false;
        }
    }

}
