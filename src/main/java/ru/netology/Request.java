package ru.netology;

import javax.management.Query;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Request {
    private String query;
    private String path;
    private String response;
    private Path filePath;
    private String mimeType;
    private long length;
    public Request(String query, String path){
        this.query = query;
        this.path = path;
        filePath = Path.of(".", "public", path);
    }
    public String getResponse() {
        try {
            mimeType = Files.probeContentType(filePath);

            length = Files.size(filePath);

        }catch (IOException e){
            e.printStackTrace();
        }
        response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + mimeType + "\r\n" +
                "Content-Length: " + length + "\r\n" +
                "Connection: close\r\n" +
                "\r\n";
        return response;
    }

    public Object getFilePath() {
        return filePath;
    }
}
