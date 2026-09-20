package com.miniredis.command;

public class Command {

    private final CommandType type;
    private final String key;
    private final String value;

    public Command(CommandType type, String key, String value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public CommandType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}