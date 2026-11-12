package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Agent {
    protected final String agentId;
    protected final String name;
    protected Map<String, Object> preferences;
    protected Map<String, Object> constraints;
    protected final int maxSubmissions = 6;
    protected List<Message> inbox;

    public Agent(String agentId, String name) {
        this.agentId = agentId;
        this.name = name;
        this.preferences = new HashMap<>();
        this.constraints = new HashMap<>();
        this.inbox = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Generates an offer in response to a counteroffer (null in the first round).
     */
    public abstract Offer generateOffer(Offer counterOffer, int round);

    /**
     * Checks whether a received offer is acceptable.
     */
    public abstract boolean evaluateOffer(Offer offer);

    public void sendMessage(Message message, Agent receiver) {
        System.out.println("  " + message);
        receiver.receiveMessage(message);
    }

    public void receiveMessage(Message message) {
        inbox.add(message);
    }

    public String getAgentId() { return agentId; }
    public String getName() { return name; }
    public int getMaxSubmissions() { return maxSubmissions; }
    public List<Message> getInbox() { return inbox; }

    public void setPreference(String key, Object value) { preferences.put(key, value); }
    public void setConstraint(String key, Object value) { constraints.put(key, value); }
    public Object getPreference(String key) { return preferences.get(key); }
    public Object getConstraint(String key) { return constraints.get(key); }
}
