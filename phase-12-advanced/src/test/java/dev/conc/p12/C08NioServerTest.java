package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class C08NioServerTest extends ServerContract {
    @Override
    EventServer create(EventSink sink) {
        return new NioEventServer(sink);
    }

    @Test
    void threadCountDoesNotGrowWithConnections() throws Exception {
        started();
        int before = Thread.getAllStackTraces().size();
        List<Socket> conns = new ArrayList<>();
        for (int i = 0; i < 300; i++) conns.add(connect());
        for (int i = 0; i < 300; i++) send(conns.get(i), "c" + i + ",p,VIEW\n");
        for (int i = 0; i < 300; i++) assertEquals("OK", reader(conns.get(i)).readLine());
        int after = Thread.getAllStackTraces().size();
        assertTrue(after - before <= 4, "threads before " + before + ", with 300 connections " + after);
    }

    @Test
    void everythingRunsOnTheOneSelectorThread() throws Exception {
        started();
        for (int i = 0; i < 20; i++) {
            Socket s = connect();
            send(s, "u" + i + ",p,VIEW\n");
            BufferedReader in = reader(s);
            assertEquals("OK", in.readLine());
        }
        Set<String> names = sinkThreads.stream().map(Thread::getName).collect(Collectors.toSet());
        assertEquals(Set.of("p12-srv-selector"), names);
    }

    @Test
    void aBigResponseBacklogToAClientThatReadsSlowlyDoesNotStopOtherClients() throws Exception {
        started();
        Socket slow = connect();
        String junk = "x".repeat(200);
        int lines = 20_000;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines; i++) sb.append("slow,p").append(i).append(',').append(junk).append('\n');
        Thread writer = new Thread(() -> {
            try {
                send(slow, sb.toString());
            } catch (Exception ignored) {
            }
        });
        writer.start();
        Thread.sleep(500);
        // the answers (about 4.6 MB of "ERR unknown type: xxx...") do not fit the socket buffers while this client is not reading
        Socket fast = connect();
        send(fast, "fast,p,VIEW\n");
        long t0 = System.nanoTime();
        assertEquals("OK", reader(fast).readLine());
        assertTrue((System.nanoTime() - t0) / 1_000_000 < 2_000, "a client that does not read held up the whole server");
        BufferedReader in = reader(slow);
        for (int i = 0; i < lines; i++) assertEquals("ERR unknown type: " + junk, in.readLine(), "answer " + i);
        writer.join();
    }
}
