package me.n1ar4.jar.analyzer.utils.http;

import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String responseMessage;
    private Map<String, String> headers;
    private byte[] body;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", responseMessage='" + responseMessage + '\'' +
                ", headers=" + headers +
                ", body=" + new String(body) +
                '}';
    }
}
