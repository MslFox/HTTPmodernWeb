package ru.netology;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        server.getValidPaths().
                forEach(path -> server.addHandler("GET", path, (request, responseStream) -> {
                    try {
                        new MethodGETHandler(request, responseStream);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }));
        server.addHandler("POST", "/forms.html", (request, responseStream) -> {
            try {
                new MethodPOSTHandler(request, responseStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        server.listen(9999);
    }
}
