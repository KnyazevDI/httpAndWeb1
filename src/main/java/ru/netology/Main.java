package ru.netology;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args){
        int threadPoolSize = 64;
        int port = 9999;

        Server server = new Server(threadPoolSize);

        server.addHandler("GET", "/classic.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                String template = Files.readString(request.getFilePath());
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                responseStream.write(request.getResponse(content, request.getFilePath()).getBytes());
                responseStream.write(content);
                responseStream.flush();
            }
        });

        server.addHandler("GET", "/index.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) {
                try {
                    Path filePath = Path.of(".", "public", request.getPath());
                    String mimeType = Files.probeContentType(filePath);
                    long sizeFile = Files.size(filePath);
                    outResponse(mimeType, sizeFile, out, filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        server.addHandler("POST", "/messages", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) {
                System.out.println("Post method");
                try {
                    Path filePath = Path.of(".", "public", request.getPath());
                    String mimeType = Files.probeContentType(filePath);
                    long sizeFile = Files.size(filePath);
                    outResponse(mimeType, sizeFile, out, filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        server.listen(port);

    }

    public static void outResponse(String mimeType, long size, BufferedOutputStream bufferedOutputStream, Path path) throws IOException {
        bufferedOutputStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + size + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(path, bufferedOutputStream);
        bufferedOutputStream.flush();
    }
}


