# 04 Knee and USL

**Do:** implement `Knee.find` and the four functions of `Scalability`. Pure maths.

**Run:** `mvn -q test -Dtest='C04*'`

**Think about:**
- Amdahl says speedup is limited by the serial fraction. The USL adds a second term, coherency, that makes throughput *fall* after a peak. What physical thing in your Phase 5 and Phase 3 experiments is the coherency cost (cache lines moving between cores, lock hand-offs)?
- Take your worker-sweep numbers for `cpuBurn` and fit `sigma` and `kappa` by trial and error so the USL curve matches. What values did you get, and what does a large `kappa` tell you to fix?
- Little's law says `L = lambda * W`. Use it on your knee: at the rate where p99 explodes, how many requests are in the system?
