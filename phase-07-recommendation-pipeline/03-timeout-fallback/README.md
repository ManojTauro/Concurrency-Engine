# 03 Timeout and fallback

**Do:** implement `ResilientRecommender`. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C03*'`

**Think about:**
- `orTimeout` completes the *same* future exceptionally. If that future is shared with other callers, what do they see? What does `copy()` change?
- When the timeout fires, the slow primary call is still running. Did you cancel it? Can you? What does that mean for a service that is already overloaded?
- A timeout of 100 ms on a call whose p99 is 300 ms: what share of requests now use the fallback? How would you choose the number?
