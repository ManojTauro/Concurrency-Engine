# 03 Stamped lock

## The problem
A shared pair of numbers that change together (`a` and `b = 2 * a`) is read thousands of times for every write. Locking every read is wasteful, since readers do not change anything. A `StampedLock` offers an **optimistic read**: take a stamp (no lock), copy the fields, then ask "did a write happen meanwhile?" (`validate`). If not, the copy is good and you paid almost nothing; if yes, fall back to a real read lock. The danger: a copy taken during a write can be **torn** (a new `a` with an old `b`).

## What you implement
`ScoreBoard` in `src/main/java/dev/conc/p12/` (`Snapshot(long a, long b)` is given):
- `void set(long x)`
- `Snapshot read()`
- `long optimisticReads()`, `long fallbackReads()`

## Rules
- `set(x)` stores `a = x` and `b = 2 * x` under the **write** lock.
- `read()` returns a `Snapshot` that is **never torn**: its `b` is always exactly `2 * a` (initially both 0).
- Try an **optimistic read** first (`tryOptimisticRead`, copy the fields to locals, then `validate`). Only if validation fails, fall back to a real **read lock**.
- `optimisticReads()` counts reads served by a validated optimistic stamp; `fallbackReads()` counts reads that had to take the read lock. Their **sum** is the number of reads.
- The fields are plain (non-volatile) fields.
- `StampedLock` is not reentrant: never call `set()` or `read()` while holding the lock.

## Example
```java
var board = new ScoreBoard();
board.read();             // Snapshot(0, 0)
board.set(21);
board.read();             // Snapshot(21, 42)
board.optimisticReads() + board.fallbackReads();    // 2

// one writer calling set(1), set(2), set(3)... while six threads call read() in a loop:
// every Snapshot any reader gets satisfies b == 2 * a. A Snapshot(3, 4) would mean a torn read.
// With no writer running, every read is optimistic (fallbackReads() stays 0).
```

## Run
`mvn -q test -Dtest='C03*'`. One test reads from six threads while a writer runs and checks that no snapshot is ever torn.

## Done when
All `C03*` tests pass.

## Think about
- In an optimistic read you copy the fields *before* you validate. Why must you never use the values (or follow pointers from them) before `validate` says yes?
- Compare against `ReentrantReadWriteLock` and `synchronized` with 8 readers and 1 writer, then with 8 readers and no writer. Where does `StampedLock` win, by how much, and why (readers do not write to shared memory)?
- `StampedLock` is not reentrant and has no `Condition`. What bugs does that invite?
- When is the optimistic path a bad idea (write-heavy workloads, long reads)?
