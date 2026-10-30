# Phase 5: Shared user state

Many worker threads update `userId -> UserBehaviourProfile`. You make it fail first, then fix it three ways, then learn the rule that decides whether any of it is safe.

| # | Challenge | Run (from this directory) |
|---|---|---|
| 1 | `01-lost-updates`: show a race on purpose | `mvn -q test -Dtest='C01*'` |
| 2 | `02-stop-flag`: a flag one thread sets and another never sees | `mvn -q test -Dtest='C02*'` |
| 3 | `03-synchronized`: fix with `synchronized` | `mvn -q test -Dtest='C03*'` |
| 4 | `04-concurrent-map`: `ConcurrentHashMap` + `LongAdder` | `mvn -q test -Dtest='C04*'` |
| 5 | `05-striped-locks`: `ReentrantLock` per stripe | `mvn -q test -Dtest='C05*'` |
| 6 | `06-memory-model`: ten Java Memory Model situations | `mvn -q test -Dtest='C06*'` |

The domain classes (`Event`, `EventType`, `UserBehaviourProfile`) are given complete here, so this phase does not depend on Phase 1. Your code is in `src/main/java/dev/conc/p5/`; replace each `TODO`.

**Read first (once):** JCIP ch. 2, 3 and 15; Shipilev, *Java Memory Model Pragmatics* (shipilev.net/blog/2014/jmm-pragmatics/); the JSR-133 Cookbook (gee.cs.oswego.edu/dl/jmm/cookbook.html). Check the links.

**Evidence:** every experiment gets a note in `../results/` from `_TEMPLATE.md`, hypothesis first. Challenges 1 and 2 are the first post: write both up before moving on.

Tests that depend on a race (challenges 1 and 2) need a machine with more than one core, and they assume a loss shows up within many attempts. If one fails on a quiet machine, run it again before suspecting your code.
