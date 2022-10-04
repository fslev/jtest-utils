package io.jtest.utils.clients.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpResponseLoggerInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void process(HttpResponse response, HttpContext context) {
        LOG.debug(System.lineSeparator() + "--- HTTP RESPONSE ---" + System.lineSeparator() + "Response STATUS: {}" +
                        System.lineSeparator() + "Response HEADERS: {}" + System.lineSeparator() + "Response BODY:" +
                        System.lineSeparator() + "{}" + System.lineSeparator(),
                response::getStatusLine, () -> Arrays.asList(response.getAllHeaders()), () -> {
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        return null;
                    }
                    String content = null;
                    try {
                        content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                        return content;
                    } catch (IOException e) {
                        return "Cannot consume HTTP response: " + e.getMessage();
                    } finally {
                        try {
                            EntityUtils.consume(entity);
                        } catch (IOException e) {
                            LOG.error(e);
                        }
                        if (content != null) {
                            response.setEntity(new StringEntity(content, StandardCharsets.UTF_8));
                        }
                    }
                });
    }
}
