package net.avh4.outline;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.avh4.android.ThrowableDialog;
import rx.Observable;
import rx.functions.Action1;

class OutlineAdapter extends BaseAdapter {
    private final Context context;
    private OutlineView current;

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
    public View getView(int position, View convertView, ViewGroup parent) {
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
        }
        return view;
    }
}
