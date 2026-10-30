# 09 Virtual-thread server

**Do:** implement `VirtualThreadEventServer`: the blocking style of challenge 7, but each connection runs on a virtual thread.

**Run:** `mvn -q test -Dtest='C09*'`. The test opens 500 connections and checks the number of platform threads and that the sink ran on virtual threads.

**Then:** compare challenges 7, 8 and 9 (the note `../results/<date>-server-models.md`).

**Think about:**
- Your code for 9 is almost the same as for 7. What does the JVM do underneath so that the blocking `read` costs no OS thread? Where is the equivalent of the selector hiding?
- You get the readability of blocking code and (nearly) the scalability of NIO. What do you still have to bound yourself (connections, in-flight work, memory)?
- When would you still choose the NIO event loop (latency control, a single-threaded design with no locks, a framework that already does it)?
