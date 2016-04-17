package net.avh4.outline;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import java.io.IOException;

public class OutlineApplication extends Application {
    public static final OutlineCredentials credentials = new OutlineCredentials();

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            credentials.initialize(getBaseContext());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load app credentials", e);
        }

        Fabric.with(this, new Crashlytics());
    }
}
