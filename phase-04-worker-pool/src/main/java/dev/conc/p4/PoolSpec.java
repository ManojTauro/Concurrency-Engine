package dev.conc.p4;

/**
 * @param namePrefix      worker threads are named namePrefix + "-" + n, n counting up from 1
 * @param core            core pool size
 * @param max             maximum pool size
 * @param queueCapacity   capacity of the bounded work queue (at least 1)
 * @param keepAliveMillis how long an idle thread above the core size lives
 * @param rejection       what to do when the queue is full and the pool is at max
 * @param prestart        start all core threads immediately instead of on demand
 */
public record PoolSpec(String namePrefix, int core, int max, int queueCapacity, long keepAliveMillis,
                       Rejection rejection, boolean prestart) {
}
