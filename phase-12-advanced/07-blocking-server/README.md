# 07 Blocking server

## The problem
Put the engine behind a network port. Clients connect over TCP and send **events as text lines**; the server answers each line with one response line. The simplest design is one thread for accepting connections and **one platform thread per connection** that does blocking reads and writes. It is the easiest to reason about; this challenge is also the baseline for challenges 8 and 9.

## The protocol (shared by challenges 7, 8 and 9)
A request is one line `userId,productId,TYPE` ending in `\n` (a `\r` just before it is ignored). `LineProtocol.handleLine` (given) parses it, hands the event to your sink and returns the response text.
```
client sends:   alice,sku-42,PURCHASE\n
server answers: OK\n

client sends:   alice,sku-42,NOPE\n
server answers: ERR unknown type: NOPE\n

client sends:   just-one-field\n
server answers: ERR expected userId,productId,TYPE\n

client sends:   a,b,VIEW\nc,d,CLICK\n          (two lines in one write)
server answers: OK\nOK\n

client sends:   alice,sku-1,VIEW               (no '\n', then the client disconnects)
server:         nothing: the incomplete line is discarded, the sink never sees it
```

## What you implement
`BlockingEventServer(EventSink sink)` in `src/main/java/dev/conc/p12/`, an `EventServer` with `start()`, `port()` and `close()`. Use `java.net.ServerSocket` and `Socket` (no `java.nio` channels).

## Rules
The whole contract is in `EventServer`; the key points:
- `start()` binds `127.0.0.1` on an ephemeral port; `port()` is that port.
- One acceptor thread named `p12-srv-acceptor`, and one platform thread per connection named `p12-srv-conn-0`, `p12-srv-conn-1`, ...
- For every **complete** line (its `\n` has arrived) write one response line + `\n`: the result of `LineProtocol.handleLine(line, ids, sink)`, where `ids` is **one counter for the whole server**.
- A line is complete **only** when its `\n` has arrived. Bytes after the last `\n` when the client disconnects are discarded: never parsed, never given to the sink.
- An `ERR` response does **not** close the connection.
- Responses on one connection are in request order, and one connection's events reach the sink in the order sent.
- One slow or silent client (a half-written line, an idle connection) must not delay the others.
- `close()` is idempotent. It stops accepting, closes every client connection, and ends every platform thread the server started (`p12-srv-...`). A client connecting after `close()` must fail.

## Run
`mvn -q test -Dtest='C07*'`

## Done when
All `C07*` tests pass.

## Think about
- The simplest correct design, and the easiest to reason about. What limits it? (Think: 10,000 idle connections, 1 MB stack reservation per thread, the scheduler.)
- Why can't you use `BufferedReader.readLine()` here? What does it do with a final line that has no newline when the connection closes?
- How do you stop all the threads, some of them blocked in `read()`? (Closing the socket from another thread makes the read throw.)
- What happens to the other clients if the sink is slow? Which design decisions would make a slow client unable to hurt anyone?
