# Phase 7: Recommendation pipeline

A recommendation is several remote calls composed: fetch what the user touched, find similar products, score them, rank. Done with blocking calls it ties up a thread per request while it waits. Done with futures it does not. This phase is about composing asynchronous steps without blocking, and about which thread runs each step.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-recommender-chain`: `thenCompose`, fan-out with `allOf`, no `join` | `mvn -q test -Dtest='C01*'` |
| 2 | `02-hybrid`: two independent sources in parallel with `thenCombine` | `mvn -q test -Dtest='C02*'` |
| 3 | `03-timeout-fallback`: timeouts, failures and a fallback that always answers | `mvn -q test -Dtest='C03*'` |
| 4 | `04-callback-threads`: which thread runs your callback, and why it matters | `mvn -q test -Dtest='C04*'` |

`Event` and `EventType` are given. The fake remote services (`SimClients`) and the non-blocking `Simulator` are given complete. Your code is in `src/main/java/dev/conc/p7/`; replace each `TODO`.

**Read first (once):** JCIP ch. 6.3 (`Future`) and the `CompletableFuture` javadoc, read the whole class description including "Asynchronous execution" and the table of `thenApply` / `thenCompose` / `thenCombine` / `handle` / `exceptionally`; the `CompletionStage` javadoc. Check the references.

**Evidence:** the experiment is challenge 4. In `../results/<date>-callback-threads.md` write what you expect the heavy step to do to a 30 ms timer when it runs with `thenApply` and with `thenApplyAsync(..., cpuPool)`, then measure both (temporarily write the wrong version) and put the lateness numbers in the note.
