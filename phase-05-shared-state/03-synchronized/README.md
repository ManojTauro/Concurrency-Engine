# 03 Fix with `synchronized`

## The problem
Challenge 1 showed a profile store that loses events under concurrent use. Fix it the simplest way: one lock that protects everything. You decide what the lock is and which operations must hold it.

## What you implement
`SynchronizedProfileStore` in `src/main/java/dev/conc/p5/`, implementing `ProfileStore`:
- `void record(Event event)`: fold the event into its user's `UserBehaviourProfile` (given), creating the profile on first sight.
- `long count(String userId, EventType type)`: events of that type recorded for that user; `0` for an unknown user.
- `long score(String userId)`: that user's score; `0` for an unknown user.
- `long totalEvents()`: total events recorded across all users.

## Rules
- Use the `synchronized` keyword. No `java.util.concurrent` locks or atomics.
- Correct with many threads calling `record` while other threads read. After all writers finish, every count and score is exact, and `totalEvents()` equals the number of events recorded.
- `totalEvents()` never goes backwards while writers run.
- A reader never fails or throws while writers are running.

## Example
8 threads each record a slice of 40 000 events for 64 users. Afterwards, for every user and every `EventType`, `count(user, type)` equals the number of that user's events of that type in the input, `score(user)` equals the sum of their weights, and `totalEvents() == 40_000`. An unknown user (`"nobody"`) gives `0` everywhere.

## Run
`mvn -q test -Dtest='C03*'`. The tests hammer your store with 8 threads and check every count exactly, and they run readers at the same time.

## Done when
All `C03*` tests pass. Start `../results/<date>-store-tradeoffs.md` (shared with challenges 4 and 5).

## Think about (answer in `../results/<date>-store-tradeoffs.md`)
- What does `synchronized` on a method actually lock?
- Why must the read methods be synchronized too? What can a reader see otherwise? (Hint: visibility, not only atomicity.)
- Does this store scale with more threads? Why or why not?
