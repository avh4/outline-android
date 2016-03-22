package net.avh4.outline;

class IdGenerator {
    private final String deviceId;

    IdGenerator(String deviceId) {
        this.deviceId = deviceId;
    }

    OutlineNodeId next() {
        return new OutlineNodeId(deviceId + "-" + System.currentTimeMillis());
    }
}
