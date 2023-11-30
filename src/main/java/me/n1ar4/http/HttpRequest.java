package me.n1ar4.http;

import java.net.URL;
import java.util.Map;

public class HttpRequest {
    public static final String DefaultUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";
    private String urlString;
    private URL url;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private String method;
    private String rawHeaders;
    private String rawRequest;
    private byte[] body;

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRawHeaders() {
        return rawHeaders;
    }

    public void setRawHeaders(String rawHeaders) {
        this.rawHeaders = rawHeaders;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(String rawRequest) {
        this.rawRequest = rawRequest;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String buildRawRequest() {
        StringBuilder requestBuilder = new StringBuilder();
        if (method != null && url != null) {
            String path = url.getPath();
            if (path == null || path.isEmpty()) {
                path = "/";
            }
            requestBuilder.append(method).append(" ")
                    .append(path).append(" ").append(Global.HTTP_VERSION).append(Global.LINE_SEP);
        }
        if (url != null) {
            requestBuilder.append(HttpHeaders.Host)
                    .append(": ").append(url.getHost()).append(Global.LINE_SEP);
        }
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.append(header.getKey())
                        .append(": ").append(header.getValue()).append(Global.LINE_SEP);
            }
        }
        if (cookies != null) {
            StringBuilder cookieHeader = new StringBuilder();
            for (Map.Entry<String, String> cookie : cookies.entrySet()) {
                cookieHeader.append(cookie.getKey())
                        .append("=").append(cookie.getValue()).append("; ");
            }
            if (cookieHeader.length() > 0) {
                cookieHeader.setLength(cookieHeader.length() - 2);
                requestBuilder.append(HttpHeaders.SetCookie)
                        .append(": ").append(cookieHeader).append(Global.LINE_SEP);
            }
        }
        this.rawHeaders = requestBuilder.toString();

        if (body != null) {
            requestBuilder.append(HttpHeaders.ContentLength)
                    .append(": ").append(body.length).append(Global.LINE_SEP);
            requestBuilder.append(Global.LINE_SEP);
            requestBuilder.append(new String(body));
        }else{
            requestBuilder.append(Global.LINE_SEP);
        }
        this.rawRequest = requestBuilder.toString();
        return this.rawRequest;
    }
}
