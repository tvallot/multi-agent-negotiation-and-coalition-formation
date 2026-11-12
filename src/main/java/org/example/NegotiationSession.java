package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class NegotiationSession implements Callable<Offer> {
    private final BuyerAgent buyer;
    private final SupplierAgent supplier;
    private final List<Offer> offerHistory;

    public NegotiationSession(BuyerAgent buyer, SupplierAgent supplier) {
        this.buyer = buyer;
        this.supplier = supplier;
        this.offerHistory = new ArrayList<>();
    }

    @Override
    public Offer call() {
        return runNegotiation();
    }

    public Offer runNegotiation() {
        String tag = "[" + buyer.getName() + "] ";

        if (!buyer.acceptsService(supplier.getService())) {
            System.out.println(tag + "Service does not match travel constraints; negotiation cancelled.");
            return null;
        }

        if (supplier.isSold()) {
            System.out.println(tag + "Ticket already sold; negotiation cancelled.");
            return null;
        }

        System.out.println(tag + "Starting negotiation with " + supplier.getName());
        System.out.println(tag + "Service: " + supplier.getService());
        System.out.println(tag + "Maximum budget: " + buyer.getMaxBudget() + " EUR | Supplier minimum price: " + supplier.getMinPrice() + " EUR\n");

        Offer lastCounterOffer = null;

        for (int round = 0; round < buyer.getMaxSubmissions(); round++) {
            if (supplier.isSold()) {
                System.out.println(tag + "Ticket sold to another buyer; stopping at round " + round + ".");
                return null;
            }

            Offer buyerOffer = buyer.generateOffer(lastCounterOffer, round);
            offerHistory.add(buyerOffer);

            Message propose = new Message(buyer.getAgentId(), supplier.getAgentId(), MessageType.PROPOSE, buyerOffer);
            buyer.sendMessage(propose, supplier);

            // evaluateOffer atomically marks the service sold if the price is acceptable
            if (supplier.evaluateOffer(buyerOffer)) {
                Message accept = new Message(supplier.getAgentId(), buyer.getAgentId(), MessageType.ACCEPT, buyerOffer);
                supplier.sendMessage(accept, buyer);
                System.out.println(tag + "Agreement on buyer offer at round " + round + ": " + String.format("%.2f EUR", buyerOffer.getAmount()));
                return buyerOffer;
            }

            if (supplier.isSold()) {
                System.out.println(tag + "Ticket sold during supplier response; stopping.");
                return null;
            }

            Offer counterOffer = supplier.generateOffer(buyerOffer, round);
            offerHistory.add(counterOffer);
            lastCounterOffer = counterOffer;

            Message counter = new Message(supplier.getAgentId(), buyer.getAgentId(), MessageType.COUNTER_OFFER, counterOffer);
            supplier.sendMessage(counter, buyer);

            if (buyer.evaluateOffer(counterOffer)) {
                // sell() atomically confirms the sale if no other buyer has done so
                if (supplier.sell()) {
                    Message accept = new Message(buyer.getAgentId(), supplier.getAgentId(), MessageType.ACCEPT, counterOffer);
                    buyer.sendMessage(accept, supplier);
                    System.out.println(tag + "Agreement on counteroffer at round " + round + ": " + String.format("%.2f EUR", counterOffer.getAmount()));
                    return counterOffer;
                } else {
                    System.out.println(tag + "Ticket sold before confirmation; stopping.");
                    return null;
                }
            }

            Message reject = new Message(buyer.getAgentId(), supplier.getAgentId(), MessageType.REJECT, counterOffer);
            buyer.sendMessage(reject, supplier);
            System.out.println();
        }

        System.out.println(tag + "Negotiation failed after " + buyer.getMaxSubmissions() + " rounds without agreement.");
        return null;
    }

    public String getBuyerName() { return buyer.getName(); }
    public List<Offer> getOfferHistory() { return offerHistory; }
}
