package org.example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Small executable checks with no external test dependency. */
public class ProjectChecks {
    public static void main(String[] args) throws Exception {
        checkTravelConstraints();
        checkConcurrentSaleAndIds();
        checkCoalitionOptimum();
        System.out.println("All project checks passed.");
    }

    private static void checkTravelConstraints() {
        Service flight = new Service("S", "flight", "Air France", "Lyon", "Paris",
                "2026-12-01", "2026-12-01", 100, "2026-11-30");
        BuyerAgent buyer = new BuyerAgent("B", "Buyer", 150, 100, 0, 0, "2026-11-25");
        buyer.setConstraint("destination", "Paris");
        buyer.setConstraint("departureDate", "2026-12-01");
        buyer.addPreferredCompany("Air France");
        require(buyer.acceptsService(flight), "Matching service should be accepted");
        require(buyer.evaluateOffer(new Offer(150, flight, 0, "S")), "Offer at budget should be accepted");
        require(!buyer.evaluateOffer(new Offer(151, flight, 0, "S")), "Over-budget offer accepted");
        buyer.setConstraint("destination", "London");
        require(!buyer.acceptsService(flight), "Wrong destination accepted");
        buyer.setConstraint("destination", "Paris");
        buyer.addRefusedCompany("Air France");
        require(!buyer.acceptsService(flight), "Refused company accepted");
    }

    private static void checkConcurrentSaleAndIds() throws Exception {
        Service service = new Service("S", "flight", "Air France", "Lyon", "Paris",
                "2026-12-01", "2026-12-01", 100, "2026-11-30");
        SupplierAgent supplier = new SupplierAgent("S", "Supplier", service, 0, 0);
        ExecutorService pool = Executors.newFixedThreadPool(12);
        try {
            List<Callable<Boolean>> sales = new ArrayList<>();
            for (int i = 0; i < 100; i++) sales.add(supplier::sell);
            int successes = 0;
            for (Future<Boolean> result : pool.invokeAll(sales)) if (result.get()) successes++;
            require(successes == 1, "Ticket sold more than once");

            List<Callable<String>> ids = new ArrayList<>();
            for (int i = 0; i < 100; i++)
                ids.add(() -> new Offer(100, service, 0, "B").getOfferId());
            Set<String> unique = new HashSet<>();
            for (Future<String> result : pool.invokeAll(ids)) unique.add(result.get());
            require(unique.size() == 100, "Duplicate offer IDs");
        } finally {
            pool.shutdown();
        }
    }

    private static void checkCoalitionOptimum() {
        List<BuyerAgent> agents = List.of(
                new BuyerAgent("A", "A", 0, 0, 0, 0, ""),
                new BuyerAgent("B", "B", 0, 0, 0, 0, ""),
                new BuyerAgent("C", "C", 0, 0, 0, 0, ""),
                new BuyerAgent("D", "D", 0, 0, 0, 0, ""));
        CoalitionValueTable table = new CoalitionValueTable(agents, 42);
        double optimum = bestPartition((1 << agents.size()) - 1, table);
        double idp = total(new IDPAlgorithm(table).formCoalitions(agents));
        double interval = total(new IntervalDPAlgorithm(table).formCoalitions(agents));
        require(Math.abs(optimum - idp) < 0.001, "Subset DP missed the optimum");
        require(interval <= optimum + 0.001, "Interval DP exceeded the optimum");
    }

    private static double bestPartition(int mask, CoalitionValueTable table) {
        if (mask == 0) return 0;
        double best = Double.NEGATIVE_INFINITY;
        int firstBit = Integer.lowestOneBit(mask);
        for (int sub = mask; sub > 0; sub = (sub - 1) & mask)
            if ((sub & firstBit) != 0)
                best = Math.max(best, table.get(sub) + bestPartition(mask ^ sub, table));
        return best;
    }

    private static double total(List<Coalition> coalitions) {
        return coalitions.stream().mapToDouble(Coalition::getValue).sum();
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
