# 09 Virtual-thread server

## The problem
Challenge 7 is easy to write and read but needs one expensive OS thread per connection. Challenge 8 scales but is hard to write. **Virtual threads** promise the first style with (nearly) the scalability of the second: the code is the blocking server of challenge 7, but each connection runs on a cheap virtual thread, and the JVM parks it while it waits on the socket. You change very little and compare all three designs.

## What you implement
`VirtualThreadEventServer(EventSink sink)` in `src/main/java/dev/conc/p12/`, an `EventServer`: the same blocking style as challenge 7 (`ServerSocket`/`Socket`, a blocking read and write loop per connection), but each connection is handled on a **virtual thread** (`Thread.ofVirtual()` or a virtual-thread-per-task executor).

## Rules
- Everything in the `EventServer` contract and the protocol from challenge 7 holds.
- The acceptor is still a **platform** thread named `p12-srv-acceptor`.
- The sink must be called **on a virtual thread**.
- With a few hundred or thousand connections open, the number of **platform** threads must stay small.
- `close()` is idempotent, closes every connection, ends the acceptor, and a client connecting afterwards must fail. Make sure no connection's virtual thread is left running afterwards.

## Example
```
500 clients connect and stay connected, each sends "u,p,VIEW\n" and gets "OK\n"
   inside the sink: Thread.currentThread().isVirtual() == true
   platform thread count while connected: a handful (acceptor + the JVM's carrier threads), not 500
```
Your code should be almost line for line the same as challenge 7; if it is not, ask why.

## Run
`mvn -q test -Dtest='C09*'`. The test opens 500 connections and checks the number of platform threads and that the sink ran on virtual threads.

## Done when
All `C09*` tests pass and `../results/<date>-server-models.md` compares challenges 7, 8 and 9.

## Then
Compare challenges 7, 8 and 9 in `../results/<date>-server-models.md`: reuse Phase 11; for each server measure p50/p99/p99.9 from your open-loop generator at 1,000 and 10,000 connections, thread count (`jcmd`/`ps -L`), and what happens when 5% of the clients are slow. Say which design you would build for which workload, and why.

## Think about
- Your code for 9 is almost the same as for 7. What does the JVM do underneath so that the blocking `read` costs no OS thread? Where is the equivalent of the selector hiding?
- You get the readability of blocking code and (nearly) the scalability of NIO. What do you still have to bound yourself (connections, in-flight work, memory)?
- When would you still choose the NIO event loop (latency control, a single-threaded design with no locks, a framework that already does it)?
