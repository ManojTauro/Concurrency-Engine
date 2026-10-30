# 05 Striped locks with `ReentrantLock`

**Do:** implement `LockedProfileStore(int stripes)`: spread users over `stripes` maps, each with its own `ReentrantLock`. Always unlock in `finally`.

**Run:** `mvn -q test -Dtest='C05*'`. It is tested with 1, 16 and 256 stripes.

**Measure (write `../results/<date>-store-tradeoffs.md`):**
```
mvn -q compile
java -cp target/classes dev.conc.p5.ContentionMain
```
`ContentionMain` is a deliberately naive timing harness. Run it a few times and note how much the numbers move. Then write down your belief: which store is fastest, and why? You will redo this with JMH in Phase 11 and compare. Do not post these numbers as a conclusion.

**Think about:**
- What is the cost of a stripe? What if two hot users land in the same stripe? (The generator's skew makes that real.)
- Why is `totalEvents()` a `LongAdder` here and not a sum over the stripes under every lock?
- When would a plain `synchronized` store be the better choice?
