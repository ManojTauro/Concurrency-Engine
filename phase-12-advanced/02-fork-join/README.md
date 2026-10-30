# 02 Fork/join

**Do:** implement `ParallelScorer.scoreByUser` with a `RecursiveTask`.

**Run:** `mvn -q test -Dtest='C02*'`. The test checks that your work ran in the pool it was given.

**Think about:**
- Why fork one half and compute the other half yourself, instead of forking both?
- What does the threshold control? Try 1, 100, 10,000 and 1,000,000 on 5 million events and see the time. Too small: task overhead. Too big: idle cores.
- Fork/join suits CPU-bound, divide-and-conquer work that does not block. What goes wrong if a task blocks on I/O inside a `ForkJoinPool`? (And why do parallel streams share the common pool with everyone else?)
- Work stealing: which end of the deque does the owner use, and which end does a thief use? Why?
