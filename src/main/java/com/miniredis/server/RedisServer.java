package com.miniredis.server;

import com.miniredis.store.InMemoryStore;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RedisServer {

    private static final int PORT = 6379;

    public static void main(String[] args) {

        InMemoryStore store = new InMemoryStore();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("MiniRedis Server started...");
            System.out.println("Listening on port " + PORT);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected: "
                        + clientSocket.getInetAddress());

                ClientConnection client =
                        new ClientConnection(clientSocket, store);

                client.start();
            }

        } catch (IOException e) {
            System.out.println("Server Error: " + e.getMessage());
        }
    }
}