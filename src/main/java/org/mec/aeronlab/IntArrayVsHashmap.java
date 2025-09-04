package org.mec.aeronlab;
import java.util.HashMap;
import java.util.Random;

/**
 * This class benchmarks the performance of an integer array versus a HashMap<Integer, Integer>
 * for fast lookups. The scenario assumes a known, dense range of integer keys.
 *
 * The goal is to demonstrate the performance impact of autoboxing and hashing overhead
 * in a low-latency, performance-critical application.
 */
public class IntArrayVsHashmap {

    // Define the size of our data structures and the number of operations to perform.
    private static final int DATA_SIZE = 1_000_000;
    private static final int NUM_OPERATIONS = 50_000_000;

    public static void main(String[] args) {
        // Run the benchmarks
        benchmarkArrayAccess();
        benchmarkHashMapAccess();
    }

    /**
     * Benchmarks access time for a primitive integer array.
     * This method directly uses the integer keys as array indices.
     */
    private static void benchmarkArrayAccess() {
        System.out.println("--- Benchmarking Array Access ---");
        // Create a large integer array. We are assuming the keys are within the range [0, DATA_SIZE).
        int[] intArray = new int[DATA_SIZE];
        Random random = new Random();

        // Populate the array with some values.
        for (int i = 0; i < DATA_SIZE; i++) {
            intArray[i] = random.nextInt(100);
        }

        // Warm up the JVM to ensure JIT compiler optimizations are in effect.
        for (int i = 0; i < 1_000_000; i++) {
            int key = random.nextInt(DATA_SIZE);
            int value = intArray[key];
        }

        long startTime = System.nanoTime();
        int sum = 0; // Use sum to prevent JIT from optimizing away the loop completely.
        for (int i = 0; i < NUM_OPERATIONS; i++) {
            int key = random.nextInt(DATA_SIZE);
            sum += intArray[key];
        }
        long endTime = System.nanoTime();

        // Calculate and print the results.
        double durationMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Total operations: %d\n", NUM_OPERATIONS);
        System.out.printf("Total time: %.2f ms\n", durationMs);
        System.out.printf("Average time per operation: %.2f ns\n", (durationMs * 1_000_000) / NUM_OPERATIONS);
        System.out.println("Result (to prevent dead code elimination): " + sum);
        System.out.println();
    }

    /**
     * Benchmarks access time for a HashMap with Integer keys.
     * This method involves hashing and autoboxing for each access.
     */
    private static void benchmarkHashMapAccess() {
        System.out.println("--- Benchmarking HashMap Access ---");
        // Create a HashMap with Integer keys.
        HashMap<Integer, Integer> intHashMap = new HashMap<>(DATA_SIZE);
        Random random = new Random();

        // Populate the HashMap. This involves autoboxing and hashing on insertion.
        for (int i = 0; i < DATA_SIZE; i++) {
            intHashMap.put(i, random.nextInt(100));
        }

        // Warm up the JVM.
        for (int i = 0; i < 1_000_000; i++) {
            Integer key = random.nextInt(DATA_SIZE);
            Integer value = intHashMap.get(key);
        }

        long startTime = System.nanoTime();
        int sum = 0; // Use sum to prevent JIT from optimizing away the loop completely.
        for (int i = 0; i < NUM_OPERATIONS; i++) {
            Integer key = random.nextInt(DATA_SIZE);
            sum += intHashMap.get(key); // Autoboxing of 'key' and 'sum', and hashing
        }
        long endTime = System.nanoTime();

        // Calculate and print the results.
        double durationMs = (endTime - startTime) / 1_000_000.0;
        System.out.printf("Total operations: %d\n", NUM_OPERATIONS);
        System.out.printf("Total time: %.2f ms\n", durationMs);
        System.out.printf("Average time per operation: %.2f ns\n", (durationMs * 1_000_000) / NUM_OPERATIONS);
        System.out.println("Result (to prevent dead code elimination): " + sum);
        System.out.println();
    }
}