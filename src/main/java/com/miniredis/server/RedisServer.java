
package com.miniredis.server;

import com.miniredis.store.DiskBackedStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class RedisServer {

    private static final int PORT = 6379;
    private static final Path DATA_FILE = Path.of("miniredis-data.dat");

    public static void main(String[] args) {
        final DiskBackedStore store;

        try {
            store = new DiskBackedStore(DATA_FILE);
            System.out.println(
                "Disk-backed data opened from " + DATA_FILE.toAbsolutePath()
            );
        } catch (IOException e) {
            System.err.println(
                "Could not open disk-backed store: " + e.getMessage()
            );
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                store.close();
                System.out.println("MiniRedis disk store closed.");
            } catch (IOException e) {
                System.err.println(
                    "Could not close disk store: " + e.getMessage()
                );
            }
        }, "miniredis-shutdown"));

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("MiniRedis Server started...");
            System.out.println("Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(
                    "Client connected: " + clientSocket.getInetAddress()
                );
                new ClientConnection(clientSocket, store).start();
            }
        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}
