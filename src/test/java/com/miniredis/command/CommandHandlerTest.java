package com.miniredis.command;

import com.miniredis.store.InMemoryStore;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandHandlerTest {
    @Test void supportsTtlCommands() {
        CommandHandler h = new CommandHandler(new InMemoryStore());
        assertEquals("OK", h.execute("SET user Yash"));
        assertEquals("Yash", h.execute("GET user"));
        assertEquals("1", h.execute("EXPIRE user 30"));
        assertTrue(Long.parseLong(h.execute("TTL user")) > 0);
        assertEquals("OK", h.execute("SET temp hello EX 30"));
        assertEquals("hello", h.execute("GET temp"));
    }
    @Test void handlesInvalidAndMissingCommands() {
        CommandHandler h = new CommandHandler(new InMemoryStore());
        assertEquals("(nil)", h.execute("GET absent"));
        assertTrue(h.execute("SET x y EX nope").startsWith("ERR "));
        assertTrue(h.execute("WHAT").startsWith("ERR "));
        assertEquals("ERR empty command", h.execute(" "));
    }
}
