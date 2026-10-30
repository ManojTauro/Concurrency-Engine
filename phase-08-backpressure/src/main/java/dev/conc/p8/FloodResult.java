package dev.conc.p8;

import java.util.List;

/** What a flood run saw: queue depth every 50 ms, and how many events were made and handled. */
public record FloodResult(List<Integer> depthSamples, long produced, long consumed) {
}
