# 06 Memory model quiz

**Do:** implement `MemoryModelQuiz.answer(n)` and `reason(n)` for scenarios 1 to 10 below. Answer `GUARANTEED` only if a Java Memory Model rule guarantees the outcome. "It works on my machine" is `NOT_GUARANTEED`. The reason must name the rule (at least 30 characters).

**Run:** `mvn -q test -Dtest='C06*'`

Scenarios (T1 and T2 are threads):

1. `int data; boolean ready;` all plain. T1: `data = 42; ready = true;`. T2 loops until it sees `ready`, then reads `data`. Is T2 guaranteed to finish and see 42?
2. Same, but `ready` is `volatile`. Is T2 guaranteed to see 42 after it sees `ready == true`?
3. T1 writes plain `data`, then `synchronized (L) { }`. T2 later (after T1's block) does `synchronized (L) { }`, then reads `data`. Is T2 guaranteed to see T1's write?
4. Same as 3, but T2 reads `data` without taking the lock. Guaranteed?
5. `class Box { final int x; Box() { x = 7; } }`, published through a plain static field. T2 reads `box.x`. Guaranteed to see 7 (assuming `this` does not escape the constructor)?
6. Same class with a non-final `int x`, published the same way. Guaranteed to see 7?
7. The parent thread writes a plain field, then calls `child.start()`. The child reads the field. Guaranteed to see the write?
8. A child writes a plain field and ends. The parent calls `child.join()` and then reads the field. Guaranteed?
9. `volatile long counter;` two threads each run `counter++` a million times. Guaranteed final value 2,000,000?
10. T1 writes a plain field `x = 1`, then `map.put("k", obj)` on a `ConcurrentHashMap`. T2 gets `obj` from `map.get("k")` and reads `x`. Guaranteed to see 1?

**Then the manual part (write `../results/<date>-jcstress.md`):** confirm one reordering with `jcstress`. Create the project from the jcstress README (check it for the current archetype command), write the classic `data`/`ready` test from scenario 1, run it, and keep the table of observed outcomes. On x86 you may see nothing. Say so, and say why that does not make scenario 1 safe.

**Read:** JLS 17.4 (the memory model), the JSR-133 Cookbook, Shipilev's *JMM Pragmatics*.
