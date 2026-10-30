# 04 Idempotency

**Do:** implement `DedupFilter`, then `AtMostOnceRecorder`.

**Run:** `mvn -q test -Dtest='C04*'`. Eight threads race on the same ids.

**Think about:**
- "Check if seen, then record" as two steps is a classic race. Which single operation makes it atomic? Where in your code is the atomic step?
- A bounded filter forgets old ids. What can go wrong after it forgets, and how do you choose the capacity? What would a real system do instead (a unique key in the database, an idempotency token)?
- The recorder forgets the id when the sink throws. What if the sink threw *after* it had partly done its work? Is "at most once" or "exactly once" even achievable here? Look up why exactly-once delivery is hard.
