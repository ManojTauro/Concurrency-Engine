# 02 The flag that never stops

## The problem
A worker thread spins in a loop until a flag is set; another thread sets the flag. With a plain `boolean`, nothing forces the worker to ever **re-read** the flag from memory. Once the loop is JIT-compiled, the compiler is allowed to read it once and loop forever. The fix is one keyword, `volatile`. You build both versions and look at what the JVM does.

## What you implement
`VisibilityDemo` in `src/main/java/dev/conc/p5/`, two methods with the same shape:
- `static boolean plainFlagStops(Duration runFor, Duration giveUp)`
- `static boolean volatileFlagStops(Duration runFor, Duration giveUp)`

Each does:
1. start a **daemon** worker thread that spins in an empty loop `while (!stop) { }` until the flag is true;
2. the caller sleeps for `runFor`, then sets the flag to true;
3. wait up to `giveUp` for the worker to finish;
4. return `true` if the worker finished, `false` if it is still running.

## Rules
- The only difference between the two methods: the flag is a plain `boolean` in one and `volatile` in the other.
- Keep the worker loop **empty**. Do not call anything inside it (a method call, a print or a sleep changes what the JIT does).
- The worker must be a daemon thread, so a stuck worker does not keep the JVM alive.
- The call must return within about `giveUp`, even if the worker never stops.

## Example
```java
VisibilityDemo.volatileFlagStops(Duration.ofMillis(200), Duration.ofSeconds(2));
   // true after about 200 ms: the worker saw the flag
VisibilityDemo.plainFlagStops(Duration.ofMillis(200), Duration.ofSeconds(2));
   // true after about 200 ms, OR false after about 2 s if the worker never saw the flag.
   // Which one you get depends on the JIT and your CPU. Finding out is the experiment.
```

## Run
`mvn -q test -Dtest='C02*'`. The tests cannot prove the plain version hangs (it may not, on a given run), so they check the volatile version, the time limit and that the worker is a daemon.

## Done when
All `C02*` tests pass and `../results/<date>-stop-flag.md` exists.

## The experiment is the point (write `../results/<date>-stop-flag.md`)
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

## Think about
"It works with `-Xint`" is not the same as "it is correct".
