package com.miniredis;

import java.util.Scanner;

import com.miniredis.command.Command;
import com.miniredis.command.CommandExecutor;
import com.miniredis.command.CommandParser;
import com.miniredis.store.InMemoryStore;

public class Main {

    public static void main(String[] args) {

        InMemoryStore store = new InMemoryStore();
        CommandParser parser = new CommandParser();
        CommandExecutor executor = new CommandExecutor(store);

        Scanner scanner = new Scanner(System.in);

        System.out.println("MiniRedis started!");
        System.out.println("Type commands like: SET name Navneet");
        System.out.println("Type EXIT to stop.");

        while (true) {

            System.out.print("MiniRedis> ");

            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("EXIT")) {
                System.out.println("MiniRedis stopped.");
                break;
            }

            try {
                Command command = parser.parse(input);
                String response = executor.execute(command);

                System.out.println(response);

            } catch (IllegalArgumentException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        scanner.close();
    }
}