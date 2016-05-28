package net.avh4;

public interface Action1E<T, E extends Exception> {
    void process(T inputStream) throws E;
}
