# 02 Serial executor

**Do:** implement `KeyedSerialExecutor`. Same promise as lanes, but no dedicated threads: each key has a queue, and a pool job drains that queue only while it has work. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:**
- Which operation must be atomic: "add a task and decide whether to start a drain job"? What happens if two threads add the first two tasks of a new key at the same moment?
- How do you remove the key's state exactly when its last task finishes, without losing a task that arrives at that same moment?
- What does `maxBatch` buy, and what does it cost? What goes wrong with `maxBatch` = 1? With `Integer.MAX_VALUE`?
- Compare with challenge 1: when would you choose lanes, when this?
