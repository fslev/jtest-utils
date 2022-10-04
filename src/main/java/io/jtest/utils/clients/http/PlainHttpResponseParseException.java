package io.jtest.utils.clients.http;

import io.jtest.utils.exceptions.InvalidTypeException;

public class PlainHttpResponseParseException extends InvalidTypeException {
    public final static String EXPECTED_FORMAT = "{" + System.lineSeparator() +
            "  \"status\": <number> | \"<text>\"," + System.lineSeparator() +
            "  \"body\": {<jsonObject>} | [<jsonArray>] | \"<text>\"," + System.lineSeparator() +
            "  \"headers\": [{\"<name>\":<value>}, ...]," + System.lineSeparator() +
            "  \"reason\": \"<text>\"" + System.lineSeparator() +
            "}";

    public PlainHttpResponseParseException(String msg, Throwable t) {
        super(msg, t);
    }

    public PlainHttpResponseParseException(String msg) {
        super(msg);
    }
}
