package com.miniredis;
import java.util.HashMap;
import java.util.Map;

public class CommandHandler {

    private final Map<String, String> dataStore = new HashMap<>();

    public String execute(String command) {

        if (command == null || command.trim().isEmpty()) {
            return "-ERR empty command";
        }

        String[] parts = command.trim().split("\\s+");

        String operation = parts[0].toUpperCase();

        switch (operation) {

            case "PING":
                return "+PONG";

            case "SET":
                if (parts.length < 3) {
                    return "-ERR wrong number of arguments for SET";
                }

                dataStore.put(parts[1], parts[2]);
                return "+OK";

            case "GET":
                if (parts.length != 2) {
                    return "-ERR wrong number of arguments for GET";
                }

                String value = dataStore.get(parts[1]);

                if (value == null) {
                    return "$-1";
                }

                return value;

            case "DEL":
                if (parts.length != 2) {
                    return "-ERR wrong number of arguments for DEL";
                }

                if (dataStore.remove(parts[1]) != null) {
                    return ":1";
                }

                return ":0";

            default:
                return "-ERR unknown command";
        }
    }
}