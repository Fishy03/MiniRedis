package com.miniredis.command;

import com.miniredis.store.InMemoryStore;

public class CommandExecutor {
    private final InMemoryStore store;
    public CommandExecutor(InMemoryStore store) { this.store = store; }
    public String execute(Command command) {
        switch (command.getType()) {
            case SET:
                if (command.getTtlSeconds() == null) store.set(command.getKey(), command.getValue());
                else store.set(command.getKey(), command.getValue(), command.getTtlSeconds());
                return "OK";
            case GET:
                String value = store.get(command.getKey());
                return value != null ? value : "(nil)";
            case DEL:
                boolean existed = store.exists(command.getKey()); store.delete(command.getKey()); return existed ? "1" : "0";
            case EXISTS: return store.exists(command.getKey()) ? "1" : "0";
            case EXPIRE:
                if (command.getTtlSeconds() <= 0) {
                    boolean found = store.exists(command.getKey()); store.delete(command.getKey()); return found ? "1" : "0";
                }
                return store.expire(command.getKey(), command.getTtlSeconds()) ? "1" : "0";
            case TTL: return Long.toString(store.ttl(command.getKey()));
            default: throw new IllegalArgumentException("Unsupported command");
        }
    }
}
