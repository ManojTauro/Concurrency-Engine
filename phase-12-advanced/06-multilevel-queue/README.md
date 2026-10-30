# 06 Multilevel queue

**Do:** implement `MultilevelQueue` exactly as the Javadoc defines it. `LevelEntry` is given.

**Run:** `mvn -q test -Dtest='C06*'`

**Then, as an extension (not tested):** replace the plain FIFO queue in your `TimeSlicedExecutor` with a `MultilevelQueue` (guard it with a lock), charge each slice's real elapsed time, and repeat the fairness experiment from challenge 5 with a long split that never finishes: do short splits that arrive later still get through quickly?

**Think about:**
- Splits that have used little CPU time are probably short queries; ones that have used a lot are probably big ones. Why is serving the "young" first good for latency overall (think shortest-job-first)?
- Strict priority would starve the long splits. How does the weighted share between levels prevent that, and what does Trino use for the weights? (Read `MultilevelSplitQueue` for its target shares.)
- Why charge the level the entry was at *before* the charge?
