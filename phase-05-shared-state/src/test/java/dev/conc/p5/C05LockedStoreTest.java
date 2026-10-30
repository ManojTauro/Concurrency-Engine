package dev.conc.p5;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class C05LockedStoreTest extends ProfileStoreContract {
    @Override
    ProfileStore create() {
        return new LockedProfileStore(16);
    }

    @Test
    void oneStripeIsStillCorrect() throws Exception {
        hammer(new LockedProfileStore(1), 8, TestEvents.events(20_000, 32), 32);
    }

    @Test
    void manyStripesAreStillCorrect() throws Exception {
        hammer(new LockedProfileStore(256), 8, TestEvents.events(20_000, 32), 32);
    }

    @Test
    void stripesMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new LockedProfileStore(0));
        assertThrows(IllegalArgumentException.class, () -> new LockedProfileStore(-3));
    }
}
