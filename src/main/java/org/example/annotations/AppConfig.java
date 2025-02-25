package org.example.annotations;

import org.example.annotations.RestController;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URI;
import java.util.*;

public class AppConfig {
    private static final Map<String, Method> routeMappings = new HashMap<>();
    private static final Map<String, Object> controllers = new HashMap<>();

    public AppConfig() {
    }

    public static void initialize() {
        System.out.println("Inicializando AppConfig...");
        loadComponents("org.example.controller");

    }

    private static void loadComponents(String packagePath) {
        try {
            List<Class<?>> classes = findAllClasses(packagePath);
            for (Class<?> c : classes) {
                if (c.isAnnotationPresent(RestController.class)) {
                    Object instance = c.getDeclaredConstructor().newInstance();
                    controllers.put(c.getName(), instance);

                    for (Method method : c.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(GetMapping.class)) {
                            String route = method.getAnnotation(GetMapping.class).value();
                            routeMappings.put(route, method);
                            System.out.println("Ruta registrada: " + route);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Class<?>> findAllClasses(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.toURI());

            if (directory.exists()) {
                for (String file : directory.list()) {
                    if (file.endsWith(".class")) {
                        String className = packageName + "." + file.substring(0, file.length() - 6);
                        classes.add(Class.forName(className));
                    }
                }
            }
        }
        return classes;
    }

    public static Map<String, Method> getRouteMappings() {
        return routeMappings;
    }

    public static Map<String, Object> getControllers() {
        return controllers;
    }
}
