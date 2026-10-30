package dev.conc.p12;

import java.io.IOException;

/**
 * Challenge 8. One thread named "p12-srv-selector" does everything: it accepts, reads, parses and
 * writes for ALL connections, using java.nio (Selector, ServerSocketChannel, SocketChannel, all in
 * non-blocking mode). The sink is called on that thread. No other thread may be started, however
 * many connections there are.
 *
 * Things you must handle: a read that returns half a line (keep the bytes per connection until the
 * '\n' arrives), several lines in one read, a write that does not fit in the socket buffer (keep
 * the rest per connection and register for OP_WRITE), a client that closes (read returns -1), and
 * close() called from another thread (selector.wakeup()).
 */
public final class NioEventServer implements EventServer {

    public NioEventServer(EventSink sink) {
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
