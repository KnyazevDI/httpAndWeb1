package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Server {
    private final int threadPoolSize;
    private final Map<String, Map<String, Handler>> mapRequest = new ConcurrentHashMap<>();

    public Server(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        threadPool.execute(() -> {
            try {
                connection(new ServerSocket(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addHandler(String method, String path, Handler handler) {
        if (mapRequest.get(method) == null) {
            mapRequest.put(method, new ConcurrentHashMap<>());
        }
        mapRequest.get(method).put(path, handler);
    }

    private void connection(ServerSocket serverSocket) {
        while (true) {
            try (
                    final var socket = serverSocket.accept();
                    final var in = socket.getInputStream();
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                Request requestLine = Request.parsingHttpRequest(in);
                Map<String, Handler> path = mapRequest.get(requestLine != null ? requestLine.getMethod() : null);
                if (path == null) {
                    badRequest.handle(requestLine, out);
                    return;
                }
                Handler handler = path.get(requestLine != null ? requestLine.getPath() : null);
                if (handler == null) {
                    badRequest.handle(requestLine, out);
                    return;
                }
                handler.handle(requestLine, out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private final Handler badRequest = ((request, out) -> {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });
}
