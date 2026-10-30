package dev.conc.p9;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Given, complete, thread-safe: where failed events go to be looked at by a human. */
public final class DeadLetterQueue {
    private final List<DeadLetter> letters = new CopyOnWriteArrayList<>();

    public void add(DeadLetter letter) {
        letters.add(letter);
    }

    public List<DeadLetter> snapshot() {
        return List.copyOf(letters);
    }

    public int size() {
        return letters.size();
    }
}
