package dev.conc.p12;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class C06MultilevelQueueTest {

    @Test
    void thresholdsMustStartAtZeroAndRise() {
        assertThrows(IllegalArgumentException.class, () -> new MultilevelQueue<String>(new long[]{5, 10}));
        assertThrows(IllegalArgumentException.class, () -> new MultilevelQueue<String>(new long[]{0, 10, 10}));
        assertThrows(IllegalArgumentException.class, () -> new MultilevelQueue<String>(new long[]{0, 10, 5}));
        assertThrows(IllegalArgumentException.class, () -> new MultilevelQueue<String>(new long[]{}));
    }

    @Test
    void newEntriesStartAtLevelZeroAndComeOutFirstInFirstOut() {
        MultilevelQueue<String> q = new MultilevelQueue<>(new long[]{0, 100, 1000});
        LevelEntry<String> a = q.add("a"), b = q.add("b"), c = q.add("c");
        assertEquals(0, a.level());
        assertEquals(0, a.scheduledNanos());
        assertSame(a, q.poll());
        assertSame(b, q.poll());
        assertSame(c, q.poll());
        assertNull(q.poll());
    }

    @Test
    void chargingMovesAnEntryUpAtTheThresholds() {
        MultilevelQueue<String> q = new MultilevelQueue<>(new long[]{0, 1000, 10000});
        LevelEntry<String> e = q.add("e");
        q.poll();
        q.charge(e, 999);
        assertEquals(0, e.level());
        q.charge(e, 1);
        assertEquals(1, e.level(), "the threshold itself belongs to the higher level");
        q.charge(e, 8999);
        assertEquals(1, e.level());
        q.charge(e, 1);
        assertEquals(2, e.level());
        assertEquals(10000, e.scheduledNanos());
    }

    @Test
    void timeIsChargedToTheLevelTheEntryWasAtBeforeTheCharge() {
        MultilevelQueue<String> q = new MultilevelQueue<>(new long[]{0, 100});
        LevelEntry<String> e = q.add("e");
        q.poll();
        q.charge(e, 150);
        assertEquals(150, q.consumed(0));
        assertEquals(0, q.consumed(1));
        q.requeue(e);
        assertSame(e, q.poll());
        q.charge(e, 10);
        assertEquals(150, q.consumed(0));
        assertEquals(10, q.consumed(1));
    }

    @Test
    void aLongJobKeepsGettingTurnsButItsShareIsLimited() {
        MultilevelQueue<String> q = new MultilevelQueue<>(new long[]{0, 100});
        LevelEntry<String> x = q.add("x");
        LevelEntry<String> y = q.add("y");
        assertSame(x, q.poll());
        q.charge(x, 150);
        q.requeue(x);
        // x is now at level 1 with 150 consumed at level 0; y waits at level 0.
        // Level 0 weighs 150 * 1, level 1 weighs consumed * 2: level 1 wins while 2 * consumed(1) < 150.
        int turnsForX = 0;
        while (true) {
            LevelEntry<String> next = q.poll();
            if (next == y) break;
            assertSame(x, next);
            turnsForX++;
            q.charge(x, 10);
            q.requeue(x);
            assertTrue(turnsForX < 100, "y is starved");
        }
        assertEquals(8, turnsForX);
        assertEquals(80, q.consumed(1));
    }

    @Test
    void tiesGoToTheLowerLevel() {
        MultilevelQueue<String> q = new MultilevelQueue<>(new long[]{0, 100, 1000});
        LevelEntry<String> low = q.add("low");
        LevelEntry<String> high = q.add("high");
        q.poll();
        q.poll();
        q.charge(high, 1000);
        q.requeue(high);
        q.requeue(low);
        // consumed(0) = 1000, consumed(1) = 0, consumed(2) = 0: only levels 0 and 2 are non-empty
        // weights: level 0 -> 1000, level 2 -> 0, so level 2 is chosen
        assertSame(high, q.poll());
        q.requeue(high);
        q.charge(high, 250);
        // consumed(2) = 250 -> weight 1000, equal to level 0 -> the lower level wins
        assertSame(low, q.poll());
    }

    @Test
    void requeueGoesToTheTailOfItsLevel() {
        MultilevelQueue<String> q = new MultilevelQueue<>(new long[]{0, 100});
        LevelEntry<String> a = q.add("a"), b = q.add("b");
        assertSame(a, q.poll());
        q.requeue(a);
        assertSame(b, q.poll());
        assertSame(a, q.poll());
        assertNull(q.poll());
    }
}
