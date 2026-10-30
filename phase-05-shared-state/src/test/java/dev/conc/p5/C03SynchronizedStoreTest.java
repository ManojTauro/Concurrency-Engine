package dev.conc.p5;

class C03SynchronizedStoreTest extends ProfileStoreContract {
    @Override
    ProfileStore create() {
        return new SynchronizedProfileStore();
    }
}
