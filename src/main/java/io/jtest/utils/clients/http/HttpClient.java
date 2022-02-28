package io.jtest.utils.clients.http;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class HttpClient {
    private final Integer timeout;
    private final HttpHost proxyHost;
    private final String uri;
    private final Set<Header> headers;
    private final String requestEntity;
    private final Method method;
    private final SSLContext sslContext;
    private final HostnameVerifier hostnameVerifier;
    private final HttpRequestRetryHandler requestRetryHandler;
    private final ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy;
    private final HttpClientBuilder clientBuilder;
    private final CookieStore cookieStore;
    private final CloseableHttpClient client;
    private final HttpRequestBase request;
    private final HttpContext context;

    protected HttpClient(Builder builder) {
        validateMethod(builder);
        validateAddress(builder);
        this.proxyHost = builder.proxyHost;
        this.timeout = builder.timeout;
        try {
            this.uri = builder.address + "/" + (builder.path != null ? builder.path : "") + builder.uriBuilder.build().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.headers = builder.headers;
        this.requestEntity = builder.requestEntity;
        this.method = builder.method;
        this.sslContext = builder.sslContext;
        this.hostnameVerifier = builder.hostnameVerifier;
        this.requestRetryHandler = builder.requestRetryHandler;
        this.serviceUnavailableRetryStrategy = builder.serviceUnavailableRetryStrategy;
        this.clientBuilder = builder.clientBuilder;
        this.cookieStore = builder.cookieStore;
        this.context = builder.context;
        this.client = getClient();
        this.request = getRequest();
    }

    public CloseableHttpResponse execute() throws IOException {
        return client.execute(request, context);
    }

    public Integer getTimeout() {
        return timeout;
    }

    public HttpHost getProxyHost() {
        return proxyHost;
    }

    public String getUri() {
        return this.uri;
    }

    public Set<Header> getHeaders() {
        return headers;
    }

    public String getRequestEntity() {
        return requestEntity;
    }

    public Method getMethod() {
        return method;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public HttpRequestRetryHandler getRequestRetryHandler() {
        return requestRetryHandler;
    }

    public ServiceUnavailableRetryStrategy getServiceUnavailableRetryStrategy() {
        return serviceUnavailableRetryStrategy;
    }

    public HttpContext getContext() {
        return context;
    }

    public void close() throws IOException {
        this.client.close();
    }

    private CloseableHttpClient getClient() {
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        if (timeout != null) {
            configBuilder.setConnectTimeout(timeout).setConnectionRequestTimeout(timeout)
                    .setSocketTimeout(timeout);
        }
        if (proxyHost != null) {
            configBuilder.setProxy(proxyHost);
        }
        clientBuilder.setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext == null ?
                        defaultSslContext() : sslContext, hostnameVerifier == null ?
                        new NoopHostnameVerifier() : hostnameVerifier))
                .setDefaultRequestConfig(configBuilder.build());
        if (requestRetryHandler != null) {
            clientBuilder.setRetryHandler(requestRetryHandler);
        }
        if (serviceUnavailableRetryStrategy != null) {
            clientBuilder.setServiceUnavailableRetryStrategy(serviceUnavailableRetryStrategy);
        }
        if (cookieStore != null) {
            clientBuilder.setDefaultCookieStore(cookieStore);
        }
        return clientBuilder.addInterceptorLast(new HttpResponseLoggerInterceptor())
                .addInterceptorLast(new HttpRequestLoggerInterceptor()).build();
    }

    private SSLContext defaultSslContext() {
        SSLContext ctx;
        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()},
                    new SecureRandom());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return ctx;
    }

    private HttpRequestBase getRequest() {

        HttpRequestBase request;
        switch (method) {
            case GET:
                request = new HttpGet(uri);
                break;
            case POST:
                HttpPost post = new HttpPost(uri);
                post.setEntity(new StringEntity(requestEntity != null ? requestEntity : "", StandardCharsets.UTF_8));
                request = post;
                break;
            case PUT:
                HttpPut put = new HttpPut(uri);
                put.setEntity(new StringEntity(requestEntity != null ? requestEntity : "", StandardCharsets.UTF_8));
                request = put;
                break;
            case DELETE:
                if (requestEntity == null || requestEntity.isEmpty()) {
                    request = new HttpDelete(uri);
                } else {
                    HttpDeleteWithBody deleteWithBody = new HttpDeleteWithBody(uri);
                    deleteWithBody.setEntity(new StringEntity(requestEntity, StandardCharsets.UTF_8));
                    request = deleteWithBody;
                }
                break;
            case PATCH:
                HttpPatch patch = new HttpPatch(uri);
                patch.setEntity(new StringEntity(requestEntity != null ? requestEntity : "", StandardCharsets.UTF_8));
                request = patch;
                break;
            case OPTIONS:
                request = new HttpOptions(uri);
                break;
            case TRACE:
                request = new HttpTrace(uri);
                break;
            case HEAD:
                request = new HttpHead(uri);
                break;
            default:
                throw new IllegalStateException("Invalid HTTP method");
        }
        addHeaders(request);
        return request;
    }

    private void validateMethod(Builder builder) {
        if (builder.method == null) {
            throw new IllegalStateException("HTTP Method missing");
        }
    }

    private void validateAddress(Builder builder) {
        if (builder.address == null) {
            throw new IllegalStateException("HTTP Address missing");
        }
    }

    private void addHeaders(HttpRequestBase request) {
        headers.forEach(request::addHeader);
    }

    @Override
    public String toString() {
        return "HttpClient{" + "timeout=" + timeout + ", proxyHost=" + proxyHost + ", uri='"
                + uri + ", headers=" + headers + ", requestEntity='"
                + requestEntity + '\'' + ", method=" + method + '}';
    }

    public static class Builder {
        private Integer timeout;
        private HttpHost proxyHost;
        private String address;
        private String path;
        private final URIBuilder uriBuilder = new URIBuilder();
        private final Set<Header> headers = new HashSet<>();
        private String requestEntity;
        private Method method;
        private SSLContext sslContext;
        private HostnameVerifier hostnameVerifier;
        private HttpRequestRetryHandler requestRetryHandler;
        private ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy;
        private HttpClientBuilder clientBuilder = HttpClients.custom();
        private CookieStore cookieStore;
        private HttpContext context;

        public Builder proxy(String proxyHost, int proxyPort, String proxyScheme) {
            this.proxyHost = new HttpHost(proxyHost, proxyPort, proxyScheme);
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder address(String address) {
            if (address != null) {
                this.address = address.replaceFirst("/*$", "");
            }
            return this;
        }

        public Builder path(String path) {
            if (path != null) {
                this.path = path.replaceFirst("^/*", "");
            }
            return this;
        }

        public Builder header(String name, String value) {
            return header(name, value, false);
        }

        public Builder header(String name, String value, boolean replace) {
            if (replace) {
                Set<Header> duplicatedHeaders = this.headers.stream().filter(h -> h.getName().equals(name)).collect(Collectors.toSet());
                this.headers.removeAll(duplicatedHeaders);
            }
            this.headers.add(new BasicHeader(name, value));
            return this;
        }

        public Builder nonEmptyHeader(String name, String value) {
            return nonEmptyHeader(name, value, false);
        }

        public Builder nonEmptyHeader(String name, String value, boolean replace) {
            return value != null && !value.isEmpty() ? header(name, value, replace) : this;
        }

        public Builder headers(Map<String, String> headers) {
            return headers(headers, false);
        }

        public Builder headers(Map<String, String> headers, boolean replace) {
            if (headers != null) {
                headers.forEach((k, v) -> header(k, v, replace));
            }
            return this;
        }

        public Builder queryParam(String name, String value) {
            this.uriBuilder.addParameter(name, value);
            return this;
        }

        public Builder nonEmptyQueryParam(String name, String value) {
            return value != null && !value.isEmpty() ? queryParam(name, value) : this;
        }

        public Builder queryParams(Map<String, String> queryParams) {
            if (queryParams != null) {
                List<NameValuePair> paramsList = new ArrayList<>();
                queryParams.forEach((k, v) -> paramsList.add(new BasicNameValuePair(k, v)));
                this.uriBuilder.addParameters(paramsList);
            }
            return this;
        }

        public Builder entity(String entity) {
            this.requestEntity = entity;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder sslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder requestRetryHandler(HttpRequestRetryHandler requestRetryHandler) {
            this.requestRetryHandler = requestRetryHandler;
            return this;
        }

        public Builder serviceUnavailableRetryStrategy(ServiceUnavailableRetryStrategy serviceRetryStrategy) {
            this.serviceUnavailableRetryStrategy = serviceRetryStrategy;
            return this;
        }

        public Builder clientBuilder(HttpClientBuilder clientBuilder) {
            this.clientBuilder = clientBuilder;
            return this;
        }

        public Builder cookieStore(CookieStore cookieStore) {
            this.cookieStore = cookieStore;
            return this;
        }

        public Builder context(HttpContext context) {
            this.context = context;
            return this;
        }

        public HttpClient build() {
            return new HttpClient(this);
        }
    }
}

class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}

class DefaultTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}