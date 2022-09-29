package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.netology.Errors.*;

public class Server {
    private final int nThreads = 64;
    private final ExecutorService handlersExecutorService = Executors.newFixedThreadPool(nThreads);
    private final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html",
            "/forms.html", "/classic.html", "/events.html", "/events.js", "/favicon.ico",
            "/doneStyle.css",  "/done.html" );
    private final Map<String, Map<String, Handler>> handlerMap = new HashMap<>();


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
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = new Request(in);
            if (!request.isValid()) {
                errorResponse(ERROR400, out);
                return;
            }
            final var handler = getHandler(request);
            if (handler == null) {
                errorResponse(ERROR404, out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void errorResponse(Errors error, BufferedOutputStream out) throws IOException {
        out.write((error +"\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n"
        ).getBytes());
        out.flush();
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
