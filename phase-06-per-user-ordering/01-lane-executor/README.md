# 01 Lane executor

**Do:** implement `LaneExecutor`. Single-thread lanes; `laneOf(key)` is given and decides where a key goes. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C01*'`

**Think about:**
- Why does one single-thread lane per key group give you ordering for free? What would break it if a lane had two threads?
- Hash partitioning is uneven. What happens to a lane when one user (a bot, a big merchant) produces most of the events? What can you do about it without losing ordering?
- Two *different* users share a lane. Is that a correctness problem? A performance one?
