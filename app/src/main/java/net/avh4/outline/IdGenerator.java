package net.avh4.outline;

class IdGenerator implements Generator<OutlineNodeId> {
    private final String deviceId;

    IdGenerator(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public OutlineNodeId next() {
        return new OutlineNodeId(deviceId + "-" + System.currentTimeMillis());
    }
}
