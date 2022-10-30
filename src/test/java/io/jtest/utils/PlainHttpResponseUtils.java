package io.jtest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.jtest.utils.matcher.http.PlainHttpResponse;

public class PlainHttpResponseUtils {

    public static PlainHttpResponse from(String content) {
        ObjectMapper mapper = new ObjectMapper().setNodeFactory(JsonNodeFactory.withExactBigDecimals(true))
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
                .configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);
        try {
            return mapper.readValue(content, PlainHttpResponse.class);
        } catch (JsonProcessingException e) {
            throw new PlainHttpResponse.ParseException("Cannot parse content", e);
        }
    }
}

