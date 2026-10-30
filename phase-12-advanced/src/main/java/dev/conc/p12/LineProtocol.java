package dev.conc.p12;

import java.util.concurrent.atomic.AtomicLong;

/** Given. One request line, one response line. */
public final class LineProtocol {
    private LineProtocol() {
    }

    /** Parse "userId,productId,TYPE", for example "alice,sku-42,PURCHASE". */
    public static Event parse(String line, long eventId) {
        String[] parts = line.split(",", -1);
        if (parts.length != 3) throw new IllegalArgumentException("expected userId,productId,TYPE");
        if (parts[0].isBlank() || parts[1].isBlank()) throw new IllegalArgumentException("empty field");
        EventType type;
        try {
            type = EventType.valueOf(parts[2].trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown type: " + parts[2].trim());
        }
        return new Event(eventId, parts[0], parts[1], type, System.currentTimeMillis(), 0);
    }

    /**
     * Handle one complete line (without its "\n" and without a trailing "\r"): parse it, give the
     * event an id from {@code ids}, pass it to the sink. Returns the response line WITHOUT the
     * newline: "OK", or "ERR " followed by the reason when the line is bad or the sink throws.
     */
    public static String handleLine(String line, AtomicLong ids, EventSink sink) {
        try {
            sink.accept(parse(line, ids.incrementAndGet()));
            return "OK";
        } catch (Exception e) {
            String msg = String.valueOf(e.getMessage()).replace('\n', ' ').replace('\r', ' ');
            return "ERR " + msg;
        }
    }
}
