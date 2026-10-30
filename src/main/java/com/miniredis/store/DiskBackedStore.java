
package com.miniredis.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DiskBackedStore implements Store, AutoCloseable {

    private static final long NO_EXPIRY = 0L;

    private final RandomAccessFile file;
    private final Map<String, Long> index = new HashMap<>();

    public DiskBackedStore(Path path) throws IOException {
        Path absolute = path.toAbsolutePath();
        if (absolute.getParent() != null) {
            java.nio.file.Files.createDirectories(absolute.getParent());
        }

        file = new RandomAccessFile(absolute.toFile(), "rw");
        rebuildIndex();
    }

    private void rebuildIndex() throws IOException {
        index.clear();
        long position = 0;

        while (position < file.length()) {
            file.seek(position);

            int keyLength = file.readInt();
            if (keyLength < 0 || keyLength > file.length() - file.getFilePointer()) {
                throw new IOException("Invalid key record in database");
            }

            byte[] keyBytes = new byte[keyLength];
            file.readFully(keyBytes);
            String key = new String(keyBytes, StandardCharsets.UTF_8);

            file.readLong(); // Expiration timestamp
            int valueLength = file.readInt();

            if (valueLength < -1 ||
                    (valueLength >= 0 &&
                    valueLength > file.length() - file.getFilePointer())) {
                throw new IOException("Invalid value record in database");
            }

            if (valueLength >= 0) {
                index.put(key, position);
                file.seek(file.getFilePointer() + valueLength);
            } else {
                index.remove(key); // Tombstone: deleted key
            }

            position = file.getFilePointer();
        }
    }

    private void append(String key, String value, long expiry)
            throws IOException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] valueBytes = value == null
                ? null
                : value.getBytes(StandardCharsets.UTF_8);

        long position = file.length();
        file.seek(position);

        file.writeInt(keyBytes.length);
        file.write(keyBytes);
        file.writeLong(expiry);
        file.writeInt(valueBytes == null ? -1 : valueBytes.length);

        if (valueBytes != null) {
            file.write(valueBytes);
        }

        file.getFD().sync();

        if (valueBytes == null) {
            index.remove(key);
        } else {
            index.put(key, position);
        }
    }

    private String readValue(String key, long position)
            throws IOException {
        file.seek(position);

        int keyLength = file.readInt();
        file.seek(file.getFilePointer() + keyLength);

        long expiry = file.readLong();
        int valueLength = file.readInt();

        if (valueLength < 0) {
            return null;
        }

        if (expiry != NO_EXPIRY && expiry <= System.currentTimeMillis()) {
            append(key, null, NO_EXPIRY);
            return null;
        }

        byte[] valueBytes = new byte[valueLength];
        file.readFully(valueBytes);

        return new String(valueBytes, StandardCharsets.UTF_8);
    }

    private void requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be empty");
        }
    }

    private long expiryFor(String key) {
        Long position = index.get(key);
        if (position == null) {
            return NO_EXPIRY;
        }

        try {
            file.seek(position);
            int keyLength = file.readInt();
            file.seek(file.getFilePointer() + keyLength);
            return file.readLong();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read record", e);
        }
    }

    @Override
    public synchronized void set(String key, String value) {
        requireKey(key);
        try {
            append(key, value, NO_EXPIRY);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save record", e);
        }
    }

    @Override
    public synchronized void set(String key, String value, long ttlSeconds) {
        requireKey(key);

        if (ttlSeconds <= 0) {
            delete(key);
            return;
        }

        long expiry = System.currentTimeMillis() + ttlSeconds * 1000L;

        try {
            append(key, value, expiry);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save record", e);
        }
    }

    @Override
    public synchronized String get(String key) {
        Long position = index.get(key);
        if (position == null) {
            return null;
        }

        try {
            return readValue(key, position);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read record", e);
        }
    }

    @Override
    public synchronized void delete(String key) {
        requireKey(key);

        if (!index.containsKey(key)) {
            return;
        }

        try {
            append(key, null, NO_EXPIRY);
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete record", e);
        }
    }

    @Override
    public synchronized boolean exists(String key) {
        return get(key) != null;
    }

    @Override
    public synchronized boolean expire(String key, long ttlSeconds) {
        String value = get(key);
        if (value == null) {
            return false;
        }

        if (ttlSeconds <= 0) {
            delete(key);
            return true;
        }

        set(key, value, ttlSeconds);
        return true;
    }

    @Override
    public synchronized long ttl(String key) {
        String value = get(key);
        if (value == null) {
            return -2;
        }

        long expiry = expiryFor(key);
        if (expiry == NO_EXPIRY) {
            return -1;
        }

        return Math.max(
                0,
                (expiry - System.currentTimeMillis() + 999L) / 1000L
        );
    }

    @Override
    public synchronized void close() throws IOException {
        file.close();
    }
}
