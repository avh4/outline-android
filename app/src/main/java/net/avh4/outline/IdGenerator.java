package net.avh4.outline;

import net.avh4.UniqueClock;

public class IdGenerator implements Generator<OutlineNodeId> {
    private final String deviceId;
    private final UniqueClock uniqueClock = new UniqueClock();

    public IdGenerator(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public synchronized OutlineNodeId next() {
        long next = uniqueClock.get();
        return new OutlineNodeId(deviceId + "-" + next);
    }
}
