# 03 Worker sweep

**Do:** implement `WorkerSweep.run` and `toCsv`.

**Run:** `mvn -q test -Dtest='C03*'`. Then run your sweep for real: 1, 2, 4, 8, 16, 32 workers, with `Work.cpuBurn(200_000)` and with `Work.sleepMillis(5)`, 2,000 events each.

**Think about:**
- `cpuBurn`: where does throughput stop rising, and how does that relate to `nproc`? What happens to the p99 after the knee?
- `sleepMillis`: why does it scale far past the core count? What is the limit then?
- The sojourn time includes the wait in the queue. Why does p99 *fall* when you add workers at first, and what does that tell you about where latency comes from?
- What did you control (CPU frequency, other programs, JIT warm-up, GC)? Run the sweep twice: is it repeatable?
