
package com.miniredis.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DiskBackedStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void supportsBasicOperations() throws Exception {
        Path file = tempDir.resolve("data.dat");

        try (DiskBackedStore store = new DiskBackedStore(file)) {
            store.set("a", "one");

            assertEquals("one", store.get("a"));
            assertTrue(store.exists("a"));

            store.delete("a");

            assertFalse(store.exists("a"));
            assertNull(store.get("a"));
        }
    }

    @Test
    void updatesExistingKeys() throws Exception {
        Path file = tempDir.resolve("data.dat");

        try (DiskBackedStore store = new DiskBackedStore(file)) {
            store.set("name", "Yash");
            store.set("name", "MiniRedis");

            assertEquals("MiniRedis", store.get("name"));
        }

        try (DiskBackedStore reopened = new DiskBackedStore(file)) {
            assertEquals("MiniRedis", reopened.get("name"));
        }
    }

    @Test
    void preservesDataAfterReopening() throws Exception {
        Path file = tempDir.resolve("data.dat");

        try (DiskBackedStore first = new DiskBackedStore(file)) {
            first.set("name", "Yash");
            first.set("course", "BCA");
        }

        try (DiskBackedStore second = new DiskBackedStore(file)) {
            assertEquals("Yash", second.get("name"));
            assertEquals("BCA", second.get("course"));
        }
    }

    @Test
    void reportsTtlStates() throws Exception {
        Path file = tempDir.resolve("data.dat");

        try (DiskBackedStore store = new DiskBackedStore(file)) {
            assertEquals(-2, store.ttl("missing"));

            store.set("permanent", "value");
            assertEquals(-1, store.ttl("permanent"));

            store.set("temporary", "value", 30);
            assertTrue(store.ttl("temporary") > 0);
        }
    }

    @Test
    void expiresKeys() throws Exception {
        Path file = tempDir.resolve("data.dat");

        try (DiskBackedStore store = new DiskBackedStore(file)) {
            store.set("temporary", "value", 1);

            Thread.sleep(1200);

            assertNull(store.get("temporary"));
            assertEquals(-2, store.ttl("temporary"));
        }
    }

    @Test
    void persistsExpirationAcrossRestart() throws Exception {
        Path file = tempDir.resolve("data.dat");

        try (DiskBackedStore first = new DiskBackedStore(file)) {
            first.set("temporary", "value", 30);
        }

        try (DiskBackedStore second = new DiskBackedStore(file)) {
            assertEquals("value", second.get("temporary"));
            assertTrue(second.ttl("temporary") > 0);
        }
    }
}
