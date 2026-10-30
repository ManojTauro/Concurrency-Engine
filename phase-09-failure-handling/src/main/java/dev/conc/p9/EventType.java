package dev.conc.p9;

public enum EventType {
    VIEW(1), CLICK(2), ADD_TO_CART(5), PURCHASE(10);

    private final int weight;

    EventType(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
