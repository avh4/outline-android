package net.avh4;

public class UniqueClock {
    private long last = System.currentTimeMillis();

    public synchronized long get() {
        long next = System.currentTimeMillis();
        if (next <= last) {
            next = last + 1;
        }
        last = next;
        return next;
    }
}
