# 08 NIO server

## The problem
Same server as challenge 7 (same protocol and contract: see its README), but with **one thread for every connection**. A `Selector` tells that one thread which connections are ready to read, write or accept; it never blocks on a single client. This is the event-loop model of Netty, Node.js and Redis, and what `epoll` does underneath. The price: you manage by hand what a blocking thread gave you for free, namely the half-read line and the half-written response.

## What you implement
`NioEventServer(EventSink sink)` in `src/main/java/dev/conc/p12/`, an `EventServer`. Use `java.nio` (`Selector`, `ServerSocketChannel`, `SocketChannel`, all non-blocking).

## Rules
- Everything in the `EventServer` contract and the protocol from challenge 7 holds.
- **One** thread named `p12-srv-selector` accepts, reads, parses and writes for **all** connections. The sink is called on that thread. **No other thread** may be started, however many connections there are.
- Things you must handle:
  - a read that returns **half a line**: keep the bytes **per connection** until the `\n` arrives;
  - **several lines in one read**: answer each, in order;
  - a **write that does not fit** in the socket buffer: keep the rest per connection and register for `OP_WRITE` until it is sent;
  - a client that closes (`read` returns -1); an unfinished line is discarded;
  - `close()` called from another thread (`selector.wakeup()`), which ends the selector thread and closes every connection.

## Example
```
client writes "alice,sk"                    -> nothing yet (no '\n')
client writes "u-1,VIEW\nbob,sku-2,CLICK\n" -> the server answers OK\nOK\n  (the first line was completed by the second write)

client sends 20 000 lines and does not read:
   responses pile up in the server; the socket buffer fills; the rest is kept per connection,
   OP_WRITE is registered; when the client starts reading, everything is delivered in order
   and meanwhile OTHER clients keep being served.

300 connections open at once: Thread.getAllStackTraces() shows one p12-srv-selector thread, not 300.
```

## Run
`mvn -q test -Dtest='C08*'`. One test sends 4.6 MB of answers to a client that is not reading yet; another counts threads with 300 connections open.

## Done when
All `C08*` tests pass.

## Think about
- A non-blocking `read` can return any number of bytes: half a line, five lines, nothing. Where do the bytes wait between reads, and why must that buffer be per connection?
- A non-blocking `write` can write only part of the buffer. Why do you register `OP_WRITE` only while there is something left to write? What happens to the CPU if you leave it on?
- Everything runs on one thread. What happens to *all* clients if the sink blocks for 100 ms? How do Netty and Trino's HTTP server deal with that (event loop plus worker pool)?
- This is what `epoll` does for you in the OSTEP week. Run the server and look at it with `strace -f -e trace=epoll_wait,read,write -p <pid>`.
