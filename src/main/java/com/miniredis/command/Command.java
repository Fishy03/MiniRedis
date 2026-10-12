package com.miniredis.command;

public class Command {
    private final CommandType type;
    private final String key;
    private final String value;
    private final Long ttlSeconds;

    public Command(CommandType type, String key, String value) {
        this(type, key, value, null);
    }

    public Command(CommandType type, String key, String value, Long ttlSeconds) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.ttlSeconds = ttlSeconds;
    }
    public CommandType getType() { return type; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public Long getTtlSeconds() { return ttlSeconds; }
}
