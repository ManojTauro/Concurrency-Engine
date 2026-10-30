# 04 Priority shedding

**Do:** implement `PriorityShedder`. Rules in the Javadoc.

**Run:** `mvn -q test -Dtest='C04*'`. One test checks the policy after every single offer on a random stream; the others check order and the capacity under contention.

**Think about:**
- Which operations need to be atomic together: "find the lowest", "compare", "evict", "insert"? What data structure makes the lowest and the highest both cheap?
- Shedding by type is a business decision. Who decides that a `VIEW` is worth less than a `PURCHASE`, and what if that is wrong for a flash sale?
- You shed work you already accepted. Is the caller told? What would a "429 Too Many Requests" with a `Retry-After` header do for the whole system (Phase 9)?
