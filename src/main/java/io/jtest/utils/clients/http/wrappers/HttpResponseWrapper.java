package io.jtest.utils.clients.http.wrappers;

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
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpResponseWrapper {

    @JsonProperty(value = "status")
    private String status;
    @JsonProperty(value = "body")
    private Object entity;
    @JsonProperty(value = "reason")
    private String reasonPhrase;
    @JsonProperty(value = "headers")
    private Set<Map.Entry<String, String>> headers;

    public HttpResponseWrapper() {
    }

    public HttpResponseWrapper(Object object) throws HttpResponseParseException {
        if (object instanceof HttpResponse) {
            try {
                fromHttpResponse((HttpResponse) object);
            } catch (IOException e) {
                throw new HttpResponseParseException("Cannot parse org.apache.http.HttpPResponse", e);
            }
        } else {
            try {
                fromObject(object);
            } catch (Exception e) {
                throw new HttpResponseParseException("Cannot parse object:\n" + MessageUtil.cropS(object.toString()) +
                        "\n\nObject should be convertible to HttpResponseWrapper type, such as org.apache.http.HttpPResponse\nor a String, Map or JsonNode" +
                        " with the following JSON format:\n" + HttpResponseParseException.EXPECTED_FORMAT + "\n", e);
            }
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public Set<Map.Entry<String, String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Set<Map.Entry<String, String>> headers) {
        this.headers = headers;
    }

    private void fromObject(Object content) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        HttpResponseWrapper wrapper = content instanceof String ?
                mapper.readValue((String) content, HttpResponseWrapper.class) :
                content instanceof HttpResponseWrapper ? (HttpResponseWrapper) content :
                        mapper.convertValue(content, HttpResponseWrapper.class);
        this.status = wrapper.status;
        this.entity = wrapper.entity;
        this.reasonPhrase = wrapper.reasonPhrase;
        this.headers = wrapper.headers;
    }

    private void fromHttpResponse(HttpResponse response) throws IOException {
        this.status = String.valueOf(response.getStatusLine().getStatusCode());
        this.reasonPhrase = response.getStatusLine().getReasonPhrase();
        this.headers = getHeaders(response);
        String content = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            try {
                content = EntityUtils.toString(entity);
                this.entity = content;
            } catch (Exception e) {
                throw new IOException("Cannot extract entity from HTTP Response", e);
            } finally {
                EntityUtils.consumeQuietly(entity);
                if (content != null) {
                    response.setEntity(new StringEntity(content));
                }
            }
        }
    }

    private Set<Map.Entry<String, String>> getHeaders(HttpResponse response) {
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
