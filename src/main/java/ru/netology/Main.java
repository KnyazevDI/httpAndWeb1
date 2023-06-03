package ru.netology;

import java.io.*;


public class Main {
    public static void main(String[] args){
        int threadPoolSize = 64;
        int port = 9999;

        Server server = new Server(threadPoolSize);

        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                System.out.println(" get messages");
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                System.out.println(" Post messages");
            }
        });

        server.listen(port);

    }
}


