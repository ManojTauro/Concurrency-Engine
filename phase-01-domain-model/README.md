# Phase 1: Domain model

No threads yet. You build the types every later phase passes between threads, and you make them correct for that: immutable events, a deterministic generator, and a profile that is *not* yet thread-safe (Phase 5 breaks it on purpose).

| # | Challenge | Run |
|---|---|---|
| 1 | `01-event-model`: immutable `Event` with validation | `mvn -q test -Dtest='C01*'` |
| 2 | `02-event-generator`: seeded, skewed, per-user sequence numbers | `mvn -q test -Dtest='C02*'` |
| 3 | `03-behaviour-profile`: single-threaded `UserBehaviourProfile` | `mvn -q test -Dtest='C03*'` |

Run from this directory (`phase-01-domain-model`). The code you write is in `src/main/java/dev/conc/p1/`; replace each `TODO`. Phase 5 ships complete versions of these classes, so you can start it without finishing this phase, but you will learn more if you do both.

Read first: JCIP ch. 3 on immutability and safe publication.
