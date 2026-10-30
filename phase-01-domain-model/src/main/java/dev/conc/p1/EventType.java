package dev.conc.p1;

public enum EventType {
    VIEW, CLICK, ADD_TO_CART, PURCHASE;

    /** How much this event says about a user's interest: VIEW 1, CLICK 2, ADD_TO_CART 5, PURCHASE 10. */
    public int weight() {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }
}
