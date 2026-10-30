# 02 Round runner

**Do:** implement `RoundRunner.run` with a `CyclicBarrier` (or a `Phaser`). Every thread finishes round r before any thread starts round r + 1. Contract in the Javadoc.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:**
- One thread throws in round 3. The other five are waiting at the barrier for it. What do they see, and how do you tell them? Does `barrier.reset()` solve it? When does it not?
- What if the thread fails in round 0, before the others even reach the barrier?
- A `CountDownLatch` cannot be reused for the next round. A `CyclicBarrier` can. What does `Phaser` add?
