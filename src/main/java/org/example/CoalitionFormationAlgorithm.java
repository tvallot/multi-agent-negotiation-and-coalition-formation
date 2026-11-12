package org.example;

import java.util.List;

public interface CoalitionFormationAlgorithm {
    List<Coalition> formCoalitions(List<BuyerAgent> buyers);
}
