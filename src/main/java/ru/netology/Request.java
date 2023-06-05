package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static java.lang.System.out;

public class Request {
    private final static int LIMIT = 4096;
    private final String method;
    private final InputStream body;
    private final String path;
    private final List<String> headers;
    private final Map<String, List<String>> post;
    private final Map<String, List<String>> query;

    public Request(String method, InputStream body, String path, List<String> headers, Map<String, List<String>> post,
                   Map<String, List<String>> query) {
        this.method = method;
        this.body = body;
        this.path = path;
        this.headers = headers;
        this.post = post;
        this.query = query;
    }

    public static Request parsingHttpRequest(InputStream in) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(in);
        bufferedInputStream.mark(LIMIT);
        final var buffer = new byte[LIMIT];
        final var read = bufferedInputStream.read(buffer);

        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return badRequest();
        }

        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return badRequest();
        }

        final var method = requestLine[0];
        if (!method.equals("GET") && !method.equals("POST")) {
            return badRequest();
        }

        final var pathAndQuery  = requestLine[1];
        if (!pathAndQuery.startsWith("/")) {
            return badRequest();
        }

        String path;
        Map<String, List<String>> query;
        if (pathAndQuery.contains("?")) {
            String[] urlPathAndQuery = pathAndQuery.split("/?");
            path = urlPathAndQuery[0];
            String pathQuery = urlPathAndQuery[1];
            query = getQueryParams(pathQuery);
        } else {
            path = pathAndQuery;
            query = null;
        }

        byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        int headersStarted = requestLineEnd + requestLineDelimiter.length;
        int headersEnd = indexOf(buffer, headersDelimiter, headersStarted, read);
        if (headersEnd == -1) {
            return badRequest();
        }
        bufferedInputStream.reset();
        bufferedInputStream.skip(headersStarted);
        final var headersBytes = bufferedInputStream.readNBytes(headersEnd - headersStarted);
        List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        Map<String, List<String>> post = null;
        if (!method.equals("GET")) {
            bufferedInputStream.skip(headersDelimiter.length);
            Optional<String> contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                int length = Integer.parseInt(contentLength.get());
                byte[] bodyBytes = bufferedInputStream.readNBytes(length);
                String body = new String(bodyBytes);
                if (body.contains("=")) {
                    post = getQueryParams(body);
                }
            }
        }
        return new Request(method, in, path, headers, post, query);
    }

    private static Map<String, List<String>> getQueryParams(String url) {
        Map<String, List<String>> queryParams = new HashMap<>();
        List<NameValuePair> params = URLEncodedUtils.parse(url, Charset.defaultCharset(), '&');
        for (NameValuePair param : params) {
            if (queryParams.containsKey(param.getName())) {
                queryParams.get(param.getName()).add(param.getValue());
            } else {
                List<String> values = new ArrayList<>();
                values.add(param.getValue());
                queryParams.put(param.getName(), values);
            }
        }
        out.println(queryParams);
        return queryParams;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }


    public String getResponse(byte[] content, Path filePath) {
        String response = null;
        try {
            String  mimeType = Files.probeContentType(filePath);
            long length = Files.size(filePath);
            response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";
        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[j + i] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static Request badRequest() throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
        return null;
    }


    public String getMethod() {
        return method;
    }

    public InputStream getBody() {
        return body;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getPost() {
        return post;
    }

    public Map<String, List<String>> getQuery() {
        return query;
    }


    public Path getFilePath() {
        final var filePath = Path.of(".", "public", path);
        return filePath;
    }
}
