# Technical Report — Multi-Agent Negotiation and Coalition Formation

**Stack:** Java 17, Maven

## Project overview

This educational project models a travel-ticket market with buyer and supplier agents. Buyers bid for tickets within a budget; suppliers respond with lower asking prices. It also explores how groups of buyers can be partitioned into coalitions to maximize a shared value function. The two demonstrations are independent: negotiation outcomes do not feed the coalition algorithms.

## Concurrent bilateral negotiation

`BuyerAgent` starts at a configured price and raises its offer by a random rate each round, capped at its maximum budget. It accepts a supplier counteroffer when the price fits its budget and the company is not refused. `SupplierAgent` starts at 1.5 times its minimum price and lowers its counteroffer by a random rate, never falling below that minimum.

`NegotiationSession` runs up to six rounds for one buyer and one supplier. The buyer proposes first. If the supplier accepts, the session returns the buyer offer. Otherwise, the supplier counters; if the buyer accepts and the sale succeeds, the session returns the counteroffer. A session returns `null` if it cannot reach an agreement.

Three sessions run concurrently through `ExecutorService.invokeAll`: Alice and Charlie target the same Paris ticket, while Frank targets a London ticket. Charlie's budget is below the Paris supplier's minimum, so that session cannot succeed. The exact offer sequence varies because concessions and thread scheduling are random.

The supplier keeps its current asking price in a `ThreadLocal`, giving each negotiation thread a separate price trajectory. `evaluateOffer()` and `sell()` are synchronized around the sale state, preventing two sessions from selling the same ticket. The `AtomicBoolean` also makes the sold flag visible to other threads.

The negotiation checks destination, preferred and refused companies, and that the buyer's purchase deadline does not fall after the ticket's departure date. It does not implement a live booking clock or check supplier sale expiration. Messages are logged and placed in inboxes, but the negotiation calls agents directly instead of processing inbox queues.

## Coalition formation

Given `n` buyers, a coalition is a nonempty subset of buyers. A coalition structure is a partition in which every buyer appears exactly once. The objective is to maximize the sum of coalition values.

`CoalitionValueTable` assigns a reproducible value to each nonempty subset using a seed. For a subset `S`, it generates approximately `100 × |S| + U(0, 300)`, rounded to two decimal places. All algorithms read the same table. These values are synthetic; they are not derived from negotiation outcomes or buyer preferences. `CoalitionValue` contains a separate profile-based value function, but the current algorithms do not call it.

### Random grouping (`CouplingAlgorithm`)

The algorithm shuffles buyers, then splits the resulting order into groups of random sizes. It looks up each group value after forming it. Group construction takes `O(n)` time. It provides a baseline with no optimality guarantee.

### Subset dynamic programming (`IDPAlgorithm`)

For each bitmask `S`, the algorithm evaluates every nonempty subset `T`:

```text
dp[empty] = 0
dp[S] = max over nonempty T subset of S: value[T] + dp[S without T]
```

It remembers the best first subset to reconstruct the optimal partition. This implementation takes `O(3^n)` time and `O(2^n)` auxiliary space, in addition to the value table. It is optimal for the supplied table. Despite its class name, this is the standard full subset dynamic program; it does not implement the specific pruning techniques associated with published IDP variants.

### Sequential interval dynamic programming (`IntervalDPAlgorithm`)

This algorithm considers only coalitions made of consecutive agents in the input order. For each prefix, it chooses the best final contiguous group. It takes `O(n^3)` time as implemented, because it rebuilds a bitmask for each candidate interval, and `O(n)` auxiliary space. It can miss a better partition containing noncontiguous groups. The class name describes its actual search space.

For the same value table, IDP's result is at least as valuable as either other result because it searches every partition. The random baseline can outperform the interval algorithm on a particular run; there is no fixed ordering between those two.

## Running the demonstration

Install JDK 17 or newer and Maven, then run:

```sh
mvn compile
mvn exec:java -Dexec.mainClass=org.example.Main
```

The program prints negotiation messages and outcomes, all 15 nonempty coalition values for four buyers, and a comparison of all three algorithms. The value table and random grouping use seeds `42` and `123` respectively. Negotiation prices remain nondeterministic.

The dependency-free `ProjectChecks` class checks travel constraints, concurrent single-sale behavior, unique offer IDs, and subset-DP optimality on a small case. Run it with `mvn test-compile exec:java -Dexec.mainClass=org.example.ProjectChecks -Dexec.classpathScope=test`.

## Suggested improvements

1. Add a live booking clock and reject tickets past their supplier sale deadline.
2. Use a domain-based value model if the goal is to demonstrate benefits of cooperation in travel purchasing rather than compare algorithms on synthetic scores.
