package org.example;

import java.util.concurrent.atomic.AtomicInteger;

public class Offer {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final String offerId;
    private final double amount;
    private final Service service;
    private final int submissionNumber;
    private final String senderId;

    public Offer(double amount, Service service, int submissionNumber, String senderId) {
        this.offerId = "OFF-" + COUNTER.incrementAndGet();
        this.amount = amount;
        this.service = service;
        this.submissionNumber = submissionNumber;
        this.senderId = senderId;
    }

    public String getOfferId() { return offerId; }
    public double getAmount() { return amount; }
    public Service getService() { return service; }
    public int getSubmissionNumber() { return submissionNumber; }
    public String getSenderId() { return senderId; }

    @Override
    public String toString() {
        return String.format("Offer %s from %s: EUR %.2f (round %d)",
                offerId, senderId, amount, submissionNumber);
    }
}
