package org.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Subset dynamic programming for coalition structure generation.
 *
 * Considers every split of each subset and returns the highest-value partition.
 *
 * Recurrence:
 *   dp[mask] = max over nonempty subsets T of mask:
 *              value(T) + dp[mask without T]
 *
 * Time complexity: O(3^n). Auxiliary space: O(2^n).
 *
 * Coalition values come from CoalitionValueTable (random values
 * generated in advance and shared among agents).
 */
public class IDPAlgorithm implements CoalitionFormationAlgorithm {

    private final CoalitionValueTable table;

    public IDPAlgorithm(CoalitionValueTable table) {
        this.table = table;
    }

    @Override
    public List<Coalition> formCoalitions(List<BuyerAgent> buyers) {
        int n = buyers.size();
        int total = 1 << n;

        double[] dp = new double[total];
        // bestFirst[mask] = best first subgroup to extract from mask
        int[] bestFirst = new int[total];

        for (int mask = 1; mask < total; mask++) {
            dp[mask] = Double.NEGATIVE_INFINITY;
            // Enumerates every nonempty subset of mask
            for (int sub = mask; sub > 0; sub = (sub - 1) & mask) {
                double remainderVal = (sub == mask) ? 0 : dp[mask ^ sub];
                double val = table.get(sub) + remainderVal;
                if (val > dp[mask]) {
                    dp[mask] = val;
                    bestFirst[mask] = sub;
                }
            }
        }

        // Reconstructs the optimal partition using bestFirst
        List<Coalition> result = new ArrayList<>();
        int remaining = total - 1; // all agents
        while (remaining > 0) {
            int coalMask = bestFirst[remaining];
            List<BuyerAgent> members = membersOf(coalMask, buyers, n);
            result.add(new Coalition(members, table.get(coalMask)));
            remaining ^= coalMask;
        }

        return result;
    }

    private List<BuyerAgent> membersOf(int mask, List<BuyerAgent> buyers, int n) {
        List<BuyerAgent> members = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((mask & (1 << i)) != 0) members.add(buyers.get(i));
        }
        return members;
    }
}
