# 01 Recommender chain

## The problem
A recommendation is several remote calls in a row: get what the user looked at, find similar products for each, score them, take the best. With blocking calls, a thread sits idle for every wait. With `CompletableFuture` you describe the steps and **no thread is blocked**. You build the chain.

## What you implement
`Recommender(ProfileClient profile, CatalogClient catalog, ScoringClient scoring)` with `CompletableFuture<List<String>> recommend(String userId)`. The clients are given interfaces that return futures:
- `profile.recentProducts(userId)` -> list of product ids the user touched
- `catalog.similarTo(productId)` -> list of similar product ids
- `scoring.score(userId, candidates)` -> `Map<String, Double>` product -> score (higher is better)

## Rules
1. `recent = profile.recentProducts(userId)`.
2. For **every** recent product call `catalog.similarTo(product)`; all calls are in flight at the same time (not one after the other).
3. `candidates` = the distinct similar products in order of first appearance, **without** any product that is in `recent`.
4. `scores = scoring.score(userId, candidates)`, called **exactly once**, and only if there are candidates.
5. The answer is the (up to) 5 best candidates, highest score first, ties broken by product id ascending.

- Empty history: completes with an empty list, and scoring is never called. No candidates: same.
- If any call fails, the returned future fails and the failure of that call is its cause.
- `recommend` returns at once. It must not call `join()` or `get()` (a test checks it returns in well under the latency of the calls).

## Example
With the given `SimClients`: `user-1` recently touched `product-1..product-4`, and `product-K` is similar to `K+10`, `K+11`, `K+12`.
```
recent      = [product-1, product-2, product-3, product-4]
similar     = product-1 -> [11,12,13]   product-2 -> [12,13,14]
              product-3 -> [13,14,15]   product-4 -> [14,15,16]
candidates  = [product-11, 12, 13, 14, 15, 16]     (distinct, first-seen order)
scoring is called once with that list
answer      = the 5 of those six with the highest score, best first
```
If each remote call takes 100 ms, `catalog` calls overlap, so the whole thing takes about 100 (profile) + 100 (catalogs together) + 100 (scoring) ms, not 100 + 4x100 + 100.

## Run
`mvn -q test -Dtest='C01*'`. One test checks that `recommend` returns at once; one checks the catalog calls overlap.

## Done when
All `C01*` tests pass.

## Think about
- `thenApply` versus `thenCompose`: when do you need each? What type do you get if you use `thenApply` where you needed `thenCompose`?
- `CompletableFuture.allOf` returns a `CompletableFuture<Void>`. How do you get the results back without blocking? Why is `join()` inside the callback fine here but not outside?
- One of the four catalog calls fails. What does the returned future contain, and what do the other three do?
