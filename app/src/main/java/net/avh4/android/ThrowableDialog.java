package net.avh4.android;

import android.content.Context;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import net.avh4.Throwable;
import net.avh4.outline.R;

import java.io.IOException;

public class ThrowableDialog {
    public static MaterialDialog show(Context context, IOException error) {
        String stackTrace = Throwable.getStackTraceAsString(error);
        TextView textView = new TextView(context);
        textView.setHorizontallyScrolling(true);
        textView.setText(stackTrace);
        return new MaterialDialog.Builder(context)
                .title(R.string.dialog_error_title)
                .cancelable(false)
                .customView(textView, true)
                .show();
    }
}
