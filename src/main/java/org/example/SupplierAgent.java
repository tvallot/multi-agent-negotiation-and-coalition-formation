package org.example;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SupplierAgent extends Agent {
    private final Service service;
    private final double minConcessionRate;
    private final double maxConcessionRate;

    private final AtomicBoolean sold = new AtomicBoolean(false);
    // Price per thread (each negotiation has its own trajectory)
    private final ThreadLocal<Double> sessionPrice;
    private final Random random = new Random();

    public SupplierAgent(String agentId, String name, Service service,
                         double minConcessionRate, double maxConcessionRate) {
        super(agentId, name);
        this.service = service;
        this.minConcessionRate = minConcessionRate;
        this.maxConcessionRate = maxConcessionRate;
        this.sessionPrice = ThreadLocal.withInitial(() -> service.getMinPrice() * 1.5);

        constraints.put("minPrice", service.getMinPrice());
        constraints.put("saleDateDeadline", service.getSaleDateDeadline());
    }

    @Override
    public Offer generateOffer(Offer buyerOffer, int round) {
        double current = sessionPrice.get();
        double rate = minConcessionRate + random.nextDouble() * (maxConcessionRate - minConcessionRate);
        current = current * (1 - rate);
        if (current < service.getMinPrice()) current = service.getMinPrice();
        sessionPrice.set(current);
        return new Offer(current, service, round, agentId);
    }

    // Atomically checks the price and marks the service as sold
    @Override
    public synchronized boolean evaluateOffer(Offer offer) {
        if (sold.get()) return false;
        if (offer.getAmount() >= service.getMinPrice()) {
            sold.set(true);
            return true;
        }
        return false;
    }

    // Called when the buyer accepts a supplier counteroffer
    public synchronized boolean sell() {
        if (sold.get()) return false;
        sold.set(true);
        return true;
    }

    public boolean isSold() { return sold.get(); }
    public Service getService() { return service; }
    public double getMinPrice() { return service.getMinPrice(); }
    public double getMinConcessionRate() { return minConcessionRate; }
    public double getMaxConcessionRate() { return maxConcessionRate; }
}
