package com.miniredis.server;

import com.miniredis.command.CommandHandler;
import com.miniredis.store.InMemoryStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnection extends Thread {

    private final Socket socket;
    private final InMemoryStore store;

    public ClientConnection(Socket socket, InMemoryStore store) {
        this.socket = socket;
        this.store = store;
    }

    @Override
    public void run() {

        try (
            Socket clientSocket = socket;
            BufferedReader input = new BufferedReader(
                new InputStreamReader(
                    clientSocket.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );
            PrintWriter output = new PrintWriter(
                clientSocket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
            )
        ) {
            output.println("+OK MiniRedis connected");

            CommandHandler handler = new CommandHandler(store);
            String command;

            while ((command = input.readLine()) != null) {

                System.out.println("Received: " + command);

                String response = handler.execute(command);
                output.println(response);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected.");
        }
    }
}