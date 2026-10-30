package dev.conc.p5;

class C04ConcurrentStoreTest extends ProfileStoreContract {
    @Override
    ProfileStore create() {
        return new ConcurrentProfileStore();
    }
}
