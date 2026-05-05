package io.jtest.utils.matcher.condition;

/**
 * Flags that tighten or invert the default lenient behavior of
 * {@link io.jtest.utils.matcher.ObjectMatcher} calls.
 *
 * <p>By default, JSON matching tolerates extra fields and out-of-order array elements,
 * XML matching ignores whitespace and child counts, and all scalar comparisons are
 * regex-based. Each flag below switches off one of these defaults.
 *
 * <p>Pass any combination as the trailing varargs of an {@code ObjectMatcher.matchXxx}
 * call.
 */
public enum MatchCondition {
    /**
     * Require the actual JSON object to have no fields beyond those declared in
     * the expected object. Without this flag, extra fields are tolerated.
     */
    JSON_NON_EXTENSIBLE_OBJECT,
    /**
     * Require the actual JSON array to have no elements beyond those declared in
     * the expected array. Without this flag, extra elements are tolerated.
     */
    JSON_NON_EXTENSIBLE_ARRAY,
    /**
     * Require the actual JSON array elements to appear in the same order as the
     * expected array. Without this flag, array order is ignored.
     */
    JSON_STRICT_ORDER_ARRAY,
    /**
     * Require equal child element counts at every level when comparing XML.
     * Without this flag, extra child elements in the actual document are tolerated.
     */
    XML_CHILD_NODELIST_LENGTH,
    /**
     * Require XML child elements to appear in the same order as in the expected
     * document. Without this flag, child order is ignored.
     */
    XML_CHILD_NODELIST_SEQUENCE,
    /**
     * Require equal attribute counts on each XML element. Without this flag,
     * extra attributes on the actual element are tolerated.
     */
    XML_ELEMENT_NUM_ATTRIBUTES,
    /**
     * Invert the assertion: pass if the values do <em>not</em> match, fail if they do.
     *
     * <p>For HTTP responses this flag is ambiguous (which component should be
     * negated?) — use the targeted {@code DO_NOT_MATCH_HTTP_RESPONSE_BY_*} flags
     * instead.
     */
    DO_NOT_MATCH,
    /**
     * For HTTP-response matching: assert that the actual status does <em>not</em>
     * match the expected status, while still matching the other components normally.
     */
    DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS,
    /**
     * For HTTP-response matching: assert that the actual body does <em>not</em>
     * match the expected body, while still matching the other components normally.
     */
    DO_NOT_MATCH_HTTP_RESPONSE_BY_BODY,
    /**
     * For HTTP-response matching: assert that the actual headers do <em>not</em>
     * match the expected headers, while still matching the other components normally.
     */
    DO_NOT_MATCH_HTTP_RESPONSE_BY_HEADERS,
    /**
     * For HTTP-response matching: assert that the actual reason phrase does
     * <em>not</em> match the expected reason, while still matching the other
     * components normally.
     */
    DO_NOT_MATCH_HTTP_RESPONSE_BY_REASON,
    /**
     * Disable regex matching: compare values and field names by literal equality.
     * Useful when the expected value contains regex metacharacters that should be
     * treated as plain text.
     */
    REGEX_DISABLED
}
