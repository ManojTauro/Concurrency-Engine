package dev.conc.p12;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Challenge 4, part 2. Carry a request id down a call chain without passing it as a parameter and
 * without a ThreadLocal, using a ScopedValue (final in JDK 25, JEP 506).
 *
 *  - withRequestId(id, body) runs body with REQUEST_ID bound to id and returns its result; an
 *    exception from body propagates unchanged; the binding disappears when body is done;
 *  - currentRequestId() is the bound id, or empty when called outside any withRequestId;
 *  - bindings nest: an inner withRequestId shows its id inside, and the outer id is back after it;
 *  - the binding is NOT visible in a new Thread started inside body.
 */
public final class RequestContext {
    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private RequestContext() {
    }

    public static <T> T withRequestId(String id, Callable<T> body) throws Exception {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public static Optional<String> currentRequestId() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
