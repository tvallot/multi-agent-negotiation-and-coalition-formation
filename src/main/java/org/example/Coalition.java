package org.example;

import java.util.List;

public class Coalition {
    private final List<BuyerAgent> members;
    private final double value;

    public Coalition(List<BuyerAgent> members, double value) {
        this.members = members;
        this.value = value;
    }

    public List<BuyerAgent> getMembers() { return members; }
    public double getValue() { return value; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Coalition{ members=[");
        for (int i = 0; i < members.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(members.get(i).getName());
        }
        sb.append(String.format("], value=%.2f }", value));
        return sb.toString();
    }
}
