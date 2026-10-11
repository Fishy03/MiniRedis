package com.miniredis;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RedisServer {

    private static final int PORT = 6379;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("MiniRedis Server started...");
            System.out.println("Listening on port " + PORT);

            while (true) {

                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected: "
                        + clientSocket.getInetAddress());

                ClientConnection client =
                        new ClientConnection(clientSocket);

                client.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
