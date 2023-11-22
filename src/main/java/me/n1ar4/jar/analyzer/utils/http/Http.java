package me.n1ar4.jar.analyzer.utils.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Lib
 */
public class Http {
    private static final Logger logger = LogManager.getLogger();
    private static int TIMEOUT = 10000;
    public static final String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";

    /**
     * Do Get Request
     *
     * @param url HTTP URL
     * @return HttpResponse
     */
    public static HttpResponse doGet(String url) {
        HttpRequest request = new HttpRequest();
        request.setMethod("GET");
        request.setUrl(url);
        Map<String, String> headers = new HashMap<>();
        setDefaultHeaders(headers);
        request.setHeaders(headers);
        return doRequestInternal(request);
    }

    /**
     * Do Post Request
     *
     * @param url  HTTP URL
     * @param body Body
     * @return HttpResponse
     */
    public static HttpResponse doPost(String url, byte[] body) {
        HttpRequest request = new HttpRequest();
        request.setMethod("POST");
        request.setUrl(url);
        request.setBody(body);
        Map<String, String> headers = new HashMap<>();
        setDefaultHeaders(headers);
        request.setHeaders(headers);
        return doRequestInternal(request);
    }

    /**
     * Do Http Request
     *
     * @param url     HTTP URL
     * @param body    Body
     * @param method  HTTP Method
     * @param headers HTTP Headers
     * @return HttpResponse
     */
    public static HttpResponse doRequest(String url,
                                         byte[] body,
                                         String method,
                                         Map<String, String> headers) {
        HttpRequest request = new HttpRequest();
        request.setMethod(method);
        request.setUrl(url);
        request.setBody(body);
        request.setHeaders(headers);
        return doRequestInternal(request);
    }

    /**
     * Do Http Request
     *
     * @param request HttpRequest
     * @return HttpResponse
     */
    public static HttpResponse doRequest(HttpRequest request) {
        return doRequestInternal(request);
    }

    private static void setDefaultHeaders(Map<String, String> headers) {
        headers.put("User-Agent", ua);
        headers.put("Connection", "close");
    }

    public static void setTimeout(int second) {
        if (second < 1000) {
            TIMEOUT = second * 1000;
        } else {
            TIMEOUT = second;
        }
    }

    /**
     * Do Http Request
     *
     * @param request HttpRequest
     * @return HttpResponse
     */
    private static HttpResponse doRequestInternal(HttpRequest request) {
        try {
            URL requestUrl = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod(request.getMethod());
            if (!request.getMethod().equalsIgnoreCase("GET")) {
                // POST PUT ...
                connection.setDoOutput(true);
            }
            // set timeout
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            // set request header
            if (request.getHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            // set request body
            byte[] reqBody = request.getBody();
            if (reqBody != null && reqBody.length != 0) {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(request.getBody());
                outputStream.flush();
                outputStream.close();
            }
            // get response
            HttpResponse resp = new HttpResponse();
            resp.setStatusCode(connection.getResponseCode());
            resp.setResponseMessage(connection.getResponseMessage());
            // get response headers
            Map<String, String> respHeaders = new HashMap<>();
            for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                if (entries.getKey() == null || entries.getKey().equals("null")) {
                    continue;
                }
                StringBuilder values = new StringBuilder();
                for (String value : entries.getValue()) {
                    values.append(value).append(",");
                }
                values.deleteCharAt(values.length() - 1);
                respHeaders.put(entries.getKey(), values.toString());
            }
            resp.setHeaders(respHeaders);
            // get response body
            ByteArrayOutputStream bao = new ByteArrayOutputStream();

            InputStream is;
            try {
                is = connection.getInputStream();
            } catch (Exception ignored) {
                is = connection.getErrorStream();
            }

            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = is.read(byteChunk)) > 0) {
                bao.write(byteChunk, 0, n);
            }
            resp.setBody(bao.toByteArray());
            // close stream
            bao.close();
            is.close();
            connection.disconnect();
            return resp;
        } catch (Exception ex) {
            logger.error("http error: {}", ex.getMessage());
        }
        return null;
    }

    public static void setSocksProxy(String proxyHost, int proxyPort) {
        System.setProperty("socksProxyHost", proxyHost);
        System.setProperty("socksProxyPort", String.valueOf(proxyPort));
    }
}