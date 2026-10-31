# 02 Load generators

## The problem
Most load tests lie about latency. A **closed-loop** client sends a request, waits for the answer, then sends the next. If the service stalls for 400 ms, that client simply sends nothing during the stall: the hundreds of requests that *would* have arrived are never made, never measured, and the p99 looks fine. This is **coordinated omission**. An **open-loop** generator sends on a fixed schedule whatever the service is doing, and measures each latency from the request's **intended** start time, so the backlog shows up honestly. You build both and see the difference on the same stalling service.

## What you implement
In `src/main/java/dev/conc/p11/` (`Service`, `Recorder`, `ListRecorder` are given):
1. `ClosedLoopGenerator.run(Service service, int clients, Duration duration, Recorder recorder)` returning the number of requests made.
2. `OpenLoopGenerator.run(Service service, double ratePerSecond, Duration duration, int maxThreads, Recorder recorder)` returning the number of requests issued.

## Rules
**Closed loop**
- `clients` threads named `p11-client-0`, `p11-client-1`, ...; each sends a request, waits for the answer, then sends the next.
- A request's latency = (time the call returned) - (time it was sent).
- After `duration` it stops sending, waits for the clients to finish, returns the count. A call that throws is still timed and recorded.

**Open loop**
- Request `i` (`i = 0, 1, 2, ...`) is **intended** at `start + i / ratePerSecond`.
- A thread named `p11-sender` releases each request at its intended time (sleep or park until then; never faster than the schedule) to a pool of `maxThreads` threads named `p11-open-0`, ... with an **unbounded queue**.
- Recorded latency = (time it **completed**) - (its **intended** time), not completed minus actually started.
- Requests are issued while their intended time is before the end of `duration`. `run` waits for all of them to complete and returns how many were issued. A call that throws is still recorded.
- `ratePerSecond <= 0` or `maxThreads < 1` throws `IllegalArgumentException`.

Neither leaves a `p11-` thread alive afterwards.

## Example
A service that answers in 5 ms, except one call that stalls for 400 ms. One client / 500 requests per second for 1.5 s:
```
closed loop, 1 client:   several hundred requests, only ONE sample shows the 400 ms stall
                         max = ~400 ms, but p99 is small (under 100 ms): the stall looks like one unlucky request
open loop, 500 req/s:    about 750 requests issued (500/s x 1.5 s)
                         about 200 of them were due during the 400 ms stall and queue up behind it,
                         each is measured from its intended time, so p99 is large (over 250 ms)
```
Open loop at a rate the service can serve (for example 100 req/s for 2 s against a 5 ms service): about 200 requests, p99 stays small.
Open loop at a rate the service cannot serve (50 requests in 1 s against a service that manages about 33/s): still exactly about 50 issued, and the latency of the late ones grows.

## Run
`mvn -q test -Dtest='C02*'` (takes a few seconds on purpose). The service is slow once, for 400 ms. See what each generator reports.

## Done when
All `C02*` tests pass and `../results/<date>-coordinated-omission.md` exists with both histograms side by side.

## Think about (answer in `../results/<date>-coordinated-omission.md`)
- The closed-loop client sends the next request only after the previous one answers. During a 400 ms stall, how many requests did it *not* send? How many samples show the stall? What happens to its p99?
- The open-loop generator keeps to a schedule. Why must the latency be measured from the *intended* time and not from when the request was actually sent?
- This effect is called coordinated omission. Which common tools suffer from it, and which fix it (wrk2, HdrHistogram's corrected recording)?
- A real user arrives when they arrive. Which generator models that, and which models a connection pool with 10 connections?
