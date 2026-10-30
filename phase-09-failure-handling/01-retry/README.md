# 01 Retry

**Do:** implement `Retry.call` with the rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C01*'`. The sleeper in the tests records the waits instead of sleeping.

**Think about:**
- Why exponential and not a fixed wait? Why a cap? Why jitter?
- `baseDelay * 2^(k-1)` for large `k` overflows a `long`. What does the number become, and what would your code then sleep for? How do you make the doubling safe?
- Which exceptions should never be retried, and who decides (the callee, the caller)? What happens if you retry a non-idempotent call (challenge 4)?
- A retry multiplies load exactly when a service is struggling. If every layer in a call chain retries 3 times, how many calls reach the bottom service?
