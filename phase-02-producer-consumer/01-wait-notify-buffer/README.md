# 01 Wait / notify buffer

**Do:** implement `WaitNotifyBuffer` in `src/main/java/dev/conc/p2/` with only `synchronized`, `wait` and `notify`/`notifyAll`. No `java.util.concurrent` classes. Use a circular array.

**Run:** `mvn -q test -Dtest='C01*'`. The tests include one producer with one consumer, then four producers with four consumers sharing a buffer of 16.

**Think about (answer in `../results/<date>-notify-vs-notifyall.md`):**
- Why does the waiting condition sit in a `while` loop and not an `if`?
- Both producers and consumers wait on the same monitor. What can go wrong with `notify` instead of `notifyAll`? Try it and say what you see.
- What does an interrupted `wait` leave behind? Is the buffer still consistent?
- What does `put` need to do with a `null`?
