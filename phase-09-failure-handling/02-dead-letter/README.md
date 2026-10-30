# 02 Dead letter

**Do:** implement `ResilientConsumer.processAll`.

**Run:** `mvn -q test -Dtest='C02*'`

**Think about:**
- "Retry in place" blocks every event behind the failing one. With a stream of 1 million events and one bad event retried 5 times with a 10 second wait, what is the delay for the rest? What are the alternatives (a retry queue, a delayed redelivery)?
- What must a dead letter contain for someone to fix it later? What would you add in a real system (a timestamp, a stack trace, the consumer version)?
- Who looks at the dead letter queue, and what happens when it fills up?
- Why is an `InterruptedException` neither a retry nor a dead letter?
