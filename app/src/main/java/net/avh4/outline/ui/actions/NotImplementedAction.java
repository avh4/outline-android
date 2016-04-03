package net.avh4.outline.ui.actions;

import android.content.Context;
import com.afollestad.materialdialogs.MaterialDialog;
import net.avh4.outline.AppAction;

public class NotImplementedAction implements AppAction {

    private final Context context;

    public NotImplementedAction(Context context) {
        this.context = context;
    }

    @Override
    public void run(OnError e) {
        new MaterialDialog.Builder(context)
                .content("Not implemented")
                .show();
    }
}
