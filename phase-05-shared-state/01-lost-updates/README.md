# 01 Lost updates

**Break it first.** `UnsafeCounter.increment()` is `value++`. That is three steps (read, add, write), and two threads can both read the same value.

**Do:** implement `RaceHarness` in `src/main/java/dev/conc/p5/`:
- `countWithoutSync(threads, incrementsPerThread)`: start the threads, join them, return `expected` against `actual`.
- `recordWithoutSync(threads, events)`: the same with `UnsafeProfileStore`, which is a plain `HashMap` and a plain counter. Use `Thread.start` and `join`; no executors yet.

**Run:** `mvn -q test -Dtest='C01*'`

**Then, for the post (write `../results/<date>-lost-updates.md`):**
- Hypothesis first: what total do you expect from 4 threads times 1,000,000 increments, and by how much do you expect it to miss?
- Run it ten times. Record each total and the spread.
- Try 1, 2, 4, 8 threads. Where does the loss start?
- Look at what the store lost: only `totalEvents`, or also per-user counts? Did a worker throw?

**Think about:** `value++` is one line of Java. What are the bytecode steps? (`javap -c`)
