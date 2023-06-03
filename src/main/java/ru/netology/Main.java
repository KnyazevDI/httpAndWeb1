package ru.netology;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;


public class Main {
    public static void main(String[] args){
        int threadPoolSize = 64;
        int port = 9999;

        Server server = new Server(threadPoolSize);

        server.addHandler(Method.GET, "/classic.html", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                String template = Files.readString(request.getFilePath());
                byte[] content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();

                responseStream.write(request.getResponse(content).getBytes());
                responseStream.write(content);
                responseStream.flush();

            }
        });

        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                System.out.println("Get messages");
                responseStream.write((
                        "HTTP/1.1 403 Forbidden\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.flush();
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
                System.out.println("Post messages");
                responseStream.write((
                        "HTTP/1.1 401 Unauthorized\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                responseStream.flush();
            }
        });

        server.listen(port);

    }
}


