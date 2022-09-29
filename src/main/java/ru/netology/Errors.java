package ru.netology;

public enum Errors {
    ERROR400("HTTP/1.1 400 Bad Request"),
    ERROR404("HTTP/1.1 404 Not Found");
    private final String error;
    Errors(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return error;
    }
}
