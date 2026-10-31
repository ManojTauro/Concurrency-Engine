# 01 Multi-producer pipeline

## The problem
Challenge 03 of phase 2 had one producer. Real engines have many: eight web servers each feeding events into one processing thread. With one producer, "I am done" is one signal. With eight, the consumer must stop **exactly when the last producer finishes**, not when the first does and not never.

## What you implement
`MultiProducerPipeline.run(List<List<Event>> sources, int capacity, Consumer<Event> sink)` in `src/main/java/dev/conc/p3/`. It returns the number of events consumed (`long`).
- One producer thread per source list, all feeding one `ArrayBlockingQueue` of the given capacity.
- One consumer thread calling `sink`.

## Rules
- Producer threads are named `p3-producer-0`, `p3-producer-1`, ...; the consumer is `p3-consumer`.
- The sink runs only on the consumer thread.
- Events **of one source** reach the sink in that source's order. Events of different sources may interleave in any way.
- `run` returns once every source is fully delivered, and no `p3-` thread is alive.
- No sources, or only empty sources: return `0`, do not hang.
- Producers of very different lengths must all finish (a short one finishing first must not stop the consumer early).
- Works with capacity 1 and 16 producers without deadlock.

## Example
```
sources = [ [a1, a2, a3],  [b1],  [c1, c2] ],  capacity 2
valid sink order:   a1 b1 a2 c1 a3 c2
valid sink order:   c1 c2 a1 a2 a3 b1
invalid:            a2 a1 ...     (source A out of order)
returns 6
```
`sources = [[], []]` returns 0. `sources = []` returns 0.

## Run
`mvn -q test -Dtest='C01*'`

## Done when
All `C01*` tests pass.

## Think about
- With one producer you send one poison pill. With eight, how does the consumer know the *last* one is done? Compare "one pill per producer" with "a counter, and the last producer sends the pill".
- Order inside one source is kept. Order across sources is not. Why can a queue never promise the second one?
- What is the right capacity? Run capacity 1, 16 and 1024 with 8 producers and describe how throughput changes.
