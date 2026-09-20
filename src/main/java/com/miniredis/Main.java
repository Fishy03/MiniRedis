package com.miniredis;

import com.miniredis.store.InMemoryStore;

public class Main {

    public static void main(String[] args) {

        InMemoryStore store = new InMemoryStore();

        // SET
        store.set("name", "Navneet");

        // GET
        System.out.println("Name: " + store.get("name"));

        // EXISTS
        System.out.println("Exists: " + store.exists("name"));

        // DELETE
        store.delete("name");

        // Check after DELETE
        System.out.println("After delete: " + store.get("name"));
    }
}