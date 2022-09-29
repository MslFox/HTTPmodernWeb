package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MethodPOSTHandler {
    public MethodPOSTHandler(Request request, BufferedOutputStream out) throws IOException {
        System.out.println("Поймал POST");
    }
}
