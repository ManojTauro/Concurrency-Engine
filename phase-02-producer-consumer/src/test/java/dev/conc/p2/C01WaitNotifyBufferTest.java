package dev.conc.p2;

class C01WaitNotifyBufferTest extends BufferContract {
    @Override
    Buffer<Integer> create(int capacity) {
        return new WaitNotifyBuffer<>(capacity);
    }
}
