# 03 Two-lock queue

**Do:** implement `TwoLockQueue`. A singly linked list with a head lock and a tail lock, so `offer` and `poll` rarely block each other. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C03*'`. The tests run several producers and consumers, check each producer's order, and watch `size()` from another thread.

**Think about (answer in `../results/<date>-queue-contention.md`):**
- Why does the list start with a dummy node? What would an `offer` and a `poll` on a one-item queue fight over without it?
- The `next` field of a node is written under the tail lock and read under the head lock. What guarantees the consumer sees it?
- Where do you update the size counter, and why does the order of "count" and "link" matter for a reader on another thread?
- How does `LinkedBlockingQueue` in the JDK do it? Read its source and note one difference.
