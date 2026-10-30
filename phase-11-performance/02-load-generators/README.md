# 02 Load generators

**Do:** implement `ClosedLoopGenerator` and `OpenLoopGenerator`.

**Run:** `mvn -q test -Dtest='C02*'`. The service is slow once, for 400 ms. See what each generator reports.

**Think about (answer in `../results/<date>-coordinated-omission.md`):**
- The closed-loop client sends the next request only after the previous one answers. During a 400 ms stall, how many requests did it *not* send? How many samples show the stall? What happens to its p99?
- The open-loop generator keeps to a schedule. Why must the latency be measured from the *intended* time and not from when the request was actually sent?
- This effect is called coordinated omission. Which common tools suffer from it, and which fix it (wrk2, HdrHistogram's corrected recording)?
- A real user arrives when they arrive. Which generator models that, and which models a connection pool with 10 connections?
