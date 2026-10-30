# 02 Condition buffer

**Do:** implement `ConditionBuffer` with one `ReentrantLock` and two `Condition`s (`notFull`, `notEmpty`), then the timed `offer` and `poll`.

**Run:** `mvn -q test -Dtest='C02*'`. The first tests are the same contract as challenge 1; the timeout tests are new.

**Think about:**
- With two conditions you can use `signal` and not `signalAll`. Why is that safe here, and what changed compared with the `synchronized` version?
- `awaitNanos` returns the time left. What goes wrong if you wait for the full timeout again after a spurious wakeup?
- Where must `unlock` be? What happens to the lock if `await` is interrupted?
