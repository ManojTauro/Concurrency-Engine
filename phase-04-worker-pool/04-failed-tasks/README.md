# 04 Failed tasks

**Do:** implement `afterExecute` and `failureCount` in `FailureAwarePool`. The constructor is given.

**Run:** `mvn -q test -Dtest='C04*'`

**Think about:**
- `execute(Runnable)` and `submit(Runnable)` treat an exception differently. Where does each one put it? Why does `afterExecute` get a non-null `Throwable` in one case and null in the other?
- A `Future` that is never asked for its result: what happens to the failure inside it? How many production systems lose errors this way?
- Why must you check `isDone()` before `get()` in `afterExecute`? What would a cancelled task do to a blocking `get()`?
