package me.n1ar4.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String message;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private byte[] body;
    private HttpRequest request;

    public static HttpResponse readFromStream(InputStream is) throws IOException {
        HttpResponse response = new HttpResponse();
        response.headers = new HashMap<>();
        response.cookies = new HashMap<>();
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        boolean isHeader = true;
        int prev = -1, current;
        while (isHeader && (current = is.read()) != -1) {
            headerBuffer.write(current);
            if (prev == '\n' && current == '\r') {
                current = is.read();
                if (current == '\n') {
                    isHeader = false;
                } else {
                    headerBuffer.write(current);
                }
            }
            prev = current;
        }
        String headerString = headerBuffer.toString();
        String[] headerLines = headerString.split(Global.LINE_SEP);
        for (String line : headerLines) {
            if (!line.isEmpty()) {
                processHeaderLine(line, response);
            }
        }

        ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream();
        while ((current = is.read()) != -1) {
            bodyBuffer.write(current);
        }
        response.body = bodyBuffer.toByteArray();

        return response;
    }

    private static void processHeaderLine(String headerLine, HttpResponse response) {
        if (response.statusCode == 0) {
            String[] parts = headerLine.split(" ");
            if (parts.length < 3) {
                throw new RuntimeException("http header error");
            }
            response.statusCode = Integer.parseInt(parts[1]);
            response.message = parts[2];
        } else {
            int separator = headerLine.indexOf(":");
            if (separator == -1) {
                return;
            }
            String name = headerLine.substring(0, separator).trim();
            String value = headerLine.substring(separator + 1).trim();
            response.headers.put(name, value);
            if (!name.equalsIgnoreCase(HttpHeaders.SetCookie)) {
                return;
            }
            String[] cookieValueParts = value.split(";", 2);
            if (cookieValueParts.length == 0) {
                return;
            }
            String[] cookieParts = cookieValueParts[0].split("=", 2);
            if (cookieParts.length != 2) {
                return;
            }
            response.cookies.put(cookieParts[0].trim(), cookieParts[1].trim());
        }

    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public byte[] getBody() {
        return body;
    }

    public String getRawHeaders() {
        StringBuilder headersBuilder = new StringBuilder();
        headersBuilder.append(Global.LINE_SEP)
                .append(" ").append(this.statusCode).append(Global.LINE_SEP);
        for (Map.Entry<String, String> entry : this.headers.entrySet()) {
            headersBuilder.append(entry.getKey())
                    .append(": ").append(entry.getValue()).append(Global.LINE_SEP);
        }
        return headersBuilder.toString();
    }

    public String getRawResponse() {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(getRawHeaders()).append(Global.LINE_SEP);
        if (this.body != null) {
            responseBuilder.append(new String(this.body));
        }
        return responseBuilder.toString();
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }
}
