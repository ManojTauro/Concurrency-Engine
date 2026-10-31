# 01 Retry

## The problem
Calls fail for reasons that go away: a timeout, a restarting server. Retrying helps, but retrying **immediately and forever** hammers a service that is struggling. The standard answer is **exponential backoff** (wait 1x, 2x, 4x ... the base delay), a **cap** (never wait longer than a maximum), and **jitter** (randomise the wait so 200 clients do not all retry at the same instant). You write that, with time under test control.

## What you implement
`Retry.call(Callable<T> action, RetryPolicy policy, Sleeper sleeper, Random random)` in `src/main/java/dev/conc/p9/`. `RetryPolicy(maxAttempts, baseDelay, maxDelay, jitter)` with `Jitter.NONE` or `Jitter.FULL`, `Sleeper` and the exceptions are given. Use the `Sleeper`, never `Thread.sleep`.

## Rules
- At most `policy.maxAttempts()` attempts in total (the first included). Below 1 throws `IllegalArgumentException`.
- After failed attempt `k` (`k = 1, 2, ...`) **and only if another attempt follows**, wait `cap_k = min(maxDelay, baseDelay * 2^(k-1))` through the sleeper.
- With `Jitter.FULL` the wait is `Duration.ofNanos(random.nextLong(cap_k.toNanos() + 1))` instead (exactly one `nextLong` call per wait).
- No wait after the last attempt.
- The doubling must **never overflow**, however large `k` gets.
- A `PermanentFailureException` is thrown at once: no wait, no retry.
- An `InterruptedException` (from the action or the sleeper) is thrown at once: no retry.
- When every attempt failed, throw `RetriesExhaustedException(maxAttempts, last exception)`: its `attempts()` is `maxAttempts` and its cause is the **last** exception.
- Success returns the action's result. A success on the first try never sleeps.

## Example
Policy: 7 attempts, base 100 ms, max 1000 ms, `Jitter.NONE`; the action always throws:
```
attempt 1 fails -> sleep 100 ms
attempt 2 fails -> sleep 200 ms
attempt 3 fails -> sleep 400 ms
attempt 4 fails -> sleep 800 ms
attempt 5 fails -> sleep 1000 ms   (1600 capped at 1000)
attempt 6 fails -> sleep 1000 ms
attempt 7 fails -> no sleep, throw RetriesExhaustedException(attempts = 7, cause = failure 7)
```
Fails twice then succeeds: result returned, 3 calls, 2 sleeps. Action throws `PermanentFailureException("bad request")`: thrown at once after 1 call, no sleep. `maxAttempts = 1`: one call, no sleep, `RetriesExhaustedException(1, ...)` on failure.

## Run
`mvn -q test -Dtest='C01*'`. The sleeper in the tests records the waits instead of sleeping.

## Done when
All `C01*` tests pass and `../results/<date>-retry-jitter.md` exists.

## Think about
- Why exponential and not a fixed wait? Why a cap? Why jitter?
- `baseDelay * 2^(k-1)` for large `k` overflows a `long`. What does the number become, and what would your code then sleep for? How do you make the doubling safe?
- Which exceptions should never be retried, and who decides (the callee, the caller)? What happens if you retry a non-idempotent call (challenge 4)?
- A retry multiplies load exactly when a service is struggling. If every layer in a call chain retries 3 times, how many calls reach the bottom service?
- Experiment: simulate 200 clients that all fail at the same time against one recovering server, retry with no jitter and with full jitter, and plot requests per 100 ms window for each.
