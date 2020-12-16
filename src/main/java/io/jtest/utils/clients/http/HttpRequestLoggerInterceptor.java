package io.jtest.utils.clients.http;

import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

public class HttpRequestLoggerInterceptor implements HttpRequestInterceptor {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public void process(HttpRequest request, HttpContext context) {
        LOG.debug("---- HTTP REQUEST ----");
        LOG.debug("{}: {}{}", request.getRequestLine().getMethod(), HttpClientContext.adapt(context).getTargetHost().toURI(),
                request.getRequestLine().getUri());
        LOG.debug("PROXY host: {}", () -> {
            RequestConfig config = HttpClientContext.adapt(context).getRequestConfig();
            HttpHost proxy = config.getProxy();
            return proxy != null ? proxy.toURI() : "N/A";
        });
        LOG.debug("Request HEADERS: {}", Arrays.asList(request.getAllHeaders()));
        LOG.debug("Request BODY:{}{}", System::lineSeparator, () -> {
            String content = null;
            HttpEntityEnclosingRequest entityEnclosingRequest;
            if (request instanceof HttpEntityEnclosingRequest) {
                entityEnclosingRequest = (HttpEntityEnclosingRequest) request;
                HttpEntity entity = entityEnclosingRequest.getEntity();
                try {
                    content = EntityUtils.toString(entity);
                } catch (IOException e) {
                    LOG.error(e);
                } finally {
                    try {
                        EntityUtils.consume(entity);
                        if (content != null) {
                            entityEnclosingRequest.setEntity(new StringEntity(content));
                        }
                    } catch (IOException e) {
                        LOG.error(e);
                    }
                }
            }
            return content != null ? content : "N/A";
        });
        LOG.debug("----------------------");
    }
}
