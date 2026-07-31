/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NanoHTTPDSecurityTest {
    private TestServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new TestServer();
        server.start(3000, true);
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void rejectsOversizedBodyBeforeReadingIt() throws IOException {
        String response = sendHeaders(
                "POST / HTTP/1.1\r\n"
                        + "Host: 127.0.0.1\r\n"
                        + "Content-Length: 134217728\r\n"
                        + "\r\n");

        assertEquals("HTTP/1.1 413 Payload Too Large", response);
        assertEquals(0, server.requests);
    }

    @Test
    void rejectsInvalidContentLengthFraming() throws IOException {
        assertEquals("HTTP/1.1 400 Bad Request", sendHeaders(
                "POST / HTTP/1.1\r\n"
                        + "Content-Length: -1\r\n\r\n"));
        assertEquals("HTTP/1.1 400 Bad Request", sendHeaders(
                "POST / HTTP/1.1\r\n"
                        + "Content-Length: 9999999999999999999999\r\n\r\n"));
        assertEquals("HTTP/1.1 400 Bad Request", sendHeaders(
                "POST / HTTP/1.1\r\n"
                        + "Content-Length: 1\r\n"
                        + "content-length: 1\r\n\r\n"));
        assertEquals("HTTP/1.1 400 Bad Request", sendHeaders(
                "POST / HTTP/1.1\r\n"
                        + "Content-Length: 0\r\n"
                        + "Transfer-Encoding: chunked\r\n\r\n"));
        assertEquals("HTTP/1.1 411 Length Required", sendHeaders(
                "POST / HTTP/1.1\r\nHost: 127.0.0.1\r\n\r\n"));
        assertEquals(0, server.requests);
    }

    @Test
    void rejectsOversizedHeaders() throws IOException {
        StringBuilder request = new StringBuilder("GET / HTTP/1.1\r\nX-Large: ");
        for (int i = 0; i <= NanoHTTPD.MAX_HEADER_LINE_SIZE; i++) {
            request.append('a');
        }
        request.append("\r\n\r\n");

        assertEquals("HTTP/1.1 431 Request Header Fields Too Large",
                sendHeaders(request.toString()));
        assertEquals(0, server.requests);
    }

    @Test
    void parsesAValidBoundedFormBody() throws IOException {
        String body = "&&name=jar+analyzer&&";
        String response = sendRequest(
                "POST /submit HTTP/1.1\r\n"
                        + "Content-Type: application/x-www-form-urlencoded\r\n"
                        + "Content-Length: " + body.length() + "\r\n"
                        + "\r\n"
                        + body);

        assertEquals("HTTP/1.1 200 OK", response);
        assertEquals(1, server.requests);
        assertEquals("jar analyzer", server.lastName);
    }

    @Test
    void rejectsTooManyFormParameters() throws IOException {
        StringBuilder body = new StringBuilder();
        for (int i = 0; i <= NanoHTTPD.MAX_PARAMETER_COUNT; i++) {
            if (i > 0) {
                body.append('&');
            }
            body.append('p').append(i).append("=v");
        }
        String response = sendRequest(
                "POST /submit HTTP/1.1\r\n"
                        + "Content-Type: application/x-www-form-urlencoded\r\n"
                        + "Content-Length: " + body.length() + "\r\n"
                        + "\r\n"
                        + body);

        assertEquals("HTTP/1.1 413 Payload Too Large", response);
        assertEquals(0, server.requests);
    }

    @Test
    void configuresBoundedConnectionWorkersAndQueue() {
        assertEquals(NanoHTTPD.MAX_CONNECTIONS,
                server.getMaximumWorkerCount());
        assertEquals(NanoHTTPD.MAX_QUEUED_CONNECTIONS,
                server.getConnectionQueueCapacity());
    }

    private String sendHeaders(String request) throws IOException {
        return sendRequest(request);
    }

    private String sendRequest(String request) throws IOException {
        try (Socket socket = connect()) {
            OutputStream out = socket.getOutputStream();
            out.write(request.getBytes(StandardCharsets.ISO_8859_1));
            out.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.ISO_8859_1));
            return reader.readLine();
        }
    }

    private Socket connect() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(
                "127.0.0.1", server.getListeningPort()), 1000);
        socket.setSoTimeout(3000);
        return socket;
    }

    private static final class TestServer extends NanoHTTPD {
        private volatile int requests;
        private volatile String lastName;

        private TestServer() {
            super("127.0.0.1", 0);
        }

        @Override
        public Response serve(IHTTPSession session) {
            requests++;
            List<String> names = session.getParameters().get("name");
            lastName = names == null || names.isEmpty() ? null : names.get(0);
            return newFixedLengthResponse(
                    Response.Status.OK, "text/plain", "ok");
        }

    }
}
