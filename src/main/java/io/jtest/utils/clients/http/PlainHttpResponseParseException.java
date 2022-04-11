package io.jtest.utils.clients.http;

import io.jtest.utils.exceptions.InvalidTypeException;

public class PlainHttpResponseParseException extends InvalidTypeException {
    public final static String EXPECTED_FORMAT = "{\n" +
            "  \"status\": <number> | \"<text>\",\n" +
            "  \"body\": {<jsonObject>} | [<jsonArray>] | \"<text>\",\n" +
            "  \"headers\": [{\"<name>\":<value>}, ...],\n" +
            "  \"reason\": \"<text>\"\n" +
            "}";

    public PlainHttpResponseParseException(String msg, Throwable t) {
        super(msg, t);
    }
}
