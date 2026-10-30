# 03 Circuit breaker

**Do:** implement `CircuitBreaker`. The state machine is in the Javadoc. The clock is injected.

**Run:** `mvn -q test -Dtest='C03*'`

**Think about:**
- The action must run outside the lock. What goes wrong if you hold a lock while calling a slow service?
- Why does half-open allow exactly one trial call and not "a few"? What happens to a service that is just recovering if every client sends full traffic the moment the pause ends?
- Consecutive failures versus a failure *rate* over a window: which one reacts faster, which one is more stable? (Resilience4j uses a sliding window; read how.)
- The breaker protects the caller from a slow dependency, but a slow dependency means calls that *hang*, not fail. What else do you need (timeouts, Phase 7; bulkheads, Phase 8)?
