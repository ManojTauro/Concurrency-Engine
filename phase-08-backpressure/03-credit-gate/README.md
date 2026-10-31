# 03 Credit gate

## The problem
A bounded queue limits what is **waiting**. A real overload limit is on what is waiting **plus running** (and, in a real system, what is on the network). The fix is **credits**: the producer must take a credit before sending work, and the worker returns it when the work is finished. No credit, no sending. The number of credits is the hard limit on work in flight. This is how TCP's window, HTTP/2 flow control and Reactive Streams `request(n)` work.

## What you implement
In `src/main/java/dev/conc/p8/`:
1. `CreditGate(int credits)`:
   - `void acquire() throws InterruptedException`: take a credit, waiting if none is free
   - `boolean tryAcquire(Duration timeout) throws InterruptedException`: wait up to the timeout, `false` if no credit appeared
   - `void release()`: give a credit back
   - `int inFlight()`: credits taken minus credits returned
2. `CreditPipeline.run(List<Event> events, int credits, ThreadPoolExecutor pool, Consumer<Event> sink) throws InterruptedException`.

## Rules
- `credits < 1` throws `IllegalArgumentException`.
- `release()` when nothing is in flight throws `IllegalStateException` (a double release would silently raise the limit).
- Never more than `credits` in flight at once, with any number of threads.
- `CreditPipeline`: the **calling thread is the producer**. For each event it takes a credit from a `CreditGate(credits)` **before** handing the event to the pool, and the worker gives the credit back when the sink is done, **even if the sink threw**. The pool is given; it may have an unbounded queue: the credits are the only limit. So queued plus running events never exceed `credits`.
- `run` returns the number of events the sink handled **without throwing**, after every event has been handled. A throwing sink must not stall the run. `run` does not shut the pool down.

## Example
```
CreditGate g = new CreditGate(2);
g.acquire(); g.acquire();                          // inFlight() == 2
g.tryAcquire(Duration.ofMillis(80))                // false after ~80 ms, no credit free
g.release();                                       // inFlight() == 1
g.tryAcquire(Duration.ofMillis(80))                // true
g.release(); g.release(); g.release();             // third release: IllegalStateException

CreditPipeline.run(300 events, credits=4, pool, sink that throws on every 10th event)
   // returns 270 (the 30 failing events are not counted), every credit was returned
```
With `credits = 1` the sink never runs two events at once.

## Run
`mvn -q test -Dtest='C03*'`. One test samples `queue size + active threads` from outside and checks it never exceeds the credits (plus a small sampling margin).

## Done when
All `C03*` tests pass.

## Think about
- A bounded queue limits what is *waiting*. Credits limit what is waiting *plus* running (and, in a real system, what is on the network). Why does the second one match a downstream limit better?
- Where must the credit be returned so it is never leaked? What does a leaked credit do to the system over a day?
- `release()` more often than `acquire()` raises the limit silently. Why is it worth an exception?
- This is how TCP's window, HTTP/2 flow control and Reactive Streams `request(n)` work. Map the three to your gate.
