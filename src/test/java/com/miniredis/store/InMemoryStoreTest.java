package com.miniredis.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryStoreTest {
    @TempDir Path tempDir;
    @Test void supportsBasicOperations() {
        InMemoryStore s = new InMemoryStore(); s.set("a", "one");
        assertEquals("one", s.get("a")); assertTrue(s.exists("a"));
        s.delete("a"); assertFalse(s.exists("a")); assertNull(s.get("a"));
    }
    @Test void reportsTtlStates() {
        InMemoryStore s = new InMemoryStore();
        assertEquals(-2, s.ttl("missing")); s.set("permanent", "v"); assertEquals(-1, s.ttl("permanent"));
        s.set("short", "v", 10); assertTrue(s.ttl("short") >= 1);
    }
    @Test void expiresKeys() throws InterruptedException {
        InMemoryStore s = new InMemoryStore(); s.set("short", "v", 1);
        Thread.sleep(1150); assertNull(s.get("short")); assertEquals(-2, s.ttl("short"));
    }
    @Test void savesAndLoadsValuesAndExpiry() throws Exception {
        Path file = tempDir.resolve("snapshot.properties");
        InMemoryStore first = new InMemoryStore(); first.set("hello world", "value with spaces"); first.set("temporary", "v", 30); first.save(file);
        InMemoryStore second = new InMemoryStore(); second.load(file);
        assertEquals("value with spaces", second.get("hello world")); assertEquals("v", second.get("temporary")); assertTrue(second.ttl("temporary") > 0);
    }
}
