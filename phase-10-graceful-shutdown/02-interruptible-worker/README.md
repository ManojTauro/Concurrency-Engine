# 02 Interruptible worker

## The problem
The usual way to stop a worker thread is to **interrupt** it. An interrupt can arrive at any moment: while the worker waits for an event, or in the middle of processing one. A correct worker must (a) stop promptly, (b) lose no event, not even the one it was in the middle of, and (c) leave the interrupt visible to whoever owns the thread (catching `InterruptedException` clears the flag, so you must set it again).

## What you implement
`EventWorker(BlockingQueue<Event> queue, ProcessingStep step)` in `src/main/java/dev/conc/p10/`, a `Runnable`, with:
- `run()`
- `long processed()`, `long failed()`
- `Optional<Event> interruptedEvent()`
- `boolean exitedWithInterruptFlag()`

`ProcessingStep.process(Event)` is given and may throw `InterruptedException` (like any blocking work).

## Rules
`run()` loops: take an event from the queue, run the step, count it.
- It **stops when its thread is interrupted**, whether that happens while it waits for an event or while the step is running (the step then throws `InterruptedException`).
- An event whose step was interrupted is **not** counted as processed: `interruptedEvent()` returns it so it can be handled again. An event fully processed is counted in `processed()`.
- A `RuntimeException` from the step counts in `failed()` and the worker carries on.
- When `run()` ends because of an interrupt, the thread's interrupt flag is **set again** and `exitedWithInterruptFlag()` is `true`.
- **Nothing is lost:** events processed + the interrupted one + the events left in the queue equal the events that were put in. (If the step also fails on some events, add `failed()` to the left side: a failed event was taken and reported, not lost.)

## Example
50 events queued, a step that records the event id; the worker runs, processes all 50 in order, then waits on the empty queue. Interrupt the thread:
```
worker stops within ~500 ms
processed() = 50     interruptedEvent() = Optional.empty     exitedWithInterruptFlag() = true
```
Events 1 and 2 queued, the step blocks forever on event 1; interrupt the thread:
```
step throws InterruptedException for event 1
processed() = 0     interruptedEvent() = Optional[event 1]     queue still holds event 2
exitedWithInterruptFlag() = true
```
Step throws `IllegalStateException` on 10 of 30 events: `processed() = 20`, `failed() = 10`, the worker did not stop.

## Run
`mvn -q test -Dtest='C02*'`. The accounting test interrupts the worker at 40 different moments and checks that no event is lost or counted twice.

## Done when
All `C02*` tests pass.

## Think about
- Catching `InterruptedException` clears the thread's interrupt flag. If you swallow it and carry on, what has the next `take()` lost? What does the code that interrupted you now believe?
- The interrupt arrives while the step is running. The event was taken from the queue but not finished. Whose job is it to not lose it?
- Interrupts are cooperative: only blocking calls and explicit checks see them. A step that loops on the CPU for a minute never notices. How would you design a long CPU step to be cancellable?
