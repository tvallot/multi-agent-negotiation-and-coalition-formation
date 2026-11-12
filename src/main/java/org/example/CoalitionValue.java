package org.example;

import java.util.List;

public class CoalitionValue {

    /**
     * Coalition value = collective purchasing power * negotiation leverage * compatibility.
     *
     * Formula: sum(budgets) * (1 + 0.10 * group size) * compatibilityScore
     *
     * - Each additional member adds 10% negotiation leverage.
     * - Compatibility starts at 1.0 and increases with shared preferences.
     */
    public static double compute(List<BuyerAgent> members) {
        if (members.isEmpty()) return 0;

        double totalBudget = members.stream().mapToDouble(BuyerAgent::getMaxBudget).sum();
        double sizeFactor = 1 + 0.10 * members.size();
        double compatScore = compatibilityScore(members);

        return totalBudget * sizeFactor * compatScore;
    }

    private static double compatibilityScore(List<BuyerAgent> members) {
        if (members.size() <= 1) return 1.0;

        double score = 1.0;

        // Bonus if all members share the same destination
        Object firstDest = members.get(0).getConstraint("destination");
        boolean sameDestination = firstDest != null && members.stream()
                .allMatch(b -> firstDest.equals(b.getConstraint("destination")));
        if (sameDestination) score += 0.2;

        // Bonus for each preferred company shared by all members
        long commonPreferred = members.get(0).getPreferredCompanies().stream()
                .filter(c -> members.stream().allMatch(b -> b.getPreferredCompanies().contains(c)))
                .count();
        score += 0.1 * commonPreferred;

        // Bonus for each refused company shared by all members
        long commonRefused = members.get(0).getRefusedCompanies().stream()
                .filter(c -> members.stream().allMatch(b -> b.getRefusedCompanies().contains(c)))
                .count();
        score += 0.05 * commonRefused;

        return score;
    }
}
