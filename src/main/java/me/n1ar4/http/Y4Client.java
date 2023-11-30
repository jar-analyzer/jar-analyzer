package me.n1ar4.http;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Y4Client {
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int HTTP_PORT = 80;
    private static final int HTTPS_PORT = 443;
    private Proxy proxy;
    private final int timeout;

    public static final Y4Client INSTANCE = new Y4Client();

    public Y4Client() {
        this(DEFAULT_TIMEOUT);
    }

    public Y4Client(int timeout) {
        this.timeout = timeout;
    }

    public Y4Client(String proxyHost, int proxyPort) {
        this.timeout = DEFAULT_TIMEOUT;
        this.proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
    }

    public Y4Client(int timeout, String proxyHost, int proxyPort) {
        this.timeout = timeout;
        this.proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
    }

    public HttpResponse get(String url) {
        URL u;
        try {
            u = new URL(url);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        HttpRequest request = new HttpRequest();
        request.setMethod(HttpMethod.GET);
        request.setUrl(u);
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.Connection, "close");
        headers.put(HttpHeaders.UserAgent, HttpRequest.DefaultUA);
        request.setHeaders(headers);
        return request(request);
    }

    public HttpResponse request(HttpRequest request) {
        URL u = request.getUrl();
        String host = u.getHost();
        int port;
        if (u.getPort() <= 0) {
            port = u.getProtocol().equals(HTTPS) ? HTTPS_PORT : HTTP_PORT;
        } else {
            port = u.getPort();
        }

        try {
            Socket socket = getSocket(u, host, port);
            socket.setSoTimeout(this.timeout);

            String rawRequest = request.buildRawRequest();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(rawRequest.getBytes());
            outputStream.flush();

            InputStream is = socket.getInputStream();
            HttpResponse resp = HttpResponse.readFromStream(is);
            resp.setRequest(request);
            socket.close();
            return resp;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Socket getSocket(URL u, String host, int port) throws IOException {
        Socket socket;
        if (u.getProtocol().equals(HTTP)) {
            if (this.proxy == null) {
                socket = new Socket(host, port);
            } else {
                socket = new Socket(this.proxy);
                socket.connect(new InetSocketAddress(host, port));
            }
        } else if (u.getProtocol().equals(HTTPS)) {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            if (this.proxy == null) {
                socket = sslsocketfactory.createSocket(host, port);
            } else {
                Socket plainSocket = new Socket(this.proxy);
                plainSocket.connect(new InetSocketAddress(host, port));
                socket = sslsocketfactory.createSocket(
                        plainSocket, host, port, true);
            }
        } else {
            throw new RuntimeException("unknown protocol");
        }
        return socket;
    }
}
