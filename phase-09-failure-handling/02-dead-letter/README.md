# 02 Dead letter

## The problem
One malformed or unlucky event must **never stop the stream**. For each event you retry a few times; if it still fails (or can never succeed), you park it in a **dead letter queue** with the reason, so a human can look at it later, and carry on with the next event.

## What you implement
`ResilientConsumer(EventHandler handler, int maxAttempts, DeadLetterQueue dlq)` with `Summary processAll(List<Event> events) throws InterruptedException` in `src/main/java/dev/conc/p9/`. Given: `DeadLetterQueue`, `DeadLetter(event, cause, attempts)`, `Summary(succeeded, deadLettered)`, `PermanentFailureException`.

## Rules
For each event, **in order**:
- Call the handler. If it throws an `Exception`, call it again, up to `maxAttempts` calls in total (no waiting between calls). The retries of an event happen **before the next event starts**.
- Success: count it as succeeded.
- A `PermanentFailureException`: **do not retry**. Add `DeadLetter(event, thatException, attemptsSoFar)` to the queue.
- All attempts failed: add `DeadLetter(event, the LAST exception, maxAttempts)`.
- An `InterruptedException` from the handler stops `processAll` at once and is rethrown. The interrupted event is **not** dead-lettered and later events are not touched.
- `RuntimeException`s are retried like any other exception.
- `maxAttempts < 1` throws `IllegalArgumentException`.
- Dead letters keep the order of the events.

## Example
`maxAttempts = 3`; events 1..4. Event 1 succeeds at once; event 2 fails 3 times (messages "flaky 2 try 1/2/3"); event 3 succeeds on its second try; event 4 throws `PermanentFailureException`:
```
handler calls:  1#1  2#1  2#2  2#3  3#1  3#2  4#1
Summary(succeeded = 2, deadLettered = 2)
dlq: DeadLetter(event 2, cause "flaky 2 try 3", attempts 3)
     DeadLetter(event 4, cause PermanentFailureException, attempts 1)
```
(The dead letter for event 2 holds the **last** exception, "try 3", not the first.)

## Run
`mvn -q test -Dtest='C02*'`

## Done when
All `C02*` tests pass.

## Think about
- "Retry in place" blocks every event behind the failing one. With a stream of 1 million events and one bad event retried 5 times with a 10 second wait, what is the delay for the rest? What are the alternatives (a retry queue, a delayed redelivery)?
- What must a dead letter contain for someone to fix it later? What would you add in a real system (a timestamp, a stack trace, the consumer version)?
- Who looks at the dead letter queue, and what happens when it fills up?
- Why is an `InterruptedException` neither a retry nor a dead letter?
