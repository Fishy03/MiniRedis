package com.miniredis.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/** Thread-safe in-memory key-value store with TTL and snapshot persistence. */
public class InMemoryStore {
    private final Map<String, String> data = new HashMap<>();
    // Expiration time in epoch milliseconds; absent means no expiration.
    private final Map<String, Long> expiresAt = new HashMap<>();

    public synchronized void set(String key, String value) {
        requireKey(key);
        data.put(key, value);
        expiresAt.remove(key);
    }

    public synchronized void set(String key, String value, long ttlSeconds) {
        requireKey(key);
        if (ttlSeconds <= 0) {
            delete(key);
            return;
        }
        data.put(key, value);
        expiresAt.put(key, System.currentTimeMillis() + ttlSeconds * 1000L);
    }

    public synchronized String get(String key) {
        purgeIfExpired(key);
        return data.get(key);
    }

    public synchronized void delete(String key) {
        data.remove(key);
        expiresAt.remove(key);
    }

    public synchronized boolean exists(String key) {
        purgeIfExpired(key);
        return data.containsKey(key);
    }

    /** Returns true if a live key's expiration was set. Non-positive TTL deletes the key. */
    public synchronized boolean expire(String key, long ttlSeconds) {
        purgeIfExpired(key);
        if (!data.containsKey(key)) return false;
        if (ttlSeconds <= 0) {
            delete(key);
        } else {
            expiresAt.put(key, System.currentTimeMillis() + ttlSeconds * 1000L);
        }
        return true;
    }

    /** Redis-like TTL result: -2 missing, -1 no expiration, otherwise seconds remaining. */
    public synchronized long ttl(String key) {
        purgeIfExpired(key);
        if (!data.containsKey(key)) return -2;
        Long deadline = expiresAt.get(key);
        if (deadline == null) return -1;
        return Math.max(0, (deadline - System.currentTimeMillis() + 999L) / 1000L);
    }

    public synchronized void purgeExpired() {
        long now = System.currentTimeMillis();
        expiresAt.entrySet().removeIf(e -> {
            if (e.getValue() <= now) {
                data.remove(e.getKey());
                return true;
            }
            return false;
        });
    }

    private void purgeIfExpired(String key) {
        Long deadline = expiresAt.get(key);
        if (deadline != null && deadline <= System.currentTimeMillis()) delete(key);
    }

    public synchronized void save(Path file) throws IOException {
        purgeExpired();
        Properties properties = new Properties();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String encodedKey = encoder.encodeToString(entry.getKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String encodedValue = encoder.encodeToString(entry.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            properties.setProperty("value." + encodedKey, encodedValue);
            Long deadline = expiresAt.get(entry.getKey());
            if (deadline != null) properties.setProperty("expiry." + encodedKey, deadline.toString());
        }
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, "miniredis-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                properties.store(out, "MiniRedis snapshot; generated file");
            }
            try {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveUnsupported) {
                Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public synchronized void load(Path file) throws IOException {
        if (!Files.exists(file)) return;
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file)) { properties.load(in); }
        Map<String, String> loadedData = new HashMap<>();
        Map<String, Long> loadedExpiry = new HashMap<>();
        Base64.Decoder decoder = Base64.getUrlDecoder();
        long now = System.currentTimeMillis();
        for (String name : properties.stringPropertyNames()) {
            if (!name.startsWith("value.")) continue;
            String encodedKey = name.substring("value.".length());
            String key = new String(decoder.decode(encodedKey), java.nio.charset.StandardCharsets.UTF_8);
            String value = new String(decoder.decode(properties.getProperty(name)), java.nio.charset.StandardCharsets.UTF_8);
            String expiryText = properties.getProperty("expiry." + encodedKey);
            if (expiryText != null) {
                long deadline;
                try { deadline = Long.parseLong(expiryText); }
                catch (NumberFormatException e) { continue; }
                if (deadline <= now) continue;
                loadedExpiry.put(key, deadline);
            }
            loadedData.put(key, value);
        }
        data.clear(); data.putAll(loadedData);
        expiresAt.clear(); expiresAt.putAll(loadedExpiry);
    }

    private static void requireKey(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key cannot be empty");
    }
}
