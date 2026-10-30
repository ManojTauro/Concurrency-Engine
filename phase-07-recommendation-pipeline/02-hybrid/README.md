# 02 Hybrid

**Do:** implement `HybridRecommender`. Start both sources before waiting for either, then combine.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:**
- If you write `personalised.recommend(u).thenCompose(p -> trending.top(n)...)` the calls run one after the other. How do you see that in the timing test?
- `handle` gets both the value and the exception. Why is it a better building block here than `exceptionally`?
- A source that throws *synchronously* never returns a future. Where is that failure caught?
