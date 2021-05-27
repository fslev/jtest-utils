package io.jtest.utils.clients.http.wrappers;

import io.jtest.utils.exceptions.InvalidTypeException;

public class HttpResponseParseException extends InvalidTypeException {
    public final static String EXPECTED_FORMAT = "{\n" +
            "  \"status\": <number> | \"<text>\",\n" +
            "  \"body\": {<jsonObject>} | [<jsonArray>] | \"<text>\",\n" +
            "  \"headers\": [{\"<name>\":<value>}, ...],\n" +
            "  \"reason\": \"<text>\"\n" +
            "}";

    public HttpResponseParseException(String msg, Throwable t) {
        super(msg, t);
    }
}
