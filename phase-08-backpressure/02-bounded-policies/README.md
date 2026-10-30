# 02 Bounded policies

**Do:** implement `BoundedIngest` for the four policies. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:**
- `BLOCK` pushes the slowness back to the producer. Who is the producer in a real system, and what can it do if it is blocked (an HTTP handler thread, a Kafka consumer)?
- `REJECT_NEW` and `TIMEOUT` hand the problem to the caller. What should the caller do with a rejected event?
- `DROP_OLDEST` favours fresh data. Which events is it right for (a live dashboard) and wrong for (a payment)?
- The capacity must hold under contention. How do you make "drop oldest, then add" safe when many threads do it at once?
