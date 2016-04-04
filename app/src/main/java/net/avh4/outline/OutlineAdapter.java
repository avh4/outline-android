package net.avh4.outline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import net.avh4.android.OnItemCheckedChangedListener;
import net.avh4.android.ThrowableDialog;
import rx.Observable;
import rx.functions.Action1;

class OutlineAdapter extends BaseAdapter {
    private final Context context;
    private OutlineView current;
    private OnItemCheckedChangedListener onItemCheckedChangedListener;

    OutlineAdapter(final Context context, Observable<OutlineView> outlineView) {
        this.context = context;
        outlineView.subscribe(new Action1<OutlineView>() {
            @Override
            public void call(OutlineView outlineView) {
                current = outlineView;
                notifyDataSetChanged();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                ThrowableDialog.show(context, new RuntimeException("Not implemented: handle observable errors", e));
            }
        });
    }

    private static void setStrikethrough(TextView textView, boolean strikethrough) {
        int flags = textView.getPaintFlags();
        if (strikethrough) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;
        } else {
            flags &= (~Paint.STRIKE_THRU_TEXT_FLAG);
        }
        textView.setPaintFlags(flags);
    }

    public void setOnItemCheckedChangedListener(OnItemCheckedChangedListener onItemCheckedChangedListener) {
        this.onItemCheckedChangedListener = onItemCheckedChangedListener;
    }

    @Override
    public int getCount() {
        if (current == null) {
            return 0;
        } else {
            return current.getNumberOfChildren();
        }
    }

    @Override
    public OutlineNode getItem(int position) {
        return current.getChild(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            view = vi.inflate(R.layout.list_item_outline, null);
        }

        OutlineNode item = getItem(position);
        if (item != null) {
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(item.getText());
            setStrikethrough(textView, item.isCompleted());
            CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
            checkBox.setChecked(item.isCompleted());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    onItemCheckedChangedListener.onItemCheckedChanged(position, isChecked);
                }
            });
        }
        return view;
    }
}
