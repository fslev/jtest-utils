package io.jtest.utils.clients.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import ro.skyah.util.MessageUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlainHttpResponse {

    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "body")
    private Object entity;
    @JsonProperty(value = "reason")
    private String reasonPhrase;
    @JsonProperty(value = "headers")
    private Set<Map.Entry<String, String>> headers;

    private PlainHttpResponse() {
    }

    private PlainHttpResponse(String status, String reasonPhrase, Object entity, Set<Map.Entry<String, String>> headers) {
        this.status = status;
        this.reasonPhrase = reasonPhrase;
        this.entity = entity;
        this.headers = headers;
    }

    public static PlainHttpResponse from(Object object) throws PlainHttpResponseParseException {
        if (object instanceof HttpResponse) {
            try {
                return fromHttpResponse((HttpResponse) object);
            } catch (IOException e) {
                throw new PlainHttpResponseParseException("Cannot parse org.apache.http.HttpPResponse", e);
            }
        } else {
            try {
                return fromObject(object);
            } catch (Exception e) {
                throw new PlainHttpResponseParseException("Cannot convert to PlainHttpResponse:\n" + MessageUtil.cropS(object.toString()) +
                        "\n\nObject should either be of type org.apache.http.HttpPResponse or of any other JSON convertible types with the format:\n"
                        + PlainHttpResponseParseException.EXPECTED_FORMAT + "\n", e);
            }
        }
    }

    public String getStatus() {
        return status;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Object getEntity() {
        return entity;
    }

    public Set<Map.Entry<String, String>> getHeaders() {
        return headers;
    }

    private static PlainHttpResponse fromObject(Object content) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        return content instanceof String ?
                mapper.readValue((String) content, PlainHttpResponse.class) :
                content instanceof PlainHttpResponse ? (PlainHttpResponse) content :
                        mapper.convertValue(content, PlainHttpResponse.class);
    }

    private static PlainHttpResponse fromHttpResponse(HttpResponse response) throws IOException {
        String status = String.valueOf(response.getStatusLine().getStatusCode());
        String reasonPhrase = response.getStatusLine().getReasonPhrase();
        Set<Map.Entry<String, String>> headers = extractHeaders(response);
        String content = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IOException("Cannot extract entity from HTTP Response", e);
            } finally {
                EntityUtils.consumeQuietly(entity);
                if (content != null) {
                    response.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
                }
            }
        }
        return new PlainHttpResponse(status, reasonPhrase, content, headers);
    }

    private static Set<Map.Entry<String, String>> extractHeaders(HttpResponse response) {
        Set<Map.Entry<String, String>> headers = new HashSet<>();
        for (Header h : response.getAllHeaders()) {
            headers.add(new AbstractMap.SimpleEntry<>(h.getName(), h.getValue()));
        }
        return headers;
    }

    @Override
    public String toString() {
        return "{" +
                (status != null ? "status=" + status : "") +
                (reasonPhrase != null ? ", reason='" + reasonPhrase + '\'' : "") +
                (entity != null ? ", body='" + entity + '\'' : "") +
                (headers != null ? ", headers=" + headers : "") +
                '}';
    }
}
