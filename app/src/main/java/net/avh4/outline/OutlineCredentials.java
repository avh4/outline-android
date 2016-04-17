package net.avh4.outline;

import android.content.Context;

import java.io.IOException;
import java.util.Properties;

public class OutlineCredentials {
    public String doorbellPrivateKey = null;

    public void initialize(Context context) throws IOException {
        Properties prop = new Properties();

        prop.load(context.getAssets().open("credentials.properties"));

        doorbellPrivateKey = prop.getProperty("doorbellPrivateKey");
    }
}
