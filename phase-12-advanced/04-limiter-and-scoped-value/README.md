# 04 Limiter and scoped value

**Do:** implement `ConcurrencyLimiter` (a `Semaphore`) and the two methods of `RequestContext` (a `ScopedValue`).

**Run:** `mvn -q test -Dtest='C04*'`

**Think about:**
- The permit must be released in a `finally`. What does a leaked permit do after a day of exceptions? Compare with the credit gate in Phase 8.
- `ThreadLocal` is mutable, is inherited by child threads only if you ask, and must be cleaned up by hand. How does a `ScopedValue` differ, and why does that suit millions of virtual threads?
- A request id that is lost when work moves to another thread (a pool, a `CompletableFuture` callback) is a classic observability bug. Which of your Phase 7 stages would lose it, and what could you do about it?
