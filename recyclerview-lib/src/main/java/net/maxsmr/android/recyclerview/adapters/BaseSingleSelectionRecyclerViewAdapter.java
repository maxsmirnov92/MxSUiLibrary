package net.maxsmr.android.recyclerview.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Checkable;

import com.bejibx.android.recyclerview.selection.SelectionHelper;

import java.util.Collection;
import java.util.Set;

public abstract class BaseSingleSelectionRecyclerViewAdapter<I, VH extends BaseRecyclerViewAdapter.ViewHolder<I>> extends BaseRecyclerViewAdapter<I, VH> {

    private int selection = RecyclerView.NO_POSITION;

    @Nullable
    private Drawable defaultDrawable, selectionDrawable;

    @Nullable
    private OnSelectedChangeListener selectedChangeListener;

    public BaseSingleSelectionRecyclerViewAdapter(@NotNull Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items) {
        this(context, itemLayoutId, items, null, null);
    }

    public BaseSingleSelectionRecyclerViewAdapter(Context context, @LayoutRes int itemLayoutId, @Nullable Collection<I> items, Drawable defaultDrawable, Drawable selectionDrawable) {
        super(context, itemLayoutId, items);
        if (defaultDrawable != null) {
            setDefaultDrawable(defaultDrawable);
        }
        if (selectionDrawable != null) {
            setSelectionDrawable(selectionDrawable);
        }
    }

    @NotNull
    public abstract Set<SelectionHelper.SelectMode> getSelectionModes(int position);

    @Nullable
    public Drawable getDefaultDrawable() {
        return defaultDrawable;
    }

    @CallSuper
    public void setDefaultDrawable(@Nullable Drawable defaultDrawable) {
        this.defaultDrawable = defaultDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    @Nullable
    public Drawable getSelectionDrawable() {
        return selectionDrawable;
    }

    @CallSuper
    public void setSelectionDrawable(@Nullable Drawable selectionDrawable) {
        this.selectionDrawable = selectionDrawable;
        if (isNotifyOnChange())
            notifyDataSetChanged();
    }

    protected boolean allowTogglingSelection() {
        return true;
    }

//    @Override
//    protected final boolean allowSetClickListener(@Nullable I item, int position) {
//        return false;
//    }
//
//    @Override
//    protected final boolean allowSetLongClickListener(@Nullable I item, int position) {
//        return false;
//    }

    private void prevSelection() {

    }

    @Override
    @CallSuper
    protected void processItem(@NotNull VH holder, @Nullable I item, int position) {
        super.processItem(holder, item, position);
        processSelection(holder, item, position);
    }

    protected void processSelection(@NotNull VH holder, @Nullable final I item, final int position) {
        for (SelectionHelper.SelectMode mode : getSelectionModes(position)) {
            switch (mode) {
                case CLICK:
                    holder.itemView.setClickable(true);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position == selection) {
                                if (allowTogglingSelection()) {
                                    toggleSelection(position, true);
                                } else {
                                    onReselect(position, true);
                                }
                            } else {
                                setSelection(position, true);
                            }
                            if (allowSetClickListener(item, position)) {
                                if (mItemClickListener != null) {
                                    mItemClickListener.onItemClick(position, item);
                                }
                            }
                        }
                    });
                    break;

                case LONG_CLICK:
                    holder.itemView.setLongClickable(true);
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (position == selection) {
                                if (allowTogglingSelection()) {
                                    toggleSelection(position, true);
                                } else {
                                    onReselect(position, true);
                                }
                            } else {
                                setSelection(position, true);
                            }
                            if (allowSetLongClickListener(item, position)) {
                                if (mItemLongClickListener != null) {
                                    mItemLongClickListener.onItemLongClick(position, item);
                                }
                            }
                            return true;
                        }
                    });
                    break;
            }
        }
        boolean isItemSelected = isItemPositionSelected(position);
        if (holder.itemView instanceof Checkable) {
            ((Checkable) holder.itemView).setChecked(isItemSelected);
        }
        if (isItemSelected) {
            onProcessItemSelected(holder);
        } else {
            onProcessItemNotSelected(holder);
        }
    }

    protected void onProcessItemSelected(@NotNull VH holder) {

    }

    protected void onProcessItemNotSelected(@NotNull VH holder) {

    }

    public boolean isSelected() {
        return getSelectedPosition() != RecyclerView.NO_POSITION;
    }

    public int getSelectedPosition() {
        if (selection >= 0 && selection < getItemCount()) {
            return selection;
        }
        return RecyclerView.NO_POSITION;
    }

    @Nullable
    public I getSelectedItem() {
        int selection = getSelectedPosition();
        if (selection >= 0 && selection < getItemCount()) {
            return getItem(selection);
        }
        return null;
    }

    public boolean isItemPositionSelected(int position) {
        rangeCheck(position);
        return selection != RecyclerView.NO_POSITION && selection == position;
    }

    public boolean isItemSelected(I item) {
        return isItemPositionSelected(indexOf(item));
    }

    public void setSelectionByItem(I item) {
        setSelection(indexOf(item));
    }

    public void setSelection(int selection) {
        setSelection(selection, false);
    }

    protected void setSelection(int selection, boolean fromUser) {
        rangeCheck(selection);
        if (this.selection != selection) {
            int previousSelection = this.selection;
            this.selection = selection;
            onSelectionChanged(previousSelection, this.selection, fromUser);
            if (isNotifyOnChange()) {
                notifyItemChanged(previousSelection);
                notifyItemChanged(selection);
            }
        }
    }

    public void resetSelection() {
        resetSelection(false);
    }

    private void resetSelection(boolean fromUser) {
        if (isSelected()) {
            int previousSelection = this.selection;
            this.selection = RecyclerView.NO_POSITION;
            onSelectionChanged(previousSelection, this.selection, fromUser);
            if (isNotifyOnChange()) {
                notifyItemChanged(previousSelection);
            }
        }
    }

    public void toggleSelection(int selection) {
        toggleSelection(selection, false);
    }

    private void toggleSelection(int selection, boolean fromUser) {
        if (isSelected()) {
            resetSelection(fromUser);
        } else {
            setSelection(selection, fromUser);
        }
    }

    public boolean previousSelection(boolean loop) {
        boolean changed = false;
        int selection = getSelectedPosition();
        if (selection != RecyclerView.NO_POSITION) {
            if (selection >= 1) {
                setSelection(--selection);
                changed = true;
            } else if (loop) {
                setSelection(getItemCount() - 1);
                changed = true;
            }
        }
        return changed;
    }

    public boolean nextSelection(boolean loop) {
        boolean changed = false;
        int selection = getSelectedPosition();
        if (selection != RecyclerView.NO_POSITION) {
            if (selection < getItemCount() - 1) {
                setSelection(++selection);
                changed = true;
            } else if (loop) {
                setSelection(0);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * called before {@link #notifyItemChanged(int)}}
     */
    @CallSuper
    protected void onSelectionChanged(int from, int to, boolean fromUser) {
        if (selectedChangeListener != null) {
            if (selection != RecyclerView.NO_POSITION) {
                selectedChangeListener.onSetSelection(from, to, fromUser);
            } else {
                selectedChangeListener.onResetSelection(from, fromUser);
            }
        }
    }

    @CallSuper
    protected void onReselect(int index, boolean fromUser) {
        if (selectedChangeListener != null) {
            selectedChangeListener.onReselect(index, fromUser);
        }
    }

    public void setOnSelectedChangeListener(@Nullable OnSelectedChangeListener selectedChangeListener) {
        this.selectedChangeListener = selectedChangeListener;
    }

    @Override
    protected void onItemAdded(int to, @Nullable I item) {
        fixSelectionIndexOnAdd(to, 1);
        super.onItemAdded(to, item);
    }

    @Override
    protected void onItemsAdded(int to, @NotNull Collection<I> items) {
        fixSelectionIndexOnAdd(to, items.size());
        super.onItemsAdded(to, items);
    }

    @Override
    protected void onItemRemoved(int from, @Nullable I item) {
        fixSelectionIndexOnRemove(from, 1);
        super.onItemRemoved(from, item);
    }

    @Override
    protected void onItemsRangeRemoved(int from, int to, int previousSize) {
        fixSelectionIndexOnRemove(from, from == to? 1: to - from);
        super.onItemsRangeRemoved(from, to, previousSize);
    }

    @Override
    protected void onItemsSet() {
        selection = RecyclerView.NO_POSITION;
        super.onItemsSet();
    }

    private void fixSelectionIndexOnAdd(int to, int count) {
        if (count >= 1) {
            if (selection != RecyclerView.NO_POSITION) {
                if (selection >= to) {
                    selection += count;
                }
            }
        }
    }

    private void fixSelectionIndexOnRemove(int from, int count) {
        if (count >= 1) {
            if (selection != RecyclerView.NO_POSITION) {
                if (selection > from && from + count < selection) {
                    selection -= count;
                }
                else if (selection >= from && selection <= from + count) {
                    selection = RecyclerView.NO_POSITION;
                }
            }
        }
    }

    public interface OnSelectedChangeListener {

        void onSetSelection(int fromIndex, int toIndex, boolean fromUser);

        void onResetSelection(int onIndex, boolean fromUser);

        void onReselect(int index, boolean fromUser);
    }
}
