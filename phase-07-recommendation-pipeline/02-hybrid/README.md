# 02 Hybrid

## The problem
Good recommendations mix a personalised list with what is trending now. The two sources do not depend on each other, so they must be asked **at the same time**, then combined. If one is down you still want to answer with the other.

## What you implement
`HybridRecommender(Recommending personalised, TrendingClient trending, int size)` with `CompletableFuture<List<String>> recommend(String userId)`. `TrendingClient.top(k)` returns a future of the `k` trending product ids.

## Rules
- Both calls start immediately and run at the same time. Total time is about the slower one, not the sum.
- Result = the personalised items **in order**, then trending items that are **not already** in the list, cut to `size`.
- Trending is asked for **exactly `size`** items.
- One source fails: the result is the other one alone (cut to `size`). Both fail: the returned future fails.
- A source that **throws when called** (instead of returning a failed future) counts as a failure of that source.
- `recommend` returns at once, without blocking.

## Example
`size = 5`:
```
personalised = [p1, p2]            trending = [t1, t2, t3, t4, t5]
result       = [p1, p2, t1, t2, t3]            (trending asked for 5, only 3 fit)

personalised = [p1, p2, p3, p1x]   trending = [p1, t1, t2, t3, t4]   (size 5)
result       = [p1, p2, p3, p1x, t1]           (trending's p1 is a duplicate, skipped)

trending fails      -> [p1, p2]            personalised fails -> [t1, t2, t3, t4, t5]
both fail           -> returned future fails
```
Timing: personalised takes 150 ms, trending 120 ms: the answer arrives after about 150 ms, not 270 ms.

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass.

## Think about
- If you write `personalised.recommend(u).thenCompose(p -> trending.top(n)...)` the calls run one after the other. How do you see that in the timing test?
- `handle` gets both the value and the exception. Why is it a better building block here than `exceptionally`?
- A source that throws *synchronously* never returns a future. Where is that failure caught?
