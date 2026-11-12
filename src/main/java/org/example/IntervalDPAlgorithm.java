package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval DP algorithm: sequential dynamic programming over agent intervals.
 *
 * Processes agents in a fixed order and forms only coalitions of consecutive
 * agents. This reduces the search space but can miss the global optimum.
 */
public class IntervalDPAlgorithm implements CoalitionFormationAlgorithm {

    private final CoalitionValueTable table;

    public IntervalDPAlgorithm(CoalitionValueTable table) {
        this.table = table;
    }

    @Override
    public List<Coalition> formCoalitions(List<BuyerAgent> buyers) {
        int n = buyers.size();

        double[] dp = new double[n + 1];
        // bestK[i] = size of the last coalition in the best partition of the first i agents
        int[] bestK = new int[n + 1];

        dp[0] = 0;
        for (int i = 1; i <= n; i++) {
            dp[i] = Double.NEGATIVE_INFINITY;
            for (int k = 1; k <= i; k++) {
                // Coalition = agents[i-k .. i-1] (k consecutive agents ending at i-1)
                int mask = maskFor(i - k, i);
                double val = table.get(mask) + dp[i - k];
                if (val > dp[i]) {
                    dp[i] = val;
                    bestK[i] = k;
                }
            }
        }

        // Reconstruct backwards from dp[n]
        List<Coalition> result = new ArrayList<>();
        int pos = n;
        while (pos > 0) {
            int k = bestK[pos];
            int mask = maskFor(pos - k, pos);
            List<BuyerAgent> members = new ArrayList<>(buyers.subList(pos - k, pos));
            result.add(0, new Coalition(members, table.get(mask)));
            pos -= k;
        }

        return result;
    }

    /** Builds a bitmask for agents with indices [from, to). */
    private int maskFor(int from, int to) {
        int mask = 0;
        for (int j = from; j < to; j++) mask |= (1 << j);
        return mask;
    }
}
