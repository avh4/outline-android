package net.avh4.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.pcollections.PVector;

public class PVectorAdapter<T> extends BaseAdapter {
    private final Context context;
    private final int itemLayout;
    private final int textViewId;
    private PVector<T> data;

    public PVectorAdapter(Context context, int itemLayout, int textViewId, PVector<T> data) {
        this.context = context;
        this.itemLayout = itemLayout;
        this.textViewId = textViewId;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(context);
            view = vi.inflate(itemLayout, null);
        }

        T item = getItem(position);
        if (item != null) {
            TextView textView = (TextView) view.findViewById(textViewId);
            textView.setText(item.toString());
        }
        return view;
    }

    public void update(PVector<T> newData) {
        data = newData;
        notifyDataSetChanged();
    }
}
