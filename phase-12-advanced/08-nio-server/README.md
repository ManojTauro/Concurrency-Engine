# 08 NIO server

**Do:** implement `NioEventServer`: one thread, a `Selector`, non-blocking channels. The checklist of things to handle is in the Javadoc.

**Run:** `mvn -q test -Dtest='C08*'`. One test sends 4.6 MB of answers to a client that is not reading yet; another counts threads with 300 connections open.

**Think about:**
- A non-blocking `read` can return any number of bytes: half a line, five lines, nothing. Where do the bytes wait between reads, and why must that buffer be per connection?
- A non-blocking `write` can write only part of the buffer. Why do you register `OP_WRITE` only while there is something left to write? What happens to the CPU if you leave it on?
- Everything runs on one thread. What happens to *all* clients if the sink blocks for 100 ms? How do Netty and Trino's HTTP server deal with that (event loop plus worker pool)?
- This is what `epoll` does for you in the OSTEP week. Run the server and look at it with `strace -f -e trace=epoll_wait,read,write -p <pid>`.
