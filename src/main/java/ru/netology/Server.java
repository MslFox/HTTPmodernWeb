package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService handlersExecutorService;
    private final ServerSocket serverSocket;

    private final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html",
            "/forms.html", "/classic.html", "/events.html", "/events.js", "/favicon.ico");

    public Server(int port, int nThreads) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.handlersExecutorService = Executors.newFixedThreadPool(nThreads);
        runServer();
    }

    public void runServer() {
        System.out.println("Server running...");
        while (true) {
            try {
                final Socket socket = serverSocket.accept();
                handleConnection(socket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void handleConnection(Socket socket) {
        handlersExecutorService.execute(() -> {
            try {
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
                final var requestLine = in.readLine();
                System.out.println("Request received " + requestLine + " Thread -> " + Thread.currentThread().getName());
                if (requestLine == null) {
                    return;
                }
                final var parts = requestLine.split(" ");
                if (parts.length != 3) {
                    return;
                }

                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    out.write((
                            "HTTP/1.1 404 Not Found\r\n" +
                                    "Content-Length: 0\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.flush();
                    return;
                }

                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);
                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();
                    out.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + content.length + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    out.write(content);
                    out.flush();
                    return;
                }
                final var length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}

