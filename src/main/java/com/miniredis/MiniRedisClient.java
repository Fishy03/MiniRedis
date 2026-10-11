package com.miniredis;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class MiniRedisClient {
    public static void main(String[] args) {
        try (
            Socket socket = new Socket("localhost", 6379);
            BufferedReader input = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
            );
            PrintWriter output = new PrintWriter(
                socket.getOutputStream(), true
            );
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to MiniRedis Server!");

            String response = input.readLine();
            System.out.println("Server: " + response);

            while (true) {
                System.out.print("MiniRedis> ");
                String command = scanner.nextLine();

                if (command.equalsIgnoreCase("QUIT")) {
                    break;
                }

                output.println(command);

                response = input.readLine();

                if (response == null) {
                    System.out.println("Server disconnected.");
                    break;
                }

                System.out.println("Server: " + response);
            }

        } catch (IOException e) {
            System.out.println("Client Error: " + e.getMessage());
        }
    }
}
