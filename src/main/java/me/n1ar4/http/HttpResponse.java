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

        int prev = -1, current;
        boolean isHeader = true;
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
        boolean isChunked = false;
        for (String line : headerLines) {
            if (!line.isEmpty()) {
                processHeaderLine(line, response);
                if (line.contains("Transfer-Encoding: chunked")) {
                    isChunked = true;
                }
            }
        }

        try (ByteArrayOutputStream bodyBuffer = new ByteArrayOutputStream()) {
            if (isChunked) {
                readChunkedBody(is, bodyBuffer);
            } else {
                readBody(is, bodyBuffer);
            }
            response.body = bodyBuffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    private static void readBody(InputStream is, ByteArrayOutputStream bodyBuffer) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            bodyBuffer.write(buffer, 0, bytesRead);
        }
    }

    private static void readChunkedBody(InputStream is, ByteArrayOutputStream bodyBuffer) throws IOException {
        while (true) {
            String sizeLine = readLine(is);
            int size = Integer.parseInt(sizeLine.trim(), 16);
            if (size == 0) {
                break;
            }
            byte[] buffer = new byte[size];
            int read = 0;
            while (read < size) {
                int result = is.read(buffer, read, size - read);
                if (result == -1) {
                    break;
                }
                read += result;
            }
            bodyBuffer.write(buffer, 0, read);
            readLine(is);
        }
    }

    private static String readLine(InputStream is) throws IOException {
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            lineBuffer.write(b);
            if (b == '\n') {
                break;
            }
        }
        return lineBuffer.toString();
    }

    private static void processHeaderLine(String headerLine, HttpResponse response) {
        if (response.statusCode == 0) {
            String[] parts = headerLine.split(" ");
            if (parts.length < 2) {
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
