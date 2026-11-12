package org.example;

import java.util.List;
import java.util.Random;

/**
 * Random value table for all possible coalitions (shared information).
 *
 * Every nonempty subset of n agents receives a randomly generated value.
 * Bitmask indexing: bit i is set when agent i belongs to the subset.
 *
 * The value depends on group size plus a random component:
 *   value(mask) in [size * 100, size * 100 + 300]
 *
 * Larger coalitions therefore tend to be worth more,
 * although a smaller group can still outperform a larger one.
 */
public class CoalitionValueTable {

    private final double[] values;
    private final int n;
    private final long seed;

    public CoalitionValueTable(List<BuyerAgent> agents, long seed) {
        this.n = agents.size();
        this.seed = seed;
        this.values = new double[1 << n];

        Random rng = new Random(seed);
        for (int mask = 1; mask < (1 << n); mask++) {
            int size = Integer.bitCount(mask);
            double base = size * 100.0;
            double noise = rng.nextDouble() * 300.0;
            // Round to two decimal places for readability
            values[mask] = Math.round((base + noise) * 100.0) / 100.0;
        }
    }

    public double get(int mask) {
        return values[mask];
    }

    public int getN() {
        return n;
    }

    public long getSeed() {
        return seed;
    }

    /** Prints the value of a subset with its member names. */
    public void printEntry(int mask, List<BuyerAgent> agents) {
        System.out.printf("    %-45s -> %7.2f%n", formatMask(mask, agents), values[mask]);
    }

    /** Prints every value (useful when debugging small agent groups). */
    public void printAll(List<BuyerAgent> agents) {
        System.out.println("  Generated random values (seed=" + seed + "):");
        for (int mask = 1; mask < (1 << n); mask++) {
            printEntry(mask, agents);
        }
    }

    public static String formatMask(int mask, List<BuyerAgent> agents) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < agents.size(); i++) {
            if ((mask & (1 << i)) != 0) {
                if (!first) sb.append(", ");
                sb.append(agents.get(i).getName());
                first = false;
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
