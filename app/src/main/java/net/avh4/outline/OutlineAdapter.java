package net.avh4.outline;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import net.avh4.android.OnItemCheckedChangedListener;
import net.avh4.android.ThrowableDialog;
import rx.Observable;
import rx.functions.Action1;

class OutlineAdapter extends RecyclerView.Adapter {
    private final Context context;
    private OutlineView current;
    private OnItemCheckedChangedListener onItemCheckedChangedListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;

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

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    void setOnItemCheckedChangedListener(OnItemCheckedChangedListener onItemCheckedChangedListener) {
        this.onItemCheckedChangedListener = onItemCheckedChangedListener;
    }

    public OutlineNode getItem(int position) {
        return current.getChild(position);
    }

    @Override
    public int getItemCount() {
        if (current == null) {
            return 0;
        } else {
            return current.getNumberOfChildren();
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(context);
        View view = vi.inflate(R.layout.list_item_outline, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        View view = holder.itemView;
        OutlineNode item = getItem(position);
        if (item != null) {
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);
            CheckBox checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);

            text1.setText(item.getText());
            setStrikethrough(text1, item.isCompleted());

            int displayCount = current.getDisplayCount(item);
            if (displayCount > 0) {
                text2.setVisibility(View.VISIBLE);
                text2.setText(context.getString(R.string.item_child_count, displayCount));
                checkBox.setVisibility(View.INVISIBLE);
            } else {
                text2.setVisibility(View.GONE);
                text2.setText("");
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(null);
                checkBox.setChecked(item.isCompleted());
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onItemCheckedChangedListener.onItemCheckedChanged(position, isChecked);
                    }
                });
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(null, v, position, getItemId(position));
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return onItemLongClickListener.onItemLongClick(null, v, position, getItemId(position));
                }
            });
        }
    }
}
