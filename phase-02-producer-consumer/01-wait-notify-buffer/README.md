# 01 Wait / notify buffer

## The problem
A producer thread makes events faster or slower than a consumer thread handles them. They need a **bounded buffer** between them: a queue of fixed capacity that
- makes the producer **wait** when the buffer is full (so it cannot run ahead forever), and
- makes the consumer **wait** when it is empty (so it does not spin).

`ArrayBlockingQueue` does this for you. Here you build it by hand, so you see what it does.

## What you implement
`WaitNotifyBuffer<T>` in `src/main/java/dev/conc/p2/`, implementing `Buffer<T>`:
- `new WaitNotifyBuffer<>(capacity)`
- `void put(T item) throws InterruptedException`
- `T take() throws InterruptedException`
- `int size()`

Only `synchronized`, `wait`, `notify` / `notifyAll`. No `java.util.concurrent` classes. Store items in a circular array.

## Rules
- `capacity < 1` throws `IllegalArgumentException` (in the constructor).
- `put(null)` throws `NullPointerException`.
- FIFO: items come out in the order they went in.
- `put` blocks while the buffer holds `capacity` items; `take` blocks while it is empty.
- A blocked `put` or `take` ends with `InterruptedException` when its thread is interrupted, and the buffer must still be consistent afterwards (no item lost or duplicated, `size()` correct).
- Safe for many producers and many consumers at the same time. Every item is taken exactly once.

## Example
Capacity 2:
```
put(1)            // size 1
put(2)            // size 2
put(3)            // BLOCKS: full. Thread waits.
take() -> 1       // size 1, and the blocked put(3) now completes, size 2
take() -> 2
take() -> 3       // size 0
take()            // BLOCKS: empty, until another thread calls put(...)
```
Wrap-around: with capacity 3, doing `put(i)` then `take()` a hundred times in a row must return 0, 1, 2, ..., 99 (the array indexes wrap).

## Run
`mvn -q test -Dtest='C01*'`. The tests include one producer with one consumer (100 000 items in order), then four producers with four consumers sharing a buffer of 16.

## Done when
All `C01*` tests pass, and you have written `../results/<date>-notify-vs-notifyall.md` (see below).

## Think about (answer in `../results/<date>-notify-vs-notifyall.md`)
- Why does the waiting condition sit in a `while` loop and not an `if`?
- Both producers and consumers wait on the same monitor. What can go wrong with `notify` instead of `notifyAll`? Try it and say what you see.
- What does an interrupted `wait` leave behind? Is the buffer still consistent?
- What does `put` need to do with a `null`?
