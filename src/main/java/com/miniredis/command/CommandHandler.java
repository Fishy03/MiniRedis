package com.miniredis.command;

import com.miniredis.store.InMemoryStore;

public class CommandHandler {

    private final CommandParser parser;
    private final CommandExecutor executor;

    public CommandHandler(InMemoryStore store) {
        this.parser = new CommandParser();
        this.executor = new CommandExecutor(store);
    }

    public String execute(String input) {

        if (input == null || input.trim().isEmpty()) {
            return "ERR empty command";
        }

        if (input.trim().equalsIgnoreCase("PING")) {
            return "PONG";
        }

        try {
            Command command = parser.parse(input);
            return executor.execute(command);
        } catch (IllegalArgumentException e) {
            return "ERR " + e.getMessage();
        }
    }
}