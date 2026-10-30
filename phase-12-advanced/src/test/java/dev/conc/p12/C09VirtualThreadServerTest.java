package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class C09VirtualThreadServerTest extends ServerContract {
    @Override
    EventServer create(EventSink sink) {
        return new VirtualThreadEventServer(sink);
    }

    @Test
    void connectionsAreHandledOnVirtualThreadsAndPlatformThreadsStaySmall() throws Exception {
        started();
        int before = Thread.getAllStackTraces().size();
        List<Socket> conns = new ArrayList<>();
        for (int i = 0; i < 500; i++) conns.add(connect());
        for (int i = 0; i < 500; i++) send(conns.get(i), "v" + i + ",p,VIEW\n");
        for (int i = 0; i < 500; i++) assertEquals("OK", reader(conns.get(i)).readLine());
        int after = Thread.getAllStackTraces().size();
        assertTrue(after - before <= 6, "platform threads before " + before + ", with 500 connections " + after);
        assertFalse(sinkThreads.isEmpty());
        sinkThreads.forEach(t -> assertTrue(t.isVirtual(), "the sink ran on platform thread " + t.getName()));
    }
}
