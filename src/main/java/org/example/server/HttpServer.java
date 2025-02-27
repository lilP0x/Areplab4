package org.example.server;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.example.annotations.AppConfig;

import java.io.*;

public class HttpServer {

        private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
            public static void main(String[] args) throws Exception {
                ServerSocket serverSocket = null;
                AppConfig.initialize(); 

                try {
                    serverSocket = new ServerSocket(35000);
                    
                } catch (IOException e) {
                    System.err.println("No pude escuchar en el puerto: 35000.");
                    System.exit(1);
                }
        
                boolean running = true;
                while (running) {
                    Socket clientSocket = null;
                    try {
                        System.out.println("Listo para recibir ...");
                        clientSocket = serverSocket.accept();
                        threadPool.execute(new FileReader(clientSocket));

                    } catch (IOException e) {
                        System.err.println("Accept failed.");
                        System.exit(1);
                    } 
        }
        serverSocket.close();
    }
}
