package dev.conc.p12;

import java.io.IOException;

/**
 * Challenge 7. The classic design: one acceptor thread named "p12-srv-acceptor" and one platform
 * thread per connection named "p12-srv-conn-0", "p12-srv-conn-1", ..., each doing blocking reads
 * and writes on its own socket (java.net.ServerSocket and Socket). Do not use java.nio channels.
 * Remember the contract in EventServer, especially: a final line without '\n' is discarded.
 */
public final class BlockingEventServer implements EventServer {

    public BlockingEventServer(EventSink sink) {
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
