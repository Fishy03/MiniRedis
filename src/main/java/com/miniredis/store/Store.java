
package com.miniredis.store;

public interface Store {

    void set(String key, String value);

    void set(String key, String value, long ttlSeconds);

    String get(String key);

    void delete(String key);

    boolean exists(String key);

    boolean expire(String key, long ttlSeconds);

    long ttl(String key);
}
