package net.avh4.outline;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import net.avh4.android.OnItemCheckedChangedListener;
import net.avh4.android.ThrowableDialog;
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

import static android.view.View.GONE;

class OutlineAdapter extends RecyclerView.Adapter<OutlineAdapter.MyViewHolder> {
    private final Context context;
    private OutlineView current;
    private PVector<Integer> dragMap;
    private OnItemCheckedChangedListener onItemCheckedChangedListener;
    private AdapterView.OnItemClickListener onItemClickListener;
    private AdapterView.OnItemLongClickListener onItemLongClickListener;
    private OnStartDragListener onStartDragListener;
    private OnItemsReorderedListener onItemsReorderedListener;
    public final ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.Callback() {
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            System.out.println("from: " + fromPosition + ", to: " + toPosition);

            Integer from = dragMap.get(fromPosition);
            Integer to = dragMap.get(toPosition);

            dragMap = dragMap
                    .with(fromPosition, to)
                    .with(toPosition, from);

            notifyItemMoved(fromPosition, toPosition);

            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                ((MyViewHolder) viewHolder).onItemSelected();
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            ((MyViewHolder) viewHolder).onItemClear();
            onItemsReorderedListener.onItemsReordered(current, dragMap);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
    private Mode mode;
    private ReplaySubject<Mode> modeSubject = ReplaySubject.createWithSize(1);

    OutlineAdapter(final Context context, Observable<OutlineView> outlineView) {
        setHasStableIds(true);
        this.context = context;
        outlineView.subscribe(new Action1<OutlineView>() {
            @Override
            public void call(OutlineView outlineView) {
                current = outlineView;
                PVector<Integer> newDragMap = TreePVector.empty();
                for (int i = 0; i < current.getNumberOfChildren(); i++) {
                    newDragMap = newDragMap.plus(i);
                }
                dragMap = newDragMap;
                notifyDataSetChanged();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable e) {
                ThrowableDialog.show(context, new RuntimeException("Not implemented: handle observable errors", e));
            }
        });
        setMode(Mode.CHECKBOX);
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

    public void setOnItemsReorderedListener(OnItemsReorderedListener onItemsReorderedListener) {
        this.onItemsReorderedListener = onItemsReorderedListener;
    }

    public void showReorder() {
        setMode(Mode.REORDER);
    }

    public void showCheckboxes() {
        setMode(Mode.CHECKBOX);
    }

    public Observable<Mode> getMode() {
        return modeSubject;
    }

    private void setMode(Mode mode) {
        this.mode = mode;
        modeSubject.onNext(mode);
        notifyDataSetChanged();
    }

    public OutlineNode getItem(int position) {
        return current.getChild(dragMap.get(position));
    }

    @Override
    public int getItemCount() {
        if (current == null) {
            return 0;
        } else {
            assert dragMap.size() == current.getNumberOfChildren();
            return dragMap.size();
        }
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId().hashCode();
//        return dragMap.get(position);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater vi = LayoutInflater.from(context);
        View view = vi.inflate(R.layout.list_item_outline, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        view.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        View view = holder.itemView;
        OutlineNode item = getItem(position);
        if (item != null) {

            holder.text1.setText(item.getText());
            setStrikethrough(holder.text1, item.isCompleted());

            int displayCount = current.getDisplayCount(item);
            if (displayCount > 0) {
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(context.getString(R.string.item_child_count, displayCount));
                holder.checkBox.setVisibility(View.INVISIBLE);
            } else {
                holder.text2.setVisibility(GONE);
                holder.text2.setText("");
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setOnCheckedChangeListener(null);
                holder.checkBox.setChecked(item.isCompleted());
                holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        onItemCheckedChangedListener.onItemCheckedChanged(position, isChecked);
                    }
                });
            }

            switch (mode) {
                case CHECKBOX:
                    holder.checkboxContainer.setVisibility(View.VISIBLE);
                    holder.dragHandle.setVisibility(GONE);
                    break;
                case REORDER:
                    holder.checkboxContainer.setVisibility(GONE);
                    holder.dragHandle.setVisibility(View.VISIBLE);
                    break;
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

            holder.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        onStartDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public enum Mode {CHECKBOX, REORDER}

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public final TextView text1;
        public final TextView text2;
        public final CheckBox checkBox;
        public final View checkboxContainer;
        public final View dragHandle;

        public MyViewHolder(View view) {
            super(view);

            text1 = (TextView) view.findViewById(android.R.id.text1);
            text2 = (TextView) view.findViewById(android.R.id.text2);
            checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
            checkboxContainer = view.findViewById(R.id.checkbox_container);
            dragHandle = view.findViewById(R.id.drag_handle);
        }

        public void onItemSelected() {
            itemView.setBackgroundColor(0x77CCCCCC);
        }

        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
