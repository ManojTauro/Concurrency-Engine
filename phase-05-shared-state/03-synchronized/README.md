# 03 Fix with `synchronized`

**Do:** implement `SynchronizedProfileStore` with the `synchronized` keyword. Decide first: what is the lock, and which operations must hold it? The reads too, or only the writes?

**Run:** `mvn -q test -Dtest='C03*'`. The tests hammer your store with 8 threads and check every count exactly, and they run readers at the same time.

**Think about (answer in `../results/<date>-store-tradeoffs.md`, shared with 4 and 5):**
- What does `synchronized` on a method actually lock?
- Why must the read methods be synchronized too? What can a reader see otherwise? (Hint: visibility, not only atomicity.)
- Does this store scale with more threads? Why or why not?
