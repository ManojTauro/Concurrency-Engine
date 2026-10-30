# 02 Interruptible worker

**Do:** implement `EventWorker`. The loop, the three kinds of exit, and the accounting are in the Javadoc.

**Run:** `mvn -q test -Dtest='C02*'`. The accounting test interrupts the worker at 40 different moments and checks that no event is lost or counted twice.

**Think about:**
- Catching `InterruptedException` clears the thread's interrupt flag. If you swallow it and carry on, what has the next `take()` lost? What does the code that interrupted you now believe?
- The interrupt arrives while the step is running. The event was taken from the queue but not finished. Whose job is it to not lose it?
- Interrupts are cooperative: only blocking calls and explicit checks see them. A step that loops on the CPU for a minute never notices. How would you design a long CPU step to be cancellable?
