package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Handler {
    public void handle(Request request, BufferedOutputStream responseStream) {
        try {
            responseStream.write(request.getResponse().getBytes());
            Files.copy(request.getFilePath(), responseStream);
            responseStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
}
