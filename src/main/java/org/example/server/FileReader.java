package org.example.server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;

import org.example.annotations.*;

public class FileReader implements Runnable {
    private static String staticFolder = "src/main/resources";
    private final Socket clientSocket;
    private final Map<String, Method> routeMappings;
    private final Map<String, Object> controllers;

    public FileReader(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.routeMappings = AppConfig.getRouteMappings();
        this.controllers = AppConfig.getControllers();
    }

    @Override
    public void run() {
        try (
            OutputStream out = clientSocket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String inputLine;
            boolean isFirstLine = true;
            String filePath = "";
            String method = "";

            while ((inputLine = in.readLine()) != null) {
                if (isFirstLine) {
                    String[] requestParts = inputLine.split(" ");
                    method = requestParts[0];
                    filePath = requestParts[1];
                    isFirstLine = false;
                }
                if (!in.ready()) break;
            }

            if (method.equals("GET") && filePath.startsWith("/app")) {
                handleGetRequest(filePath, out);
            } else {
                serveFile(filePath.substring(1), out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket del cliente.");
            }
        }
    }

    private void handleGetRequest(String path, OutputStream out) throws Exception {
        String route = path.split("\\?")[0].replaceAll("/$", "");
        Method handlerMethod = routeMappings.get(route);

        if (handlerMethod != null) {
            Object controller = controllers.get(handlerMethod.getDeclaringClass().getName());

            Map<String, String> queryParams = new HashMap<>();
            if (path.contains("?")) {
                String queryString = path.split("\\?")[1];
                for (String param : queryString.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }

            Parameter[] parameters = handlerMethod.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                RequestParam requestParam = param.getAnnotation(RequestParam.class);

                if (requestParam != null) {
                    String paramName = requestParam.value();
                    String paramValue = queryParams.get(paramName);

                    if (param.getType().equals(int.class) && paramValue != null) {
                        args[i] = Integer.parseInt(paramValue);
                    } else {
                        args[i] = paramValue;
                    }
                }
            }

            String result = (String) handlerMethod.invoke(controller, args);

            PrintWriter writer = new PrintWriter(out, true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/plain");
            writer.println("Content-Length: " + result.length());
            writer.println();
            writer.println(result);
        } else {
            serveFile("400badrequest.html", out);
        }
    }

    private static void serveFile(String filePath, OutputStream output) throws IOException {
        Path file = Paths.get(staticFolder, filePath);
        boolean isError = false;

        if (!Files.exists(file) || Files.isDirectory(file)) {
            file = Paths.get(staticFolder, "400badrequest.html");
            isError = true;
        }

        byte[] fileBytes = Files.readAllBytes(file);
        String contentType = getContentType(filePath);

        PrintWriter writer = new PrintWriter(output, true);
        writer.println(isError ? "HTTP/1.1 400 Bad Request" : "HTTP/1.1 200 OK");
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + fileBytes.length);
        writer.println();
        output.write(fileBytes);
        output.flush();
    }

    private static String getContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".png")) return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".gif")) return "image/gif";
        if (filePath.endsWith(".css")) return "text/css";
        if (filePath.endsWith(".js")) return "application/javascript";
        return "application/octet-stream";
    }

    public static void staticfiles(String path) {
        staticFolder = path;
    }
}
