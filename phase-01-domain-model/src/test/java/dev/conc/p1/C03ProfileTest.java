package dev.conc.p1;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class C03ProfileTest {

    private static Event ev(long id, String user, String product, EventType type, long ts, long seq) {
        return new Event(id, user, product, type, ts, seq);
    }

    @Test
    void freshProfileIsEmpty() {
        UserBehaviourProfile p = new UserBehaviourProfile("user-1");
        assertEquals("user-1", p.userId());
        for (EventType t : EventType.values()) assertEquals(0, p.count(t));
        assertEquals(0, p.score());
        assertEquals(0, p.distinctProducts());
        assertEquals(0, p.lastUserSeq());
        assertEquals(-1, p.lastTimestampMillis());
    }

    @Test
    void applyCountsAndScores() {
        UserBehaviourProfile p = new UserBehaviourProfile("user-1");
        p.apply(ev(1, "user-1", "a", EventType.VIEW, 10, 1));
        p.apply(ev(2, "user-1", "a", EventType.VIEW, 11, 2));
        p.apply(ev(3, "user-1", "b", EventType.CLICK, 12, 3));
        p.apply(ev(4, "user-1", "b", EventType.PURCHASE, 13, 4));
        assertEquals(2, p.count(EventType.VIEW));
        assertEquals(1, p.count(EventType.CLICK));
        assertEquals(0, p.count(EventType.ADD_TO_CART));
        assertEquals(1, p.count(EventType.PURCHASE));
        assertEquals(1 + 1 + 2 + 10, p.score());
        assertEquals(2, p.distinctProducts());
        assertEquals(4, p.lastUserSeq());
        assertEquals(13, p.lastTimestampMillis());
    }

    @Test
    void lastValuesAreTheLargestSeen() {
        UserBehaviourProfile p = new UserBehaviourProfile("u");
        p.apply(ev(5, "u", "a", EventType.VIEW, 50, 5));
        p.apply(ev(3, "u", "a", EventType.VIEW, 30, 3));
        assertEquals(5, p.lastUserSeq());
        assertEquals(50, p.lastTimestampMillis());
    }

    @Test
    void eventForAnotherUserIsRejected() {
        UserBehaviourProfile p = new UserBehaviourProfile("user-1");
        assertThrows(IllegalArgumentException.class, () -> p.apply(ev(1, "user-2", "a", EventType.VIEW, 0, 1)));
        assertEquals(0, p.score());
    }

    @Test
    void nullEventIsRejected() {
        assertThrows(NullPointerException.class, () -> new UserBehaviourProfile("u").apply(null));
    }

    @Test
    void blankUserIdIsRejected() {
        assertThrows(NullPointerException.class, () -> new UserBehaviourProfile(null));
        assertThrows(IllegalArgumentException.class, () -> new UserBehaviourProfile(" "));
    }
}
