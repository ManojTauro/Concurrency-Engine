# 03 Timeout and fallback

## The problem
A slow or broken service must not make the caller wait. You wrap a primary recommender with a **timeout** and a **fallback** so the caller always gets an answer, within a bounded time, and never an error.

## What you implement
`ResilientRecommender(Recommending primary, Recommending fallback, Duration timeout)` with `CompletableFuture<List<String>> recommend(String userId)`.

## Rules
- Ask the primary. If it answers within the timeout, that is the result and the **fallback is never called**.
- If the primary fails, or does not answer in time, ask the fallback and use its answer.
- If the fallback also fails (or throws when called), the result is an **empty list**.
- The returned future **never completes exceptionally**.
- A primary that answers **late** (after the fallback was used) is ignored.
- A primary that **throws when called** (instead of returning a failed future) counts as a failure.
- A primary that never answers must not hang the caller.
- `recommend` returns at once, without blocking. A failing primary falls back immediately, it does not wait for the timeout.

## Example
`timeout = 100 ms`:
```
primary answers [A] in 20 ms                -> [A], fallback not called
primary answers [A] in 500 ms               -> fallback's answer after ~100 ms
primary never completes                     -> fallback's answer after ~100 ms
primary fails after 10 ms                   -> fallback's answer after ~10 ms
primary throws when called                  -> fallback's answer immediately
primary fails and fallback fails            -> []   (empty list, not an error)
```

## Run
`mvn -q test -Dtest='C03*'`

## Done when
All `C03*` tests pass.

## Think about
- `orTimeout` completes the *same* future exceptionally. If that future is shared with other callers, what do they see? What does `copy()` change?
- When the timeout fires, the slow primary call is still running. Did you cancel it? Can you? What does that mean for a service that is already overloaded?
- A timeout of 100 ms on a call whose p99 is 300 ms: what share of requests now use the fallback? How would you choose the number?
