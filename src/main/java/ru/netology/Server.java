package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int nThreads = 64;
    private final ExecutorService handlersExecutorService = Executors.newFixedThreadPool(nThreads);
    private final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html",
            "/forms.html", "/classic.html", "/events.html", "/events.js", "/favicon.ico");
    private final Map<String, Map<String, Handler>> handlerMap = new HashMap<>();

    public Server() {
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server running...");
            while (true) {
                try {
                    final Socket socket = serverSocket.accept();
                    handlersExecutorService.execute(() -> onConnection(socket));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onConnection(Socket socket) {
        try {
            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            final var out = new BufferedOutputStream(socket.getOutputStream());
            final var requestLine = in.readLine();
            if (requestLine == null) {
                send404(out);
                return;
            }
            final var parts = requestLine.split(" ");
            if (parts.length != 3) {
                send404(out);
                return;
            }
            Request request = new Request(parts);
            final var handler = getHandler(request);

            if (handler == null) {
                send404(out);
                return;
            }
            handler.handle(request, out);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void send404(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n").getBytes());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Handler getHandler(Request request) {
        return handlerMap.get(request.getMethod()).get(request.getPath());
    }

    public List<String> getValidPaths() {
        return validPaths;
    }

    public void addHandler(String method, String path, Handler handler) {
        handlerMap.computeIfAbsent(method, k -> new HashMap<>());
        handlerMap.get(method).put(path, handler);
    }

}
