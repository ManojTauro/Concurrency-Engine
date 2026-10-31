# 04 Limiter and scoped value

## The problem
Two small tools that real services need.
1. A **concurrency limiter**: at most N calls inside a section at once (for example N database connections), however many threads want in. The classic bug is a permit that is never given back when the action throws.
2. A **request id** that every log line deep in the call chain can read without passing it as a parameter. `ThreadLocal` does this but is mutable, leaks easily and does not suit millions of virtual threads. A `ScopedValue` is immutable and bound for exactly the duration of a block of code.

## What you implement
In `src/main/java/dev/conc/p12/`:
1. `ConcurrencyLimiter(int permits)` (use a `Semaphore`):
   - `<T> T call(Callable<T> action) throws Exception`
   - `<T> Optional<T> tryCall(Callable<T> action, Duration timeout) throws Exception`
   - `int inUse()`
2. `RequestContext` (the `ScopedValue<String> REQUEST_ID` is given):
   - `static <T> T withRequestId(String id, Callable<T> body) throws Exception`
   - `static Optional<String> currentRequestId()`

## Rules
**Limiter**
- At most `permits` calls run inside the limiter at the same time.
- `call` waits for a permit, runs the action, and **always** gives the permit back, even if the action throws (the exception propagates unchanged).
- `tryCall` waits at most `timeout` for a permit and returns `Optional.empty()` if none became free; otherwise it runs the action like `call` and returns its result (an action that returns `null` gives `Optional.empty()` too).
- `inUse()` is the number of permits currently taken. `permits < 1` throws `IllegalArgumentException`.

**Request context**
- `withRequestId(id, body)` runs `body` with `REQUEST_ID` bound to `id` and returns its result. An exception from `body` propagates unchanged. The binding disappears when `body` is done.
- `currentRequestId()` is the bound id, or `Optional.empty()` outside any `withRequestId`.
- Bindings **nest**: an inner `withRequestId` shows its id inside, and the outer id is back after it.
- The binding is **not** visible in a new `Thread` started inside `body`.

## Example
```java
var limiter = new ConcurrencyLimiter(2);
limiter.call(() -> "x");                 // "x", inUse() is 0 again afterwards
limiter.call(() -> { throw new IOException(); });   // IOException propagates, permit returned
// two long calls running; a third thread:
limiter.tryCall(() -> "y", Duration.ofMillis(50))   // Optional.empty after ~50 ms
// 50 threads each call(...) and track the number currently inside: the maximum is 2

RequestContext.currentRequestId();                                  // Optional.empty
RequestContext.withRequestId("req-1", () -> {
    var a = RequestContext.currentRequestId();                      // Optional[req-1]
    RequestContext.withRequestId("req-2", () -> RequestContext.currentRequestId());  // Optional[req-2]
    var b = RequestContext.currentRequestId();                      // Optional[req-1] again
    new Thread(() -> RequestContext.currentRequestId()).start();    // that thread sees Optional.empty
    return null;
});
RequestContext.currentRequestId();                                  // Optional.empty again
```

## Run
`mvn -q test -Dtest='C04*'`

## Done when
All `C04*` tests pass.

## Think about
- The permit must be released in a `finally`. What does a leaked permit do after a day of exceptions? Compare with the credit gate in Phase 8.
- `ThreadLocal` is mutable, is inherited by child threads only if you ask, and must be cleaned up by hand. How does a `ScopedValue` differ, and why does that suit millions of virtual threads?
- A request id that is lost when work moves to another thread (a pool, a `CompletableFuture` callback) is a classic observability bug. Which of your Phase 7 stages would lose it, and what could you do about it?
