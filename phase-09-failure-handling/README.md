# Phase 9: Failure handling

Things fail all the time: a call times out, an event is malformed, a downstream service goes down. This phase builds the four standard tools for living with that: retries with backoff, a dead letter queue, a circuit breaker, and idempotency so retries are safe.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-retry`: exponential backoff with a cap and jitter | `mvn -q test -Dtest='C01*'` |
| 2 | `02-dead-letter`: one bad event must not stop the stream | `mvn -q test -Dtest='C02*'` |
| 3 | `03-circuit-breaker`: stop hammering a service that is down | `mvn -q test -Dtest='C03*'` |
| 4 | `04-idempotency`: make retries safe with a bounded de-duplication filter | `mvn -q test -Dtest='C04*'` |

`Event` and `EventType` are given. So are the small supporting types (`Sleeper`, `Clock`, the exceptions, `RetryPolicy`, `DeadLetterQueue`). `Sleeper` and `Clock` are interfaces so the tests control time; use them, never `Thread.sleep` or `System.nanoTime` directly. Your code is in `src/main/java/dev/conc/p9/`; replace each `TODO`.

**Read first (once):** Nygard, *Release It!* (Timeouts, Retries, Circuit Breaker, Bulkheads); the AWS Architecture Blog post *Exponential Backoff And Jitter* (Marc Brooker); the Google SRE book chapter *Handling Overload* and *Addressing Cascading Failures*; Martin Fowler, *CircuitBreaker*. Check the references.

**Evidence:** challenge 1 is the experiment. Simulate 200 clients that all fail at the same time against one recovering server: retry with no jitter and with full jitter, and plot the number of requests per 100 ms window for each. Write what you expect (the retry storm) and what you see in `../results/<date>-retry-jitter.md`.
