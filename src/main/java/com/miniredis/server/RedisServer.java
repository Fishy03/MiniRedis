package com.miniredis.server;

import com.miniredis.store.InMemoryStore;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class RedisServer {
    private static final int PORT = 6379;
    private static final Path DATA_FILE = Path.of("miniredis-data.properties");

    public static void main(String[] args) {
        InMemoryStore store = new InMemoryStore();
        try {
            store.load(DATA_FILE);
            System.out.println("Loaded persisted data from " + DATA_FILE.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Could not load persisted data: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                store.save(DATA_FILE);
                System.out.println("MiniRedis data saved to " + DATA_FILE.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("Could not save MiniRedis data: " + e.getMessage());
            }
        }, "miniredis-persistence-shutdown"));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("MiniRedis Server started...");
            System.out.println("Listening on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new ClientConnection(clientSocket, store).start();
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}
