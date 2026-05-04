package io.jtest.utils.matcher;

import io.jtest.utils.exceptions.InvalidTypeException;
import io.jtest.utils.matcher.condition.MatchCondition;
import io.jtest.utils.matcher.http.PlainHttpResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class ObjectMatcher {

    private ObjectMatcher() {

    }

    /**
     * Matches objects as Json, Xml or String in that order
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> match(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        return new FlowMatcher().match(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions)));
    }

    public static Map<String, Object> matchJson(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new JsonMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> matchXml(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new XmlMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches two objects as strings<br>
     * Expected could contain regular expressions.<br>
     * If expected contains special regex characters and you want to match them as simple characters, just quote the expression using \Q and \E.
     *
     * @return properties captured after the match
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> matchString(String message, Object expected, Object actual, MatchCondition... matchConditions) {
        try {
            return new StringMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Matches two objects representing HTTP responses<br>
     * MatchCondition.DO_NOT_MATCH is ambiguous in this case. Use MatchCondition.DO_NOT_MATCH_HTTP_RESPONSE_BY_STATUS, ...BY_BODY, etc<br>
     *
     * @param expected a PlainHttpResponse object<br>
     *                 <p>
     *                 {"status": <number> | "<text>", <br>
     *                 "body": <json>} | <xml>] | "<text>", <br>
     *                 "headers": [{"name":"value"}, ...], <br>
     *                 "reason": "<text>" <br>
     *                 } <br>
     *                 All fields are optional <br>
     * @param actual   a PlainHttpResponse object
     * @return properties captured after the match <br>
     * Expected object can contain placeholders for capturing values from the actual object: ~[placeholder_name]
     */
    public static Map<String, Object> matchHttpResponse(String message, PlainHttpResponse expected, PlainHttpResponse actual, MatchCondition... matchConditions) {
        try {
            return new HttpResponseMatcher(message, expected, actual, new HashSet<>(Arrays.asList(matchConditions))).match();
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }
}
