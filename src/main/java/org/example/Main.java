package org.example;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);

        // ========================================================================
        //  PART 1 — Buyer/supplier bilateral negotiation
        // ========================================================================
        sep("PART 1 — BILATERAL NEGOTIATION");
        System.out.println();
        System.out.println("  Buyer and supplier exchange offers round by round.");
        System.out.println("  The buyer raises bids while the supplier lowers asking prices.");
        System.out.println("  They can agree when prices meet; a budget below the minimum cannot succeed.");
        System.out.println();
        System.out.println("  Three example cases:");
        System.out.println("  [Case 1] Alice   -> Paris   (budget EUR 380 >= min EUR 300) -> agreement expected");
        System.out.println("  [Case 2] Charlie -> Paris   (budget EUR 150 < min EUR 300) -> failure guaranteed");
        System.out.println("  [Case 3] Frank   -> London (budget EUR 250 >= min EUR 180) -> agreement expected");
        System.out.println();

        Service serviceParis   = new Service("S01", "flight", "Air France", "Lyon", "Paris",
                                             "2026-12-01", "2026-12-01", 300.0, "2026-11-30");
        Service serviceLondon = new Service("S03", "flight", "EasyJet",    "Lyon", "London",
                                             "2026-12-10", "2026-12-10", 180.0, "2026-12-08");

        SupplierAgent suppParis   = new SupplierAgent("SUPP-1", "AirFrance", serviceParis,   0.05, 0.15);
        SupplierAgent suppLondon = new SupplierAgent("SUPP-3", "EasyJet",   serviceLondon, 0.06, 0.18);

        BuyerAgent alice   = new BuyerAgent("BUY-1", "Alice",   380.0, 200.0, 0.05, 0.10, "2026-11-25");
        alice.addPreferredCompany("Air France");
        alice.setConstraint("destination", "Paris");

        BuyerAgent charlie = new BuyerAgent("BUY-3", "Charlie", 150.0, 100.0, 0.03, 0.06, "2026-11-25");
        charlie.addPreferredCompany("Air France");
        charlie.setConstraint("destination", "Paris");

        BuyerAgent frank   = new BuyerAgent("BUY-6", "Frank",   250.0, 120.0, 0.06, 0.15, "2026-12-07");
        frank.addPreferredCompany("EasyJet");
        frank.setConstraint("destination", "London");

        List<NegotiationSession> sessions = List.of(
                new NegotiationSession(alice,   suppParis),
                new NegotiationSession(charlie, suppParis),
                new NegotiationSession(frank,   suppLondon)
        );

        ExecutorService executor = Executors.newFixedThreadPool(sessions.size());
        List<Future<Offer>> futures = executor.invokeAll(sessions);
        executor.shutdown();

        System.out.println("\n-- Part 1 results -------------------------------------------");
        for (int i = 0; i < sessions.size(); i++) {
            Offer r    = futures.get(i).get();
            String buyerName = sessions.get(i).getBuyerName();
            if (r != null) System.out.printf("  %-8s -> AGREEMENT    at EUR %.2f%n", buyerName, r.getAmount());
            else            System.out.printf("  %-8s -> NO AGREEMENT%n", buyerName);
        }

        // ========================================================================
        //  PART 2 — Coalition formation algorithms
        // ========================================================================
        System.out.println();
        sep("PART 2 - COALITION FORMATION");
        System.out.println();
        System.out.println("  Goal: find the partition with the highest TOTAL value.");
        System.out.println();

        BuyerAgent a1 = new BuyerAgent("A", "Alice",   0, 0, 0, 0, "");
        BuyerAgent a2 = new BuyerAgent("B", "Bob",     0, 0, 0, 0, "");
        BuyerAgent a3 = new BuyerAgent("C", "Charlie", 0, 0, 0, 0, "");
        BuyerAgent a4 = new BuyerAgent("D", "Diana",   0, 0, 0, 0, "");
        List<BuyerAgent> agents = List.of(a1, a2, a3, a4);

        long seed = 42L;
        CoalitionValueTable vt = new CoalitionValueTable(agents, seed);

        System.out.println("  All possible coalition values:");
        System.out.println();
        for (int mask = 1; mask < (1 << 4); mask++) {
            int size = Integer.bitCount(mask);
            System.out.printf("    [%d agent%s]  %-22s -> %7.2f%n",
                    size, size > 1 ? "s" : " ",
                    CoalitionValueTable.formatMask(mask, agents),
                    vt.get(mask));
        }

        // -- Algorithm A: random competitive grouping --------------------------------------
        System.out.println();
        sep("ALGORITHM A - Random grouping (no information sharing)");
        System.out.println();
        System.out.println();

        List<Coalition> cRes  = new CouplingAlgorithm(vt, 123L).formCoalitions(agents);
        double          totC  = printPartition(cRes, agents);
        System.out.printf("%n  Total random grouping value : %.2f%n", totC);

        // -- Algorithm B: interval dynamic programming ------------------------
        System.out.println();
        sep("ALGORITHM B - INTERVAL DYNAMIC PROGRAMMING");
        List<Coalition> intervalResult = new IntervalDPAlgorithm(vt).formCoalitions(agents);
        double intervalTotal = printPartition(intervalResult, agents);
        System.out.printf("%n  Total interval DP value: %.2f%n", intervalTotal);

        // -- Algorithm C: optimal subset dynamic programming ------------------
        System.out.println();
        sep("ALGORITHM C - OPTIMAL SUBSET DYNAMIC PROGRAMMING");
        System.out.println();
        System.out.println();

        List<Coalition> idpRes  = new IDPAlgorithm(vt).formCoalitions(agents);
        double          totIDP  = printPartition(idpRes, agents);
        System.out.printf("%n  Total IDP value : %.2f  %n", totIDP);

        // -- Summary -------------------------------------------------------------
        System.out.println();
        sep("COMPARISON");
        System.out.println();
        System.out.printf("  Random grouping: %8.2f%n", totC);
        System.out.printf("  Interval DP:     %8.2f%n", intervalTotal);
        System.out.printf("  Subset DP:       %8.2f%n", totIDP);
        System.out.println();

        if (totIDP - totC > 0.01) {
            System.out.printf("  -> IDP exceeds this random grouping by %.2f.%n",
                              totIDP - totC);
        }
        System.out.println();
        System.out.println("  Conclusion: IDP gives the optimal value for this table.");
        System.out.println();
    }

    // -- helpers ---------------------------------------------------------------

    private static double printPartition(List<Coalition> coalitions, List<BuyerAgent> ref) {
        double total = 0;
        for (Coalition c : coalitions) {
            int mask = maskOf(c.getMembers(), ref);
            System.out.printf("    %-22s -> %7.2f%n",
                    CoalitionValueTable.formatMask(mask, ref), c.getValue());
            total += c.getValue();
        }
        return total;
    }

    private static int maskOf(List<BuyerAgent> members, List<BuyerAgent> ref) {
        int mask = 0;
        for (BuyerAgent b : members) {
            int idx = ref.indexOf(b);
            if (idx >= 0) mask |= (1 << idx);
        }
        return mask;
    }

    private static void sep(String title) {
        String line = "=".repeat(62);
        System.out.println(line);
        System.out.println("  " + title.trim());
        System.out.println(line);
    }
}
