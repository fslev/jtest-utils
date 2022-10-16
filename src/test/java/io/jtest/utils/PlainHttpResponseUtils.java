package io.jtest.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jtest.utils.matcher.http.PlainHttpResponse;

public class PlainHttpResponseUtils {

    public static PlainHttpResponse from(String content) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try {
            return mapper.readValue(content, PlainHttpResponse.class);
        } catch (JsonProcessingException e) {
            throw new PlainHttpResponse.ParseException("Cannot parse content", e);
        }
    }
}

