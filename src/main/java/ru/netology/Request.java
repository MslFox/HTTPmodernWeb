package ru.netology;

import java.io.IOException;

public class Request {
    private final String method;
    private final String path;
public Request(String[] parts) throws IOException {
        this.method = parts[0];
        this.path = parts[1];
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "Request{" +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}

