package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Algorithm (a): fully random competitive grouping.
 *
 * In competitive mode, agents share no information:
 * neither coalition values, preferences, nor budgets.
 *
 * Coalitions are therefore formed entirely at random:
 *   1. Agent order is shuffled.
 *   2. The shuffled sequence is split into randomly sized groups.
 *
 * The value of each resulting coalition is read from CoalitionValueTable.
 *
 * This models agents making local grouping decisions
 * without knowing the actual coalition values.
 */
public class CouplingAlgorithm implements CoalitionFormationAlgorithm {

    private final CoalitionValueTable table;
    private final Random random;

    public CouplingAlgorithm(CoalitionValueTable table) {
        this.table = table;
        this.random = new Random();
    }

    public CouplingAlgorithm(CoalitionValueTable table, long seed) {
        this.table = table;
        this.random = new Random(seed);
    }

    @Override
    public List<Coalition> formCoalitions(List<BuyerAgent> buyers) {
        int n = buyers.size();

        // Step 1: shuffle agent order
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        Collections.shuffle(order, random);

        System.out.print("  Random order: ");
        for (int idx : order) System.out.print(buyers.get(idx).getName() + " ");
        System.out.println();

        // Step 2: split shuffled order into contiguous groups
        List<Coalition> coalitions = new ArrayList<>();
        int pos = 0;
        while (pos < n) {
            int remaining = n - pos;
            // Next group size: random integer from 1 to remaining
            int size = 1 + random.nextInt(remaining);
            List<Integer> groupIndices = order.subList(pos, pos + size);

            List<BuyerAgent> members = new ArrayList<>();
            int mask = 0;
            for (int idx : groupIndices) {
                members.add(buyers.get(idx));
                mask |= (1 << idx);
            }

            double value = table.get(mask);
            coalitions.add(new Coalition(members, value));
            pos += size;
        }

        return coalitions;
    }
}
