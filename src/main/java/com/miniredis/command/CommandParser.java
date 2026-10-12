package com.miniredis.command;

public class CommandParser {
    public Command parse(String input) {
        if (input == null || input.trim().isEmpty()) throw new IllegalArgumentException("Empty command");
        String[] parts = input.trim().split("\\s+");
        CommandType type;
        try { type = CommandType.valueOf(parts[0].toUpperCase()); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Unknown command: " + parts[0]); }
        switch (type) {
            case SET:
                if (parts.length == 3) return new Command(type, parts[1], parts[2]);
                if (parts.length == 5 && parts[3].equalsIgnoreCase("EX")) {
                    long seconds = parseSeconds(parts[4]);
                    if (seconds <= 0) throw new IllegalArgumentException("SET EX seconds must be positive");
                    return new Command(type, parts[1], parts[2], seconds);
                }
                throw new IllegalArgumentException("Usage: SET key value [EX seconds]");
            case GET: case DEL: case EXISTS: case TTL:
                if (parts.length != 2) throw new IllegalArgumentException(type + " requires a key");
                return new Command(type, parts[1], null);
            case EXPIRE:
                if (parts.length != 3) throw new IllegalArgumentException("Usage: EXPIRE key seconds");
                return new Command(type, parts[1], null, parseSeconds(parts[2]));
            default: throw new IllegalArgumentException("Unsupported command");
        }
    }
    private long parseSeconds(String value) {
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("TTL seconds must be an integer"); }
    }
}
