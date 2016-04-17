package net.avh4.outline;

import android.app.Application;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;

public class OutlineApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new CrashlyticsCore());
    }
}
