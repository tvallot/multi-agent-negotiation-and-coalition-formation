# Multi-Agent Negotiation & Coalition Formation

**Java 17 · Maven · Multi-agent systems · Concurrency · Dynamic programming**

A university project that uses a travel-ticket market to explore two questions: how can buyers and suppliers reach an agreement when several negotiations happen at once, and how should buyers form groups to maximize their combined value?

## How it works

1. **Negotiation:** buyers gradually increase their bids while suppliers lower their asking prices. Three sessions run concurrently. A supplier can sell a ticket only once, even if two buyers negotiate for it at the same time. Buyers also check their budget, destination, travel date, and company preferences.
2. **Coalition formation:** the program assigns reproducible scores to every possible group of four buyers. It compares random grouping, a faster algorithm restricted to consecutive buyers, and subset dynamic programming that finds the best partition.

This demonstrates thread-safe agent interaction and the trade-off between search cost and solution quality. Coalition scores are synthetic, so the project is an algorithm demonstration rather than a booking application.

## Example result

With the included coalition seeds, the demo prints:

```text
Random grouping: 658.46
Interval DP:     989.08
Subset DP:       989.08
```

The subset algorithm guarantees the best score for the supplied values. Negotiation prices can vary between runs because bids and thread scheduling are random.

## Run

Requires **JDK 17+** and **Maven**.

```sh
mvn compile
mvn exec:java -Dexec.mainClass=org.example.Main
```

To run the executable checks for travel constraints, concurrent sales, unique IDs, and coalition optimality:

```sh
mvn test-compile exec:java -Dexec.mainClass=org.example.ProjectChecks -Dexec.classpathScope=test
```

See [the technical report](RAPPORT.md) for the algorithms, design decisions, and limitations.
