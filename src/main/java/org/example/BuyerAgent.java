package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;

public class BuyerAgent extends Agent {
    private final double maxBudget;
    private final double startOfferValue;
    private final double minGrowthRate;
    private final double maxGrowthRate;
    private final List<String> preferredCompanies;
    private final List<String> refusedCompanies;
    private final String purchaseDeadline;

    private double currentOffer;
    private final Random random = new Random();

    public BuyerAgent(String agentId, String name,
                      double maxBudget, double startOfferValue,
                      double minGrowthRate, double maxGrowthRate,
                      String purchaseDeadline) {
        super(agentId, name);
        this.maxBudget = maxBudget;
        this.startOfferValue = startOfferValue;
        this.minGrowthRate = minGrowthRate;
        this.maxGrowthRate = maxGrowthRate;
        this.purchaseDeadline = purchaseDeadline;
        this.preferredCompanies = new ArrayList<>();
        this.refusedCompanies = new ArrayList<>();
        this.currentOffer = startOfferValue;

        constraints.put("maxBudget", maxBudget);
        constraints.put("purchaseDeadline", purchaseDeadline);
    }

    public void addPreferredCompany(String company) { preferredCompanies.add(company); }
    public void addRefusedCompany(String company) { refusedCompanies.add(company); }

    // Increases the current offer by a random rate in [minGrowthRate, maxGrowthRate]
    @Override
    public Offer generateOffer(Offer counterOffer, int round) {
        double rate = minGrowthRate + random.nextDouble() * (maxGrowthRate - minGrowthRate);
        currentOffer = currentOffer * (1 + rate);
        if (currentOffer > maxBudget) currentOffer = maxBudget;
        return new Offer(currentOffer, counterOffer != null ? counterOffer.getService() : null, round, agentId);
    }

    @Override
    public boolean evaluateOffer(Offer offer) {
        return offer.getAmount() <= maxBudget && acceptsService(offer.getService());
    }

    public boolean acceptsService(Service service) {
        if (service == null) return false;
        Object destination = getConstraint("destination");
        if (destination != null && !destination.equals(service.getArrival())) return false;
        Object departureDate = getConstraint("departureDate");
        if (departureDate != null && !departureDate.equals(service.getDepartureDate())) return false;
        if (refusedCompanies.contains(service.getCompany())) return false;
        if (!preferredCompanies.isEmpty() && !preferredCompanies.contains(service.getCompany())) return false;
        if (!purchaseDeadline.isBlank() &&
                LocalDate.parse(purchaseDeadline).isAfter(LocalDate.parse(service.getDepartureDate()))) return false;
        return true;
    }

    public double getMaxBudget() { return maxBudget; }
    public double getStartOfferValue() { return startOfferValue; }
    public double getMinGrowthRate() { return minGrowthRate; }
    public double getMaxGrowthRate() { return maxGrowthRate; }
    public String getPurchaseDeadline() { return purchaseDeadline; }
    public List<String> getPreferredCompanies() { return preferredCompanies; }
    public List<String> getRefusedCompanies() { return refusedCompanies; }
}
