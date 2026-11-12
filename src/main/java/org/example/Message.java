package org.example;

import java.util.concurrent.atomic.AtomicInteger;

public class Message {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private final String messageId;
    private final String senderId;
    private final String receiverId;
    private final MessageType type;
    private final Offer offer;

    public Message(String senderId, String receiverId, MessageType type, Offer offer) {
        this.messageId = "MSG-" + COUNTER.incrementAndGet();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.offer = offer;
    }

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public MessageType getType() { return type; }
    public Offer getOffer() { return offer; }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s : %s | %s",
                messageId, senderId, receiverId, type,
                offer != null ? offer.toString() : "no offer");
    }
}
