package dev.conc.p6;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Given, complete. The obvious design: a plain thread pool and a lock per profile. Every event is
 * applied safely (counts are exact) but NOT in order, because two events of one user can sit in
 * the queue and be picked up by two different workers.
 */
public final class NaivePoolStore implements OrderedStore {
    private final ExecutorService pool;
    private final ConcurrentHashMap<String, UserBehaviourProfile> profiles = new ConcurrentHashMap<>();
    private final LongAdder total = new LongAdder();
    private final LongAdder violations = new LongAdder();
    private final AtomicLong submitted = new AtomicLong();
    private final AtomicLong finished = new AtomicLong();

    public NaivePoolStore(int threads) {
        this.pool = Executors.newFixedThreadPool(threads);
    }

    @Override
    public void record(Event e) {
        submitted.incrementAndGet();
        pool.execute(() -> {
            try {
                UserBehaviourProfile p = profiles.computeIfAbsent(e.userId(), UserBehaviourProfile::new);
                synchronized (p) {
                    if (e.userSeq() <= p.lastUserSeq()) violations.increment();
                    p.apply(e);
                }
                total.increment();
            } finally {
                finished.incrementAndGet();
            }
        });
    }

    @Override
    public void awaitIdle() throws InterruptedException {
        while (finished.get() < submitted.get()) Thread.sleep(1);
    }

    @Override
    public long count(String userId, EventType type) {
        UserBehaviourProfile p = profiles.get(userId);
        if (p == null) return 0;
        synchronized (p) {
            return p.count(type);
        }
    }

    @Override
    public long score(String userId) {
        UserBehaviourProfile p = profiles.get(userId);
        if (p == null) return 0;
        synchronized (p) {
            return p.score();
        }
    }

    @Override
    public long totalEvents() {
        return total.sum();
    }

    @Override
    public long orderViolations() {
        return violations.sum();
    }

    @Override
    public void close() {
        pool.shutdown();
    }
}
