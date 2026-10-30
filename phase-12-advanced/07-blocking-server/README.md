# 07 Blocking server

**Do:** implement `BlockingEventServer`: one acceptor thread and one platform thread per connection, blocking `read` and `write`. The contract is in `EventServer`; the line handling is in `LineProtocol.handleLine`, so your job is the connection handling.

**Run:** `mvn -q test -Dtest='C07*'`

**Think about:**
- The simplest correct design, and the easiest to reason about. What limits it? (Think: 10,000 idle connections, 1 MB stack reservation per thread, the scheduler.)
- Why can't you use `BufferedReader.readLine()` here? What does it do with a final line that has no newline when the connection closes?
- How do you stop all the threads, some of them blocked in `read()`? (Closing the socket from another thread makes the read throw.)
- What happens to the other clients if the sink is slow? Which design decisions would make a slow client unable to hurt anyone?
