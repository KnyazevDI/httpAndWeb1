package ru.netology;

import java.io.*;


public class Main {
    public static void main(String[] args){
        int threadPoolSize = 64;
        int port = 9999;

        Server server = new Server(threadPoolSize);
        server.listen(port);

    }
}


