# 01 Virtual threads

**Do:** implement `FanOut.runAll` with a virtual-thread-per-task executor.

**Run:** `mvn -q test -Dtest='C01*'`. The test also times it against the given `PlatformFanOut`.

**Think about:**
- Why does a virtual thread that blocks in `sleep`, a socket read or a `BlockingQueue.take` not tie up an OS thread? What does the JVM do when it blocks, and what is a *carrier thread*?
- Which of your Phase 4 and 8 rules still apply (bounding in-flight work with credits)? Ten thousand cheap threads can still overload a database with ten thousand connections.
- Virtual threads do not make CPU-bound code faster. Run `cpuBurn` on them and on a pool of `nproc` threads: what do you see?
- In JDK 21 a `synchronized` block that blocked *pinned* the carrier; JEP 491 (JDK 24) removed that for monitors. What still pins (native frames, some `Object.wait` cases)? How do you find it (`-Djdk.tracePinnedThreads`, JFR `VirtualThreadPinned`)?
