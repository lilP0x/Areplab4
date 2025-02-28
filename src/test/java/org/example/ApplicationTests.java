package org.example;

import org.example.annotations.AppConfig;
import org.example.server.FileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ApplicationTests {
    private Socket mockSocket;
    private FileReader fileReader;

    @BeforeEach
    void setUp() throws IOException {
        AppConfig.initialize();
        mockSocket = mock(Socket.class);
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream("GET /app/greeting?name=John HTTP/1.1\n\n".getBytes()));
        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        fileReader = new FileReader(mockSocket);
    }

    @Test
    void testControllersLoaded() {
        Map<String, Object> controllers = AppConfig.getControllers();
        assertFalse(controllers.isEmpty(), "Los controladores no fueron cargados correctamente");
    }

    @Test
    void testRoutesLoaded() {
        Map<String, Method> routes = AppConfig.getRouteMappings();
        assertFalse(routes.isEmpty(), "Las rutas no fueron registradas correctamente");
    }

    @Test
    void testHandleGetRequest() {
        assertDoesNotThrow(() -> fileReader.run(), "El manejo de la petición GET falló");
    }
}
