package dev.conc.p12;

import java.io.IOException;

/**
 * A TCP server for the event line protocol. The contract, shared by challenges 7, 8 and 9:
 *
 *  - start() binds 127.0.0.1 on an ephemeral port; port() is that port; close() stops the server;
 *  - a request is a line ending in '\n' (a '\r' just before it is ignored). For every complete
 *    line the server writes one response line + "\n", the result of
 *    LineProtocol.handleLine(line, ids, sink), where ids is one counter for the whole server;
 *  - a line is complete ONLY when its '\n' has arrived. Bytes after the last '\n' when the client
 *    disconnects are discarded: they are never parsed and never reach the sink;
 *  - an "ERR" response does not close the connection;
 *  - responses on one connection are in request order, and the events of one connection reach
 *    the sink in the order they were sent;
 *  - one slow or silent client (a half-written line, an idle connection) must not delay the others;
 *  - close() is idempotent. It stops accepting, closes every client connection, and ends every
 *    platform thread the server started (they are named "p12-srv-..."). A client connecting after
 *    close() must fail.
 */
public interface EventServer extends AutoCloseable {

    void start() throws IOException;

    int port();

    @Override
    void close();
}
