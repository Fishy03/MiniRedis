
package com.miniredis.benchmark;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class LoadTest {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;

    private static final int VALUE_SIZE = 1024; // 1 KB
    private static final int[] BATCHES = {
        1000, 5000, 10000, 25000
    };

    public static void main(String[] args) {

        String value = "X".repeat(VALUE_SIZE);

        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader input = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );
            PrintWriter output = new PrintWriter(
                socket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
            )
        ) {

            System.out.println(input.readLine());

            int total = 0;

            for (int batch : BATCHES) {

                System.out.println(
                    "\nInserting next " + batch + " records..."
                );

                for (int i = 0; i < batch; i++) {

                    String key = "benchmark_" + total;

                    output.println("SET " + key + " " + value);

                    String response = input.readLine();

                    if (!"OK".equals(response)) {
                        throw new RuntimeException(
                            "SET failed for " + key + ": " + response
                        );
                    }

                    total++;
                }

                System.out.println(
                    "Successfully inserted: " + total
                );

                System.out.println(
                    "Approximate value payload: " +
                    (total * VALUE_SIZE / 1024) + " KB"
                );

                System.out.println(
                    "Check MiniRedis memory usage now."
                );

                Thread.sleep(3000);
            }

            System.out.println("\nLoad test completed!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
