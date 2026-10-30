package dev.conc.p12;

class C07BlockingServerTest extends ServerContract {
    @Override
    EventServer create(EventSink sink) {
        return new BlockingEventServer(sink);
    }
}
