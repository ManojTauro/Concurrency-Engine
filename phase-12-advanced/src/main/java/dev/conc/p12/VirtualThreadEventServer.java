package dev.conc.p12;

import java.io.IOException;

/**
 * Challenge 9. The same blocking style as challenge 7, but each connection is handled on a VIRTUAL
 * thread (Thread.ofVirtual() or a virtual-thread-per-task executor). The acceptor is still a
 * platform thread named "p12-srv-acceptor". The sink must be called on a virtual thread. With a few
 * hundred or thousand connections open, the number of platform threads must stay small.
 */
public final class VirtualThreadEventServer implements EventServer {

    public VirtualThreadEventServer(EventSink sink) {
        // TODO
    }

    @Override
    public void start() throws IOException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int port() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void close() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
