package ru.netology;

import javax.management.Query;
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
import java.util.Optional;
import java.util.concurrent.*;

public class Server {
    static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    public static final String GET = "GET";
    public static final String POST = "POST";
    final var alloweddMethods = List.of(GET, POST);
    private final int threadPoolSize;
    private Map<Query, Handler> map;

    public Server(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void listen(int port) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize);
        threadPool.execute(() -> {
            try {
                handle(new ServerSocket(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handle(ServerSocket serverSocket) {

        while (true) {
            try (
                    final var socket = serverSocket.accept();
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                final var limit = 4096;
                in.mark(limit);
                final var buffer = new byte[limit];
                final var read = in.read(buffer);

                final var requestLineDelimiter = new byte[] {'\r', '\n'};
                final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
                if(requestLineEnd == -1) {
                    badRequest(out);
                    continue;
                }

                final var requestLine =new String(Arrays.copyOf(buffer, requestLineEnd)).split(" "));
                if (requestLine.length != 3) {
                    badRequest(out);
                    continue;
                }
                final var method = requestLine[0];
                if (!alloweddMethods.contains(method)) {
                    badRequest(out);
                    continue;
                }
                System.out.println(method);

                final var path = parts[1];
                if (!path.startWith("/")) {
                    badRequest(out);
                    continue;
                }
                System.out.println(path);

                final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                final var headersStart = requestLineEnd + requestLineDelimiter.length;
                final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                if (headersEnd == -1) {
                    badRequest(out);
                    continue;
                }

                in.reset();
                in.skip(headersStart);

                final var headersBytes = in.readNBytes(headersEnd - headersStart);
                final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
                System.out.println(headers);

                // для GET тела нет
                if (!method.equals(GET)) {
                    in.skip(headersDelimiter.length);
                    // вычитываем Content-Length, чтобы прочитать body
                    final var contentLength = extractHeader(headers, "Content-Length");
                    if (contentLength.isPresent()) {
                        final var length = Integer.parseInt(contentLength.get());
                        final var bodyBytes = in.readNBytes(length);

                        final var body = new String(bodyBytes);
                        System.out.println(body);
                    }
                }

                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//                final var filePath = Path.of(".", "public", path);
//                final var mimeType = Files.probeContentType(filePath);
//
//                // special case for classic
//                if (path.equals("/classic.html")) {
//                    final var template = Files.readString(filePath);
//                    final var content = template.replace(
//                            "{time}",
//                            LocalDateTime.now().toString()
//                    ).getBytes();
//                    out.write(responseClassic(mimeType,content).getBytes());
//                    out.write(content);
//                    out.flush();
//                    continue;
//                }

//                final var length = Files.size(filePath);
//                out.write(response200(mimeType,length).getBytes());
//                Files.copy(filePath, out);
//                out.flush();
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
//        }
    }
    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
//    public String response200 (String mimeType, long length){
//        String response = "HTTP/1.1 200 OK\r\n" +
//                "Content-Type: " + mimeType + "\r\n" +
//                "Content-Length: " + length + "\r\n" +
//                "Connection: close\r\n" +
//                "\r\n";
//        return response;
//    }

//    private static void okRequest (BufferedOutputStream out, String mimeType, long length) throws IOException {
//        out.write((
//                "HTTP/1.1 200 OK\r\n" +
//                "Content-Type: " + mimeType + "\r\n" +
//                "Content-Length: " + length + "\r\n" +
//                "Connection: close\r\n" +
//                "\r\n";
//        ).getBytes());
//        out.flush();
//    }

//    public String responseClassic (String mimeType, byte[] content){
//        String response = "HTTP/1.1 200 OK\r\n" +
//                "Content-Type: " + mimeType + "\r\n" +
//                "Content-Length: " + content.length + "\r\n" +
//                "Connection: close\r\n" +
//                "\r\n";
//        return response;
//    }
    public void addHandler(String method, String path, Handler handler) {
//        if (mapRequest.get(method) == null){
//            mapRequest.put(method, new ConcurrentHashMap<>());
//        }
//        mapRequest.get(method).put(path,handler);
        method.addToMap(path, handler);
    }

    private static void badRequest (BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
    private static int indexOf(byte[] array, byte[] target, int start, int max){
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
