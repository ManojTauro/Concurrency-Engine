# 01 Multi-producer pipeline

**Do:** implement `MultiProducerPipeline.run`. One thread per source list, one consumer, an `ArrayBlockingQueue`. The contract is in the Javadoc above `run`.

**Run:** `mvn -q test -Dtest='C01*'`

**Think about:**
- With one producer you send one poison pill. With eight, how does the consumer know the *last* one is done? Compare "one pill per producer" with "a counter, and the last producer sends the pill".
- Order inside one source is kept. Order across sources is not. Why can a queue never promise the second one?
- What is the right capacity? Run capacity 1, 16 and 1024 with 8 producers and describe how throughput changes.
