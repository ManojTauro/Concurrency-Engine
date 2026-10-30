# 01 Recommender chain

**Do:** implement `Recommender` as a chain of futures. The five steps are in the Javadoc.

**Run:** `mvn -q test -Dtest='C01*'`. One test checks that `recommend` returns at once; one checks the catalog calls overlap.

**Think about:**
- `thenApply` versus `thenCompose`: when do you need each? What type do you get if you use `thenApply` where you needed `thenCompose`?
- `CompletableFuture.allOf` returns a `CompletableFuture<Void>`. How do you get the results back without blocking? Why is `join()` inside the callback fine here but not outside?
- One of the four catalog calls fails. What does the returned future contain, and what do the other three do?
