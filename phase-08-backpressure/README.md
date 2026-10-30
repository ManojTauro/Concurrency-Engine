# Phase 8: Backpressure

What happens when events arrive faster than they can be handled? You first make a queue grow without limit and watch it, then bound it and choose what a full queue does, bound the whole path with credits, and finally decide which work to lose first.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-flood`: break it on purpose with an unbounded queue and a faster producer | `mvn -q test -Dtest='C01*'` |
| 2 | `02-bounded-policies`: block, reject, drop oldest, or time out | `mvn -q test -Dtest='C02*'` |
| 3 | `03-credit-gate`: bound the work in flight end to end with credits | `mvn -q test -Dtest='C03*'` |
| 4 | `04-priority-shedding`: when overloaded, lose the least valuable events first | `mvn -q test -Dtest='C04*'` |

`Event` and `EventType` are given. Your code is in `src/main/java/dev/conc/p8/`; replace each `TODO`.

**Read first (once):** JCIP ch. 8.3 (saturation policies); Little's law (any queueing-theory primer; Jeff Dean's and Gil Tene's talks on tail latency are good follow-ups); the Reactive Streams specification (reactive-streams.org), rule 1.1 and the section on demand; Nygard, *Release It!* (Steady State, Fail Fast, Shed Load, Backpressure). Check the references.

**Evidence:** challenge 1 is the first experiment. Run `FloodExperiment` with the producer at 20,000 events/s and the consumer at 1,000 events/s and plot the depth samples. Then run it with an unthrottled producer (a value of 0) with a small heap (`-Xmx64m`) and record what the JVM does when it runs out: the time, the depth, the GC log. Write it up in `../results/<date>-flood.md`, hypothesis first.
