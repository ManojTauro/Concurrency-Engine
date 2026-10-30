package dev.conc.p1;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class C01EventModelTest {

    private static Event ev(long id, String user, String product, EventType type, long ts, long seq) {
        return new Event(id, user, product, type, ts, seq);
    }

    @Test
    void weights() {
        assertEquals(1, EventType.VIEW.weight());
        assertEquals(2, EventType.CLICK.weight());
        assertEquals(5, EventType.ADD_TO_CART.weight());
        assertEquals(10, EventType.PURCHASE.weight());
    }

    @Test
    void validEventIsCreated() {
        Event e = ev(1, "user-1", "product-1", EventType.VIEW, 0, 1);
        assertEquals("user-1", e.userId());
        assertEquals(EventType.VIEW, e.type());
    }

    @Test
    void equalValuesAreEqual() {
        assertEquals(ev(1, "u", "p", EventType.CLICK, 5, 1), ev(1, "u", "p", EventType.CLICK, 5, 1));
        assertEquals(ev(1, "u", "p", EventType.CLICK, 5, 1).hashCode(), ev(1, "u", "p", EventType.CLICK, 5, 1).hashCode());
    }

    @Test
    void nullsAreRejectedWithNullPointerException() {
        assertThrows(NullPointerException.class, () -> ev(1, null, "p", EventType.VIEW, 0, 1));
        assertThrows(NullPointerException.class, () -> ev(1, "u", null, EventType.VIEW, 0, 1));
        assertThrows(NullPointerException.class, () -> ev(1, "u", "p", null, 0, 1));
    }

    @Test
    void blanksAreRejectedWithIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ev(1, "", "p", EventType.VIEW, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> ev(1, "   ", "p", EventType.VIEW, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> ev(1, "u", " ", EventType.VIEW, 0, 1));
    }

    @Test
    void numbersOutOfRangeAreRejectedWithIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> ev(0, "u", "p", EventType.VIEW, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> ev(-1, "u", "p", EventType.VIEW, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> ev(1, "u", "p", EventType.VIEW, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> ev(1, "u", "p", EventType.VIEW, 0, 0));
    }

    @Test
    void zeroTimestampIsAllowed() {
        assertDoesNotThrow(() -> ev(1, "u", "p", EventType.VIEW, 0, 1));
    }
}
