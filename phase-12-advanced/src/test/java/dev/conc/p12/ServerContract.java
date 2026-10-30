package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** What every correct EventServer must do. */
abstract class ServerContract {

    abstract EventServer create(EventSink sink);

    final List<Event> received = Collections.synchronizedList(new ArrayList<>());
    final Set<Thread> sinkThreads = Collections.synchronizedSet(new HashSet<>());
    final List<Socket> sockets = Collections.synchronizedList(new ArrayList<>());
    EventServer server;

    EventServer started() throws IOException {
        server = create(e -> {
            sinkThreads.add(Thread.currentThread());
            if (e.userId().equals("bad")) throw new IllegalStateException("sink says no");
            received.add(e);
        });
        server.start();
        return server;
    }

    @AfterEach
    void cleanup() throws IOException {
        for (Socket s : sockets) s.close();
        if (server != null) server.close();
    }

    Socket connect() throws IOException {
        Socket s = new Socket(InetAddress.getLoopbackAddress(), server.port());
        s.setSoTimeout(5_000);
        sockets.add(s);
        return s;
    }

    static BufferedReader reader(Socket s) throws IOException {
        return new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
    }

    static void send(Socket s, String text) throws IOException {
        OutputStream out = s.getOutputStream();
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    static boolean serverThreadAlive() {
        return Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.isAlive() && t.getName().startsWith("p12-srv-"));
    }

    @Test
    void answersEachLineWithOkAndPassesTheEventsOn() throws Exception {
        started();
        Socket s = connect();
        BufferedReader in = reader(s);
        send(s, "alice,sku-1,VIEW\nbob,sku-2,PURCHASE\n");
        assertEquals("OK", in.readLine());
        assertEquals("OK", in.readLine());
        send(s, "carol,sku-3,CLICK\n");
        assertEquals("OK", in.readLine());
        assertEquals(3, received.size());
        assertEquals("alice", received.get(0).userId());
        assertEquals("sku-2", received.get(1).productId());
        assertEquals(EventType.PURCHASE, received.get(1).type());
        assertEquals(EventType.CLICK, received.get(2).type());
        assertEquals(3, received.stream().map(Event::eventId).distinct().count());
    }

    @Test
    void badLinesGetErrAndTheConnectionStaysOpen() throws Exception {
        started();
        Socket s = connect();
        BufferedReader in = reader(s);
        send(s, "only-one-field\nu,p,NOPE\n\nu,p,VIEW\n");
        assertTrue(in.readLine().startsWith("ERR"));
        assertTrue(in.readLine().startsWith("ERR"));
        assertTrue(in.readLine().startsWith("ERR"));
        assertEquals("OK", in.readLine());
        assertEquals(1, received.size());
    }

    @Test
    void aSinkFailureBecomesErrAndTheServerCarriesOn() throws Exception {
        started();
        Socket s = connect();
        BufferedReader in = reader(s);
        send(s, "bad,p,VIEW\ngood,p,VIEW\n");
        String first = in.readLine();
        assertTrue(first.startsWith("ERR") && first.contains("sink says no"), first);
        assertEquals("OK", in.readLine());
    }

    @Test
    void carriageReturnBeforeTheNewlineIsIgnored() throws Exception {
        started();
        Socket s = connect();
        BufferedReader in = reader(s);
        send(s, "u,p,CLICK\r\n");
        assertEquals("OK", in.readLine());
        assertEquals(EventType.CLICK, received.get(0).type());
    }

    @Test
    void aHalfWrittenLineWaitsForItsNewlineAndBlocksNobodyElse() throws Exception {
        started();
        Socket slow = connect();
        BufferedReader slowIn = reader(slow);
        send(slow, "slow,p1,");
        Thread.sleep(100);
        Socket fast = connect();
        BufferedReader fastIn = reader(fast);
        long t0 = System.nanoTime();
        send(fast, "fast,p2,VIEW\n");
        assertEquals("OK", fastIn.readLine());
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 1_000, "another client was held up by the half-written line");
        assertEquals(1, received.size());
        send(slow, "VIEW\n");
        assertEquals("OK", slowIn.readLine());
        assertEquals(2, received.size());
    }

    @Test
    void aLineSplitAcrossManySmallWritesIsAssembled() throws Exception {
        started();
        Socket s = connect();
        BufferedReader in = reader(s);
        for (String piece : new String[]{"al", "ice", ",sk", "u-9,P", "URCHA", "SE", "\n"}) {
            send(s, piece);
            Thread.sleep(20);
        }
        assertEquals("OK", in.readLine());
        assertEquals("alice", received.get(0).userId());
        assertEquals(EventType.PURCHASE, received.get(0).type());
    }

    @Test
    void aClientThatDisconnectsMidLineLeavesNoEventAndNoDamage() throws Exception {
        started();
        Socket ghost = connect();
        send(ghost, "ghost,p,VI");
        ghost.close();
        Thread.sleep(300);
        Socket s = connect();
        BufferedReader in = reader(s);
        send(s, "real,p,VIEW\n");
        assertEquals("OK", in.readLine());
        Thread.sleep(200);
        assertEquals(1, received.size());
        assertEquals("real", received.get(0).userId());
    }

    @Test
    void manyClientsSendingAtOnceAllGetTheirAnswersInOrder() throws Exception {
        started();
        int clients = 100, lines = 50;
        List<Thread> ts = new ArrayList<>();
        AtomicReference<String> failure = new AtomicReference<>();
        for (int c = 0; c < clients; c++) {
            int id = c;
            ts.add(new Thread(() -> {
                try {
                    Socket s = connect();
                    BufferedReader in = reader(s);
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < lines; k++) sb.append("client-").append(id).append(",item-").append(k).append(",VIEW\n");
                    send(s, sb.toString());
                    for (int k = 0; k < lines; k++) {
                        String r = in.readLine();
                        if (!"OK".equals(r)) failure.compareAndSet(null, "client " + id + " line " + k + ": " + r);
                    }
                } catch (Exception e) {
                    failure.compareAndSet(null, e.toString());
                }
            }));
        }
        assertTimeoutPreemptively(Duration.ofSeconds(60), () -> {
            ts.forEach(Thread::start);
            for (Thread t : ts) t.join();
        });
        assertNull(failure.get());
        assertEquals(clients * lines, received.size());
        assertEquals(clients * lines, received.stream().map(Event::eventId).distinct().count(), "event ids must be unique");
        for (int c = 0; c < clients; c++) {
            String user = "client-" + c;
            int expected = 0;
            for (Event e : new ArrayList<>(received)) {
                if (!e.userId().equals(user)) continue;
                assertEquals("item-" + expected, e.productId(), user + " out of order");
                expected++;
            }
            assertEquals(lines, expected, user);
        }
    }

    @Test
    void manyIdleConnectionsAreFineAndStillServed() throws Exception {
        started();
        List<Socket> idle = new ArrayList<>();
        for (int i = 0; i < 300; i++) idle.add(connect());
        for (int i = 0; i < 300; i++) send(idle.get(i), "idle-" + i + ",p,VIEW\n");
        for (int i = 0; i < 300; i++) assertEquals("OK", reader(idle.get(i)).readLine());
        assertEquals(300, received.size());
    }

    @Test
    void closeStopsAcceptingDisconnectsClientsAndEndsTheThreads() throws Exception {
        started();
        Socket s = connect();
        BufferedReader in = reader(s);
        send(s, "u,p,VIEW\n");
        assertEquals("OK", in.readLine());
        int port = server.port();
        server.close();
        server.close();
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        boolean eof = false;
        while (System.nanoTime() < deadline && !eof) {
            try {
                eof = in.read() == -1;
            } catch (IOException e) {
                eof = true;
            }
        }
        assertTrue(eof, "the open client connection should be closed by the server");
        assertThrows(IOException.class, () -> new Socket(InetAddress.getLoopbackAddress(), port).close());
        deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (serverThreadAlive() && System.nanoTime() < deadline) Thread.sleep(20);
        assertFalse(serverThreadAlive(), "server threads should have ended");
    }
}
