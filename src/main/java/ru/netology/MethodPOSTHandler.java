package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MethodPOSTHandler {
    public MethodPOSTHandler(Request request, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", request.getPath());
        final var mimeType = Files.probeContentType(filePath);
        if (request.getPath().equals("/done.html"))
        {
            final var template = Files.readString(Path.of(".", "public", "/done.html"));
            final var content = template
                    .replace("{user}",
                            request.getBodyParam("login").get(0).getValue())
                    .replace("{password}",
                            request.getBodyParam("password").get(0).getValue())
                    .replace("{path}", request.getPath())
                    .replace("{queryString}", URLDecoder.decode(request.getQueryString(), StandardCharsets.UTF_8))
                    .replace("{bodyString}", URLDecoder.decode(request.getBody(), StandardCharsets.UTF_8))
                    .replace("{allParams}", request.getPostParams().toString())

                    .getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
        }

    }
}
