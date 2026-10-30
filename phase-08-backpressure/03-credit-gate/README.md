# 03 Credit gate

**Do:** implement `CreditGate`, then `CreditPipeline.run`. The pool in the test has an unbounded queue; the credits are the only limit.

**Run:** `mvn -q test -Dtest='C03*'`. One test samples `queue size + active threads` from outside and checks it never exceeds the credits.

**Think about:**
- A bounded queue limits what is *waiting*. Credits limit what is waiting *plus* running (and, in a real system, what is on the network). Why does the second one match a downstream limit better?
- Where must the credit be returned so it is never leaked? What does a leaked credit do to the system over a day?
- `release()` more often than `acquire()` raises the limit silently. Why is it worth an exception?
- This is how TCP's window, HTTP/2 flow control and Reactive Streams `request(n)` work. Map the three to your gate.
