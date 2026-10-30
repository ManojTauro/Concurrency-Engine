# 01 Event model

**Why this matters for concurrency:** an `Event` is created by one thread and read by many. If it is immutable (all fields final, no setters, nothing mutable inside), any thread can read it with no lock and no visibility problem. That is the cheapest kind of thread safety there is.

**Do:** in `src/main/java/dev/conc/p1/`:
- `EventType.weight()`: VIEW 1, CLICK 2, ADD_TO_CART 5, PURCHASE 10.
- The `Event` compact constructor:
  - `userId`, `productId`, `type` must not be null (`NullPointerException`).
  - `userId` and `productId` must not be blank (`IllegalArgumentException`).
  - `eventId >= 1`, `userSeq >= 1`, `timestampMillis >= 0` (`IllegalArgumentException`).

**Run:** `mvn -q test -Dtest='C01*'`

**Think about:** why a record with only final primitives, strings and an enum is safe to share. What would break if `Event` held a `List<String>` field?
