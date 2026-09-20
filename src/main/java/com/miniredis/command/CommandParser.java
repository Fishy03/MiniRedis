package com.miniredis.command;

public class CommandParser {

    public Command parse(String input) {

        String[] parts = input.trim().split("\\s+");

        if (parts.length == 0) {
            throw new IllegalArgumentException("Empty command");
        }

        CommandType type;

        try {
            type = CommandType.valueOf(parts[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown command: " + parts[0]);
        }

        switch (type) {

            case SET:
                if (parts.length != 3) {
                    throw new IllegalArgumentException("SET requires key and value");
                }
                return new Command(type, parts[1], parts[2]);

            case GET:
            case DEL:
            case EXISTS:
                if (parts.length != 2) {
                    throw new IllegalArgumentException(type + " requires a key");
                }
                return new Command(type, parts[1], null);

            default:
                throw new IllegalArgumentException("Unsupported command");
        }
    }
}