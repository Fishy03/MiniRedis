package com.miniredis.command;

import com.miniredis.store.InMemoryStore;

public class CommandExecutor {

    private final InMemoryStore store;

    public CommandExecutor(InMemoryStore store) {
        this.store = store;
    }

    public String execute(Command command) {

        switch (command.getType()) {

            case SET:
                store.set(command.getKey(), command.getValue());
                return "OK";

            case GET:
                String value = store.get(command.getKey());
                return value != null ? value : "(nil)";

            case DEL:
                boolean existed = store.exists(command.getKey());
                store.delete(command.getKey());
                return existed ? "1" : "0";

            case EXISTS:
                return store.exists(command.getKey()) ? "1" : "0";

            default:
                throw new IllegalArgumentException("Unsupported command");
        }
    }
}