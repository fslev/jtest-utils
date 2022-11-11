package io.jtest.utils.common;

import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.List;
import java.util.regex.Pattern;

public class SpELParser {
    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
    public static final String PREFIX = "#{";
    public static final String SUFFIX = "}";
    private static final Pattern captureGroupPattern = Pattern.compile("(?<!\\\\)" + Pattern.quote(PREFIX) + "(.*?)"
            + "(?<!\\\\)" + Pattern.quote(SUFFIX), Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    public static Object parse(String source) {
        List<String> expressions = extractExpressions(source);
        if (expressions.isEmpty()) {
            return source;
        }
        return StringParser.replacePlaceholders(expressions, source, PREFIX, SUFFIX,
                exp -> parseExpression(unescapeDelimiters(exp)), SpELParser::isExpressionValid);
    }

    public static Object parseExpression(String expression) {
        try {
            Expression exp = SPEL_EXPRESSION_PARSER.parseExpression(expression);
            return exp.getValue(Object.class);
        } catch (Exception e) {
            return expression;
        }
    }

    public static List<String> extractExpressions(String source) {
        return StringParser.captureValues(source, captureGroupPattern);
    }

    public static Boolean isExpressionValid(String expression) {
        try {
            SPEL_EXPRESSION_PARSER.parseExpression(expression).getValue(Object.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String unescapeDelimiters(String value) {
        return value == null || value.isEmpty() ? value :
                value.replaceAll(Pattern.quote("\\" + PREFIX), PREFIX).replaceAll(Pattern.quote("\\" + SUFFIX), SUFFIX);
    }
}
