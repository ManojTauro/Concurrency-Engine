# 02 The flag that never stops

**Break it first, and look at the JIT.** A worker spins on `while (!stop) { }`. Another thread sets `stop = true`. With a plain boolean, nothing makes the worker re-read it, and once the loop is compiled the JIT is allowed to read it once and loop forever.

**Do:** implement `VisibilityDemo` (`plainFlagStops`, `volatileFlagStops`). The only difference between the two is `volatile` on the flag. Keep the worker loop empty.

**Run:** `mvn -q test -Dtest='C02*'`. The tests cannot prove the plain version hangs (it may not, on a given run), so they check the volatile version, the time limit and that the worker is a daemon.

**The experiment is the point (write `../results/<date>-stop-flag.md`):**
```
mvn -q compile
java -cp target/classes dev.conc.p5.VisibilityDemoMain
java -Xint -cp target/classes dev.conc.p5.VisibilityDemoMain
java -XX:+PrintCompilation -cp target/classes dev.conc.p5.VisibilityDemoMain
```
- Hypothesis first: which of the three runs hang for the plain flag, and why?
- With `-Xint` there is no JIT. What changes? What does that tell you about where the problem comes from?
- With `PrintCompilation`, can you see the loop's method being compiled around when it stops responding?
- Do not assume it reproduces on JDK 25 on your CPU. If it does not hang, record that and think about why.

**Think about:** "it works with `-Xint`" is not the same as "it is correct".
