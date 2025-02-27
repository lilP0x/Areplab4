package org.example.server;

import java.net.*;
import java.util.concurrent.*;
import java.io.*;

import org.example.annotations.AppConfig;

public class HttpServer {

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ServerSocket serverSocket;
    private static volatile boolean running = true;
    private static final int TIMEOUT_SECONDS = 30; 
    private static ScheduledFuture<?> shutdownTask;

    public static void main(String[] args) {
        AppConfig.initialize();

        try {
            serverSocket = new ServerSocket(35000);
            System.out.println("Servidor HTTP iniciado en el puerto 35000.");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdownServer()));

            resetShutdownTimer();

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    resetShutdownTimer(); 
                    threadPool.execute(new FileReader(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error al aceptar conexión: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("No pude escuchar en el puerto: 35000.");
            System.exit(1);
        }
    }

    private static void resetShutdownTimer() {
        if (shutdownTask != null && !shutdownTask.isCancelled()) {
            shutdownTask.cancel(false);
        }

        shutdownTask = scheduler.schedule(() -> {
            System.out.println("No se recibieron peticiones en " + TIMEOUT_SECONDS + " segundos. Apagando servidor...");
            shutdownServer();
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private static void shutdownServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            scheduler.shutdown();
            System.out.println("Servidor apagado correctamente.");
        } catch (IOException e) {
            System.err.println("Error cerrando el servidor: " + e.getMessage());
        }
    }
}
