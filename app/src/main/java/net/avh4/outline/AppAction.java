package net.avh4.outline;

public interface AppAction {
    void run(OnError e);

    interface OnError {
        void onError(Throwable err);
    }
}
