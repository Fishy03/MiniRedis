package com.miniredis;
import java.io.*;
import java.net.*;

public class ClientConnection extends Thread {

    private Socket socket;

    public ClientConnection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter output = new PrintWriter(
                    socket.getOutputStream(), true);

            output.println("+OK MiniRedis connected");

            CommandHandler handler = new CommandHandler();

            String command;

            while ((command = input.readLine()) != null) {

                System.out.println("Received: " + command);

                String response = handler.execute(command);

                output.println(response);
            }

        } catch (IOException e) {

            System.out.println("Client disconnected.");

        } finally {

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}