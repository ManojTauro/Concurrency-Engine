# 03 Circuit breaker

## The problem
If a service is down, every call to it waits and fails, and your own threads pile up. A **circuit breaker** notices repeated failures and **stops calling** for a while (fail fast), then carefully tests whether the service is back. It is a small state machine with three states: `CLOSED` (normal), `OPEN` (refusing), `HALF_OPEN` (trying one call).

## What you implement
`CircuitBreaker(int failureThreshold, Duration openDuration, Clock clock)` in `src/main/java/dev/conc/p9/`:
- `<T> T call(Callable<T> action) throws Exception`
- `State state()`

`Clock` (given) has `nanos()` so tests move time by hand; use it, never `System.nanoTime`. `CircuitOpenException` is given.

## Rules
- **CLOSED:** calls run. Any `Exception` from the action counts as a failure and is rethrown. A success resets the count of **consecutive** failures to 0. When consecutive failures reach `failureThreshold`, the breaker becomes `OPEN` and remembers the time (`clock.nanos()`).
- **OPEN:** `call` throws `CircuitOpenException` **without running the action**, until `openDuration` has passed. Then the next call is the single trial call (`HALF_OPEN`).
- **HALF_OPEN:** exactly one trial call is in flight; every other call that arrives meanwhile throws `CircuitOpenException`. The trial's success makes the breaker `CLOSED` (count 0). Its failure makes it `OPEN` again with a **new** open time, and the exception is rethrown.
- `state()` reports `CLOSED`, `OPEN` or `HALF_OPEN`. An `OPEN` breaker whose `openDuration` has passed reports `HALF_OPEN`, because the next call will be allowed through.
- The action must run **outside any lock**; the class is safe to call from many threads.
- `failureThreshold < 1` throws `IllegalArgumentException`.

## Example
Threshold 3, open for 10 s, a controllable clock:
```
fail, fail           -> CLOSED          (2 consecutive)
success              -> CLOSED          (count reset to 0)
fail, fail           -> CLOSED          (2 + success + 2 are not 3 in a row)
fail                 -> OPEN            (3 in a row), time t0 remembered
call(...)            -> CircuitOpenException, action NOT run
clock advances 10 s  -> state() == HALF_OPEN
call(() -> "trial")  -> "trial", state() == CLOSED, failure count 0
-- or --
trial call fails     -> exception rethrown, state() == OPEN, the 10 s restart from now
```
While one trial is running (blocked), a second thread calling gets `CircuitOpenException` and the action is called only once.

## Run
`mvn -q test -Dtest='C03*'`

## Done when
All `C03*` tests pass.

## Think about
- The action must run outside the lock. What goes wrong if you hold a lock while calling a slow service?
- Why does half-open allow exactly one trial call and not "a few"? What happens to a service that is just recovering if every client sends full traffic the moment the pause ends?
- Consecutive failures versus a failure *rate* over a window: which one reacts faster, which one is more stable? (Resilience4j uses a sliding window; read how.)
- The breaker protects the caller from a slow dependency, but a slow dependency means calls that *hang*, not fail. What else do you need (timeouts, Phase 7; bulkheads, Phase 8)?
